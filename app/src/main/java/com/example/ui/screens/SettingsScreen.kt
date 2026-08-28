package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MinimalistBackground
import com.example.ui.theme.MinimalistBlueContainer
import com.example.ui.theme.MinimalistBlueText
import com.example.ui.theme.MinimalistBorder
import com.example.ui.theme.MinimalistPurpleContainer
import com.example.ui.theme.MinimalistPurplePrimary
import com.example.ui.theme.MinimalistPurpleText
import com.example.ui.theme.MinimalistStatusDanger
import com.example.ui.theme.MinimalistSurface
import com.example.ui.theme.MinimalistTextMuted
import com.example.ui.theme.MinimalistTextPrimary
import com.example.ui.theme.MinimalistTextSecondary
import com.example.viewmodel.AttendanceViewModel

@Composable
fun SettingsScreen(
    viewModel: AttendanceViewModel
) {
    var serverUrl by remember { mutableStateOf("http://10.0.2.2:8000") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MinimalistBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MinimalistPurpleContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MinimalistPurpleText,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "System Settings & Sync",
                        style = MaterialTheme.typography.titleLarge,
                        color = MinimalistTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Server endpoint connection & database controls",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalistTextSecondary
                    )
                }
            }
        }

        item {
            // FastAPI Backend Server Sync Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalistSurface),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MinimalistBorder, RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = MinimalistPurplePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FastAPI Backend Server Sync",
                            style = MaterialTheme.typography.titleMedium,
                            color = MinimalistPurplePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text("FastAPI Server Base URL") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MinimalistPurplePrimary,
                            unfocusedBorderColor = MinimalistBorder,
                            focusedLabelColor = MinimalistPurplePrimary,
                            unfocusedLabelColor = MinimalistTextMuted,
                            focusedTextColor = MinimalistTextPrimary,
                            unfocusedTextColor = MinimalistTextPrimary,
                            focusedContainerColor = MinimalistSurface,
                            unfocusedContainerColor = MinimalistSurface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("server_url_input")
                    )

                    Text(
                        text = "Supports standalone offline mode or live sync with the included Python FastAPI backend.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalistTextMuted
                    )
                }
            }
        }

        item {
            // Reset / Seed Data Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalistSurface),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MinimalistBorder, RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Database Management",
                        style = MaterialTheme.typography.titleMedium,
                        color = MinimalistTextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = {
                            viewModel.seedDemoData()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MinimalistBlueContainer),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("seed_data_button")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, tint = MinimalistBlueText)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Seed Sample Demo Students & Session", color = MinimalistBlueText, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.clearAllLogs()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MinimalistStatusDanger.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clear_logs_button")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MinimalistStatusDanger)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear All Attendance Logs", color = MinimalistStatusDanger, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            // System Architecture Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalistSurface),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MinimalistBorder, RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MinimalistPurplePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Computer Vision Architecture",
                            style = MaterialTheme.typography.titleMedium,
                            color = MinimalistTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text("• ML Kit Face Detection: Real-time 60fps bounding box & facial landmark tracking.", style = MaterialTheme.typography.bodySmall, color = MinimalistTextSecondary)
                    Text("• 128-d Embedding Extraction: Normalized facial feature vectors computed on-device.", style = MaterialTheme.typography.bodySmall, color = MinimalistTextSecondary)
                    Text("• Anti-Spoofing Liveness: Eye Aspect Ratio (EAR) blink detection & organic face motion check.", style = MaterialTheme.typography.bodySmall, color = MinimalistTextSecondary)
                    Text("• Session Cooldown Logic: 30-minute default cooldown prevents duplicate check-ins.", style = MaterialTheme.typography.bodySmall, color = MinimalistTextSecondary)
                    Text("• Local Room Database: Offline-first persistence for Students, Embeddings, Sessions & Logs.", style = MaterialTheme.typography.bodySmall, color = MinimalistTextSecondary)
                }
            }
        }
    }
}
