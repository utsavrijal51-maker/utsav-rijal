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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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
import com.example.ui.theme.MinimalistStatusSuccess
import com.example.ui.theme.MinimalistSurface
import com.example.ui.theme.MinimalistTextMuted
import com.example.ui.theme.MinimalistTextPrimary
import com.example.ui.theme.MinimalistTextSecondary
import com.example.viewmodel.AttendanceViewModel

@Composable
fun SessionsScreen(
    viewModel: AttendanceViewModel
) {
    val activeSession by viewModel.activeSession.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()

    var courseName by remember { mutableStateOf("Computer Vision & ML") }
    var classId by remember { mutableStateOf("CS-2026") }
    var cooldownMinutes by remember { mutableStateOf(30f) }
    var distanceThreshold by remember { mutableStateOf(0.55f) }

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
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MinimalistPurpleText,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Session & Recognition Config",
                        style = MaterialTheme.typography.titleLarge,
                        color = MinimalistTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Set active course, distance threshold & cooldown period",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalistTextSecondary
                    )
                }
            }
        }

        item {
            // New Session Configuration Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalistSurface),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MinimalistBorder, RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Activate Class Session",
                        style = MaterialTheme.typography.titleMedium,
                        color = MinimalistPurplePrimary,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = courseName,
                        onValueChange = { courseName = it },
                        label = { Text("Course Name") },
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
                            .testTag("course_name_input")
                    )

                    OutlinedTextField(
                        value = classId,
                        onValueChange = { classId = it },
                        label = { Text("Class / Department ID") },
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
                            .testTag("session_class_id_input")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Distance Threshold Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Euclidean Distance Threshold",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MinimalistTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = String.format("%.2f", distanceThreshold),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MinimalistPurplePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Lower values = stricter matching (< 0.55 recommended)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MinimalistTextMuted
                        )
                        Slider(
                            value = distanceThreshold,
                            onValueChange = { distanceThreshold = it },
                            valueRange = 0.35f..0.75f,
                            colors = SliderDefaults.colors(
                                thumbColor = MinimalistPurplePrimary,
                                activeTrackColor = MinimalistPurplePrimary,
                                inactiveTrackColor = MinimalistBorder
                            ),
                            modifier = Modifier.testTag("threshold_slider")
                        )
                    }

                    // Cooldown Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Check-in Cooldown Period",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MinimalistTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${cooldownMinutes.toInt()} mins",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MinimalistPurplePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Prevents duplicate check-ins within same class session",
                            style = MaterialTheme.typography.bodySmall,
                            color = MinimalistTextMuted
                        )
                        Slider(
                            value = cooldownMinutes,
                            onValueChange = { cooldownMinutes = it },
                            valueRange = 5f..60f,
                            steps = 11,
                            colors = SliderDefaults.colors(
                                thumbColor = MinimalistPurplePrimary,
                                activeTrackColor = MinimalistPurplePrimary,
                                inactiveTrackColor = MinimalistBorder
                            ),
                            modifier = Modifier.testTag("cooldown_slider")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.createSession(
                                courseName = courseName,
                                classId = classId,
                                cooldownMins = cooldownMinutes.toInt(),
                                threshold = distanceThreshold
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MinimalistPurplePrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("activate_session_button")
                    ) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LAUNCH ACTIVE SESSION",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Class Sessions History",
                style = MaterialTheme.typography.titleMedium,
                color = MinimalistTextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(allSessions) { session ->
            val isActive = activeSession?.sessionId == session.sessionId

            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalistSurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isActive) MinimalistStatusSuccess else MinimalistBorder,
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = session.courseName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MinimalistTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            if (isActive) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "• ACTIVE",
                                    color = MinimalistStatusSuccess,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                        Text(
                            text = "Session ID: ${session.sessionId} • Class: ${session.classId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MinimalistTextSecondary
                        )
                        Text(
                            text = "Cooldown: ${session.cooldownMinutes}m • Threshold: ${session.distanceThreshold}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MinimalistTextMuted
                        )
                    }

                    if (!isActive) {
                        Button(
                            onClick = {
                                viewModel.setActiveSession(session.sessionId)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MinimalistBlueContainer),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("switch_session_${session.sessionId}")
                        ) {
                            Text("Activate", color = MinimalistBlueText, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
