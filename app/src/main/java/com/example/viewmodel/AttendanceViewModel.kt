package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.AttendanceLog
import com.example.data.models.AttendanceWithStudent
import com.example.data.models.ClassSession
import com.example.data.models.FaceEmbedding
import com.example.data.models.Student
import com.example.data.repository.AttendanceRepository
import com.example.data.vision.CooldownManager
import com.example.data.vision.FaceEmbeddingExtractor
import com.example.data.vision.FaceMatcher
import com.example.data.vision.LivenessDetector
import com.example.data.vision.LivenessStatus
import com.example.export.CsvExporter
import com.google.mlkit.vision.face.Face
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class CameraScanState(
    val faceDetected: Boolean = false,
    val faceBoundingBox: android.graphics.Rect? = null,
    val livenessStatus: LivenessStatus = LivenessStatus.UNKNOWN,
    val matchedStudent: Student? = null,
    val confidence: Float = 0.0f,
    val isCooldownActive: Boolean = false,
    val remainingCooldownSeconds: Int = 0,
    val lastCheckInStatusMessage: String? = null
)

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {
    val repository = AttendanceRepository(application)
    val cooldownManager = CooldownManager()
    val livenessDetector = LivenessDetector()

    // Active State
    val activeSession: StateFlow<ClassSession?> = repository.activeSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<ClassSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow("ALL")
    val selectedStatusFilter: StateFlow<String> = _selectedStatusFilter.asStateFlow()

    val filteredLogs: StateFlow<List<AttendanceWithStudent>> = combine(
        repository.logsWithStudents,
        _searchQuery,
        _selectedStatusFilter
    ) { logs, query, filter ->
        logs.filter { item ->
            val matchesQuery = query.isEmpty() ||
                (item.student?.name?.contains(query, ignoreCase = true) == true) ||
                (item.student?.rollNo?.contains(query, ignoreCase = true) == true) ||
                (item.log.studentId.contains(query, ignoreCase = true))

            val matchesFilter = when (filter) {
                "ALL" -> true
                "PRESENT" -> item.log.status == "PRESENT" || item.log.status == "Present"
                "LATE" -> item.log.status == "LATE" || item.log.status == "Late"
                "ABSENT" -> item.log.status == "ABSENT" || item.log.status == "Absent"
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Camera Real-time Scan State
    private val _scanState = MutableStateFlow(CameraScanState())
    val scanState: StateFlow<CameraScanState> = _scanState.asStateFlow()

    // Registration Temp Embeddings
    private val _registrationEmbeddings = MutableStateFlow<List<FloatArray>>(emptyList())
    val registrationEmbeddings: StateFlow<List<FloatArray>> = _registrationEmbeddings.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDemoDataIfEmpty()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(filter: String) {
        _selectedStatusFilter.value = filter
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // Process Live Camera Frame with ML Kit Face
    fun processCameraFrame(face: Face, bitmap: Bitmap?) {
        viewModelScope.launch(Dispatchers.Default) {
            val session = activeSession.value ?: return@launch
            val liveness = livenessDetector.processFace(face)

            // Extract Face Embedding
            val queryVector = FaceEmbeddingExtractor.extractEmbedding(face, bitmap)

            // Query Known Embeddings
            val knownEmbeddings = repository.getKnownEmbeddings()
            val match = FaceMatcher.findBestMatch(
                queryVector,
                knownEmbeddings,
                threshold = session.distanceThreshold
            )

            if (match != null) {
                val student = repository.getStudentById(match.studentId)
                val inCooldown = !cooldownManager.canCheckIn(
                    studentId = match.studentId,
                    sessionId = session.sessionId,
                    cooldownMinutes = session.cooldownMinutes
                )
                val remSeconds = cooldownManager.getRemainingCooldownSeconds(
                    studentId = match.studentId,
                    sessionId = session.sessionId,
                    cooldownMinutes = session.cooldownMinutes
                )

                _scanState.value = CameraScanState(
                    faceDetected = true,
                    faceBoundingBox = face.boundingBox,
                    livenessStatus = liveness,
                    matchedStudent = student,
                    confidence = match.confidence,
                    isCooldownActive = inCooldown,
                    remainingCooldownSeconds = remSeconds
                )

                // Auto-mark attendance if anti-spoofing / liveness verified and not in cooldown
                val isLiveVerified = liveness == LivenessStatus.VERIFIED_LIVE || livenessDetector.isVerified()
                if (isLiveVerified && !inCooldown) {
                    cooldownManager.recordCheckIn(match.studentId, session.sessionId)
                    repository.logAttendance(
                        studentId = match.studentId,
                        sessionId = session.sessionId,
                        status = "PRESENT",
                        confidence = match.confidence,
                        livenessVerified = true
                    )
                    _toastMessage.value = "Attendance Logged: ${student?.name ?: match.studentId} (Present)"
                }
            } else {
                _scanState.value = CameraScanState(
                    faceDetected = true,
                    faceBoundingBox = face.boundingBox,
                    livenessStatus = liveness,
                    matchedStudent = null,
                    confidence = 0.0f
                )
            }
        }
    }

    fun onNoFaceDetected() {
        _scanState.value = CameraScanState(faceDetected = false)
        livenessDetector.reset()
    }

    // Registration Flow
    fun captureRegistrationSample(face: Face, bitmap: Bitmap?) {
        viewModelScope.launch(Dispatchers.Default) {
            val vector = FaceEmbeddingExtractor.extractEmbedding(face, bitmap)
            _registrationEmbeddings.value = _registrationEmbeddings.value + vector
            _toastMessage.value = "Sample ${_registrationEmbeddings.value.size} captured!"
        }
    }

    fun clearRegistrationSamples() {
        _registrationEmbeddings.value = emptyList()
    }

    fun addSampleEmbeddingVector(vector: FloatArray) {
        _registrationEmbeddings.value = _registrationEmbeddings.value + vector
        _toastMessage.value = "Sample ${_registrationEmbeddings.value.size} added!"
    }

    fun submitStudentRegistration(
        studentId: String,
        name: String,
        rollNo: String,
        classId: String,
        photoPath: String?,
        onSuccess: () -> Unit
    ) {
        if (studentId.isBlank() || name.isBlank() || rollNo.isBlank()) {
            _toastMessage.value = "Please complete all required fields."
            return
        }
        val samples = _registrationEmbeddings.value
        if (samples.isEmpty()) {
            // Generate fallback sample embedding
            val fallback = FloatArray(128) { (it * 0.017f + studentId.hashCode() % 100 * 0.01f) % 0.8f }
            FaceEmbeddingExtractor.normalize(fallback)
            viewModelScope.launch {
                repository.registerStudent(studentId, name, rollNo, classId, photoPath, listOf(fallback))
                clearRegistrationSamples()
                _toastMessage.value = "Student $name registered successfully!"
                onSuccess()
            }
        } else {
            viewModelScope.launch {
                repository.registerStudent(studentId, name, rollNo, classId, photoPath, samples)
                clearRegistrationSamples()
                _toastMessage.value = "Student $name registered with ${samples.size} face samples!"
                onSuccess()
            }
        }
    }

    // Session Management
    fun createSession(courseName: String, classId: String, cooldownMins: Int, threshold: Float) {
        val sessionId = "${courseName.take(4).uppercase()}-${System.currentTimeMillis() % 10000}"
        viewModelScope.launch {
            repository.createOrUpdateSession(sessionId, courseName, classId, cooldownMins, threshold)
            _toastMessage.value = "Class session $sessionId activated!"
        }
    }

    // Manual Attendance Override
    fun updateAttendanceStatus(logId: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateLogStatus(logId, newStatus)
            _toastMessage.value = "Attendance updated to $newStatus"
        }
    }

    fun setActiveSession(sessionId: String) {
        viewModelScope.launch {
            repository.setActiveSession(sessionId)
            _toastMessage.value = "Active session switched"
        }
    }

    fun deleteStudent(studentId: String) {
        viewModelScope.launch {
            repository.deleteStudent(studentId)
            _toastMessage.value = "Student profile deleted"
        }
    }

    fun seedDemoData() {
        viewModelScope.launch {
            repository.seedDemoDataIfEmpty()
            _toastMessage.value = "Demo data reseeded"
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearAllLogs()
            _toastMessage.value = "All logs cleared"
        }
    }

    // Export CSV
    fun exportAttendanceCsv(onExportReady: (File) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val logs = repository.allLogs.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList()).value
            val students = repository.allStudents.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList()).value
            val studentMap = students.associateBy { it.studentId }

            val file = CsvExporter.generateCsvReport(getApplication(), logs, studentMap)
            withContext(Dispatchers.Main) {
                onExportReady(file)
            }
        }
    }
}
