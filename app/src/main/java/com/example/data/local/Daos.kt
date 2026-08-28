package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.models.AttendanceLog
import com.example.data.models.ClassSession
import com.example.data.models.FaceEmbedding
import com.example.data.models.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY registeredAt DESC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE studentId = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: String): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Query("DELETE FROM students WHERE studentId = :studentId")
    suspend fun deleteStudent(studentId: String)

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCount(): Flow<Int>
}

@Dao
interface FaceEmbeddingDao {
    @Query("SELECT * FROM face_embeddings")
    suspend fun getAllEmbeddings(): List<FaceEmbedding>

    @Query("SELECT * FROM face_embeddings WHERE studentId = :studentId")
    suspend fun getEmbeddingsForStudent(studentId: String): List<FaceEmbedding>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmbedding(embedding: FaceEmbedding): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEmbeddings(embeddings: List<FaceEmbedding>)
}

@Dao
interface ClassSessionDao {
    @Query("SELECT * FROM class_sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<ClassSession>>

    @Query("SELECT * FROM class_sessions WHERE isActive = 1 LIMIT 1")
    fun getActiveSession(): Flow<ClassSession?>

    @Query("SELECT * FROM class_sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): ClassSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ClassSession)

    @Query("UPDATE class_sessions SET isActive = 0")
    suspend fun deactivateAllSessions()

    @Query("UPDATE class_sessions SET isActive = 1 WHERE sessionId = :sessionId")
    suspend fun setActiveSession(sessionId: String)
}

@Dao
interface AttendanceLogDao {
    @Query("SELECT * FROM attendance_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AttendanceLog>>

    @Query("SELECT * FROM attendance_logs WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getLogsForSession(sessionId: String): Flow<List<AttendanceLog>>

    @Query("SELECT * FROM attendance_logs WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getLogsFromTime(startTime: Long): Flow<List<AttendanceLog>>

    @Query("""
        SELECT * FROM attendance_logs 
        WHERE studentId = :studentId AND sessionId = :sessionId 
        ORDER BY timestamp DESC LIMIT 1
    """)
    suspend fun getMostRecentLog(studentId: String, sessionId: String): AttendanceLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AttendanceLog): Long

    @Query("UPDATE attendance_logs SET status = :newStatus WHERE id = :logId")
    suspend fun updateStatus(logId: Long, newStatus: String)

    @Query("DELETE FROM attendance_logs WHERE id = :logId")
    suspend fun deleteLog(logId: Long)

    @Query("DELETE FROM attendance_logs")
    suspend fun clearAllLogs()
}
