package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.models.AttendanceLog
import com.example.data.models.AttendanceWithStudent
import com.example.data.models.ClassSession
import com.example.data.models.FaceEmbedding
import com.example.data.models.Student
import com.example.data.vision.FaceEmbeddingExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AttendanceRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val studentDao = db.studentDao()
    private val embeddingDao = db.faceEmbeddingDao()
    private val sessionDao = db.classSessionDao()
    private val logDao = db.attendanceLogDao()

    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()
    val allSessions: Flow<List<ClassSession>> = sessionDao.getAllSessions()
    val activeSession: Flow<ClassSession?> = sessionDao.getActiveSession()
    val allLogs: Flow<List<AttendanceLog>> = logDao.getAllLogs()

    val logsWithStudents: Flow<List<AttendanceWithStudent>> = combine(
        logDao.getAllLogs(),
        studentDao.getAllStudents()
    ) { logs, students ->
        val studentMap = students.associateBy { it.studentId }
        logs.map { log ->
            AttendanceWithStudent(log, studentMap[log.studentId])
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getKnownEmbeddings(): List<FaceEmbedding> = withContext(Dispatchers.IO) {
        embeddingDao.getAllEmbeddings()
    }

    suspend fun registerStudent(
        studentId: String,
        name: String,
        rollNo: String,
        classId: String,
        photoPath: String?,
        embeddings: List<FloatArray>
    ): Boolean = withContext(Dispatchers.IO) {
        val student = Student(
            studentId = studentId,
            name = name,
            rollNo = rollNo,
            classId = classId,
            photoPath = photoPath
        )
        studentDao.insertStudent(student)

        val embeddingEntities = embeddings.mapIndexed { idx, vector ->
            FaceEmbedding(
                studentId = studentId,
                embeddingJson = FaceEmbeddingExtractor.toJson(vector),
                sampleIndex = idx + 1
            )
        }
        embeddingDao.insertAllEmbeddings(embeddingEntities)
        true
    }

    suspend fun getStudentById(studentId: String): Student? = withContext(Dispatchers.IO) {
        studentDao.getStudentById(studentId)
    }

    suspend fun deleteStudent(studentId: String) = withContext(Dispatchers.IO) {
        studentDao.deleteStudent(studentId)
    }

    suspend fun createOrUpdateSession(
        sessionId: String,
        courseName: String,
        classId: String,
        cooldownMinutes: Int,
        distanceThreshold: Float
    ) = withContext(Dispatchers.IO) {
        sessionDao.deactivateAllSessions()
        val session = ClassSession(
            sessionId = sessionId,
            courseName = courseName,
            classId = classId,
            cooldownMinutes = cooldownMinutes,
            distanceThreshold = distanceThreshold,
            isActive = true
        )
        sessionDao.insertSession(session)
    }

    suspend fun setActiveSession(sessionId: String) = withContext(Dispatchers.IO) {
        sessionDao.deactivateAllSessions()
        sessionDao.setActiveSession(sessionId)
    }

    suspend fun logAttendance(
        studentId: String,
        sessionId: String,
        status: String = "PRESENT",
        confidence: Float,
        livenessVerified: Boolean = true
    ): Long = withContext(Dispatchers.IO) {
        val log = AttendanceLog(
            studentId = studentId,
            sessionId = sessionId,
            status = status,
            confidenceScore = confidence,
            livenessVerified = livenessVerified
        )
        logDao.insertLog(log)
    }

    suspend fun getMostRecentLog(studentId: String, sessionId: String): AttendanceLog? = withContext(Dispatchers.IO) {
        logDao.getMostRecentLog(studentId, sessionId)
    }

    suspend fun updateLogStatus(logId: Long, newStatus: String) = withContext(Dispatchers.IO) {
        logDao.updateStatus(logId, newStatus)
    }

    suspend fun deleteLog(logId: Long) = withContext(Dispatchers.IO) {
        logDao.deleteLog(logId)
    }

    suspend fun clearAllLogs() = withContext(Dispatchers.IO) {
        logDao.clearAllLogs()
    }

    suspend fun seedDemoDataIfEmpty() = withContext(Dispatchers.IO) {
        val count = studentDao.getStudentById("STU-101")
        if (count == null) {
            // Default Session
            val defaultSession = ClassSession(
                sessionId = "CS101-SEC1",
                courseName = "Computer Vision & ML",
                classId = "CS-2026",
                cooldownMinutes = 30,
                distanceThreshold = 0.55f,
                isActive = true
            )
            sessionDao.insertSession(defaultSession)

            // Demo Student 1
            val s1 = Student("STU-101".hashCode().toLong(), "STU-101", "Alex Rivera", "2026-CS-01", "CS-2026")
            studentDao.insertStudent(s1)
            val v1 = FloatArray(128) { (it * 0.015f + 0.1f) % 0.8f }
            FaceEmbeddingExtractor.normalize(v1)
            embeddingDao.insertEmbedding(FaceEmbedding(studentId = "STU-101", embeddingJson = FaceEmbeddingExtractor.toJson(v1)))

            // Demo Student 2
            val s2 = Student("STU-102".hashCode().toLong(), "STU-102", "Priya Sharma", "2026-CS-02", "CS-2026")
            studentDao.insertStudent(s2)
            val v2 = FloatArray(128) { (it * 0.022f + 0.3f) % 0.8f }
            FaceEmbeddingExtractor.normalize(v2)
            embeddingDao.insertEmbedding(FaceEmbedding(studentId = "STU-102", embeddingJson = FaceEmbeddingExtractor.toJson(v2)))

            // Demo Student 3
            val s3 = Student("STU-103".hashCode().toLong(), "STU-103", "Jordan Chen", "2026-CS-03", "CS-2026")
            studentDao.insertStudent(s3)
            val v3 = FloatArray(128) { (it * 0.031f + 0.5f) % 0.8f }
            FaceEmbeddingExtractor.normalize(v3)
            embeddingDao.insertEmbedding(FaceEmbedding(studentId = "STU-103", embeddingJson = FaceEmbeddingExtractor.toJson(v3)))

            // Demo logs
            logDao.insertLog(AttendanceLog(studentId = "STU-101", sessionId = "CS101-SEC1", status = "PRESENT", confidenceScore = 0.94f, livenessVerified = true, timestamp = System.currentTimeMillis() - 3600000))
            logDao.insertLog(AttendanceLog(studentId = "STU-102", sessionId = "CS101-SEC1", status = "LATE", confidenceScore = 0.88f, livenessVerified = true, timestamp = System.currentTimeMillis() - 1800000))
        }
    }
}
