package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AttendanceWithStudent
import com.example.export.CsvExporter
import com.example.ui.theme.MinimalistBackground
import com.example.ui.theme.MinimalistBlueContainer
import com.example.ui.theme.MinimalistBlueText
import com.example.ui.theme.MinimalistBorder
import com.example.ui.theme.MinimalistPurpleContainer
import com.example.ui.theme.MinimalistPurplePrimary
import com.example.ui.theme.MinimalistPurpleText
import com.example.ui.theme.MinimalistStatusDanger
import com.example.ui.theme.MinimalistStatusPresentBg
import com.example.ui.theme.MinimalistStatusPresentText
import com.example.ui.theme.MinimalistStatusSuccess
import com.example.ui.theme.MinimalistStatusWarning
import com.example.ui.theme.MinimalistSurface
import com.example.ui.theme.MinimalistSurfaceVariant
import com.example.ui.theme.MinimalistTextMuted
import com.example.ui.theme.MinimalistTextPrimary
import com.example.ui.theme.MinimalistTextSecondary
import com.example.viewmodel.AttendanceViewModel

@Composable
fun DashboardScreen(
    viewModel: AttendanceViewModel
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedStatusFilter.collectAsState()
    val filteredLogs by viewModel.filteredLogs.collectAsState()

    var selectedLogForOverride by remember { mutableStateOf<AttendanceWithStudent?>(null) }

    // Statistics metrics
    val totalCount = filteredLogs.size
    val presentCount = filteredLogs.count { it.log.status.equals("PRESENT", ignoreCase = true) }
    val lateCount = filteredLogs.count { it.log.status.equals("LATE", ignoreCase = true) }

    if (selectedLogForOverride != null) {
        val item = selectedLogForOverride!!
        AlertDialog(
            onDismissRequest = { selectedLogForOverride = null },
            title = {
                Text(
                    text = "Override Attendance Status",
                    color = MinimalistTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Student: ${item.student?.name ?: item.log.studentId}",
                        color = MinimalistTextSecondary
                    )
                    Text(
                        text = "Current Status: ${item.log.status}",
                        color = MinimalistTextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Select New Status:", color = MinimalistTextPrimary, fontWeight = FontWeight.SemiBold)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                viewModel.updateAttendanceStatus(item.log.id, "PRESENT")
                                selectedLogForOverride = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MinimalistStatusSuccess),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Present", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.updateAttendanceStatus(item.log.id, "LATE")
                                selectedLogForOverride = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MinimalistStatusWarning),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Late", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.updateAttendanceStatus(item.log.id, "ABSENT")
                                selectedLogForOverride = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MinimalistStatusDanger),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Absent", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedLogForOverride = null }) {
                    Text("Cancel", color = MinimalistTextMuted)
                }
            },
            containerColor = MinimalistSurface
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MinimalistBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header with CSV Export
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MinimalistPurpleContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = MinimalistPurpleText,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Attendance Reports",
                            style = MaterialTheme.typography.titleLarge,
                            color = MinimalistTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Real-time records, manual override & CSV export",
                            style = MaterialTheme.typography.bodySmall,
                            color = MinimalistTextSecondary
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.exportAttendanceCsv { file ->
                            CsvExporter.shareCsvFile(context, file)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MinimalistPurplePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("export_csv_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Export CSV",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CSV", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            // Summary Stats Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MinimalistSurface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, MinimalistBorder, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("TOTAL LOGS", style = MaterialTheme.typography.labelSmall, color = MinimalistTextMuted, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$totalCount",
                            style = MaterialTheme.typography.titleLarge,
                            color = MinimalistTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Present Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MinimalistSurface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, MinimalistBorder, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("PRESENT", style = MaterialTheme.typography.labelSmall, color = MinimalistStatusSuccess, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$presentCount",
                            style = MaterialTheme.typography.titleLarge,
                            color = MinimalistStatusSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Late Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MinimalistSurface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, MinimalistBorder, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("LATE", style = MaterialTheme.typography.labelSmall, color = MinimalistStatusWarning, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$lateCount",
                            style = MaterialTheme.typography.titleLarge,
                            color = MinimalistStatusWarning,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            // Search and Status Filters
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search student name, ID, or roll number...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MinimalistPurplePrimary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MinimalistPurplePrimary,
                        unfocusedBorderColor = MinimalistBorder,
                        focusedTextColor = MinimalistTextPrimary,
                        unfocusedTextColor = MinimalistTextPrimary,
                        focusedPlaceholderColor = MinimalistTextMuted,
                        unfocusedPlaceholderColor = MinimalistTextMuted,
                        focusedContainerColor = MinimalistSurface,
                        unfocusedContainerColor = MinimalistSurface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("attendance_search_input")
                )

                // Filter Pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("ALL", "PRESENT", "LATE", "ABSENT").forEach { filterTag ->
                        val isSelected = selectedFilter == filterTag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) MinimalistPurplePrimary else MinimalistSurface)
                                .border(1.dp, if (isSelected) MinimalistPurplePrimary else MinimalistBorder, RoundedCornerShape(20.dp))
                                .clickable { viewModel.setStatusFilter(filterTag) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("filter_pill_$filterTag")
                        ) {
                            Text(
                                text = filterTag,
                                color = if (isSelected) Color.White else MinimalistTextSecondary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }

        items(filteredLogs) { item ->
            val log = item.log
            val student = item.student

            val (statusColor, statusBg) = when (log.status.uppercase()) {
                "PRESENT" -> Pair(MinimalistStatusPresentText, MinimalistStatusPresentBg)
                "LATE" -> Pair(MinimalistStatusWarning, MinimalistStatusWarning.copy(alpha = 0.15f))
                "ABSENT" -> Pair(MinimalistStatusDanger, MinimalistStatusDanger.copy(alpha = 0.15f))
                else -> Pair(MinimalistBlueText, MinimalistBlueContainer)
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalistSurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MinimalistBorder, RoundedCornerShape(20.dp))
                    .testTag("log_item_${log.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(statusBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (student?.name?.take(1) ?: log.studentId.take(1)).uppercase(),
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = student?.name ?: "Student ID: ${log.studentId}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MinimalistTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Roll: ${student?.rollNo ?: "N/A"} • Class: ${student?.classId ?: "N/A"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MinimalistTextSecondary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${log.formattedDate()} at ${log.formattedTime()} • Conf: ${(log.confidenceScore * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MinimalistTextMuted
                                )
                                if (log.livenessVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Liveness Verified",
                                        tint = MinimalistStatusSuccess,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(statusBg)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = log.status.uppercase(),
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        IconButton(
                            onClick = { selectedLogForOverride = item },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("override_log_${log.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Override Status",
                                tint = MinimalistPurplePrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
