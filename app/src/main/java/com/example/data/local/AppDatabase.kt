package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.models.AttendanceLog
import com.example.data.models.ClassSession
import com.example.data.models.FaceEmbedding
import com.example.data.models.Student

@Database(
    entities = [
        Student::class,
        FaceEmbedding::class,
        ClassSession::class,
        AttendanceLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun faceEmbeddingDao(): FaceEmbeddingDao
    abstract fun classSessionDao(): ClassSessionDao
    abstract fun attendanceLogDao(): AttendanceLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "student_attendance_db"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
