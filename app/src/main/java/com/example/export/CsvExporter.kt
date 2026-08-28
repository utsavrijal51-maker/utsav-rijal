package com.example.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.models.AttendanceLog
import com.example.data.models.Student
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    fun generateCsvReport(
        context: Context,
        logs: List<AttendanceLog>,
        studentMap: Map<String, Student>
    ): File {
        val fileName = "Attendance_Report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
        val file = File(context.cacheDir, fileName)

        file.printWriter().use { out ->
            out.println("Log ID,Student ID,Student Name,Roll No,Class ID,Session ID,Status,Confidence Score,Liveness Verified,Date,Time")
            for (log in logs) {
                val student = studentMap[log.studentId]
                val name = student?.name ?: "Unknown"
                val rollNo = student?.rollNo ?: "N/A"
                val classId = student?.classId ?: "N/A"
                val confidence = String.format(Locale.US, "%.2f%%", log.confidenceScore * 100)
                
                out.println(
                    "${log.id}," +
                    "\"${log.studentId}\"," +
                    "\"$name\"," +
                    "\"$rollNo\"," +
                    "\"$classId\"," +
                    "\"${log.sessionId}\"," +
                    "\"${log.status}\"," +
                    "\"$confidence\"," +
                    "${log.livenessVerified}," +
                    "\"${log.formattedDate()}\"," +
                    "\"${log.formattedTime()}\""
                )
            }
        }
        return file
    }

    fun shareCsvFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Student Attendance Report")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Share Attendance CSV Report")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
