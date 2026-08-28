package com.example.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(
    tableName = "students",
    indices = [Index(value = ["studentId"], unique = true)]
)
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String,
    val name: String,
    val rollNo: String,
    val classId: String,
    val photoPath: String? = null,
    val registeredAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "face_embeddings",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["studentId"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studentId"])]
)
data class FaceEmbedding(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String,
    val embeddingJson: String, // Stored as JSON array string of Float
    val sampleIndex: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "class_sessions")
data class ClassSession(
    @PrimaryKey val sessionId: String,
    val courseName: String,
    val classId: String,
    val cooldownMinutes: Int = 30,
    val distanceThreshold: Float = 0.55f,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "attendance_logs",
    indices = [Index(value = ["studentId"]), Index(value = ["sessionId"])]
)
data class AttendanceLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String,
    val sessionId: String,
    val status: String = "PRESENT", // PRESENT, LATE, ABSENT, MANUAL_OVERRIDE
    val confidenceScore: Float,
    val livenessVerified: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun formattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formattedDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

// Data Transfer / Composite UI models
data class AttendanceWithStudent(
    val log: AttendanceLog,
    val student: Student?
)
