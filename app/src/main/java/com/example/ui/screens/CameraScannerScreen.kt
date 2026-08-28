package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.content.ContextCompat
import com.example.data.vision.LivenessStatus
import com.example.ui.components.BoundingBoxOverlay
import com.example.ui.components.CameraPreviewView
import com.example.ui.theme.CameraViewportBackground
import com.example.ui.theme.MinimalistBackground
import com.example.ui.theme.MinimalistBlueContainer
import com.example.ui.theme.MinimalistBlueLight
import com.example.ui.theme.MinimalistBlueText
import com.example.ui.theme.MinimalistBorder
import com.example.ui.theme.MinimalistPurpleContainer
import com.example.ui.theme.MinimalistPurpleLight
import com.example.ui.theme.MinimalistPurplePrimary
import com.example.ui.theme.MinimalistPurpleText
import com.example.ui.theme.MinimalistStatusDanger
import com.example.ui.theme.MinimalistStatusPresentBg
import com.example.ui.theme.MinimalistStatusPresentText
import com.example.ui.theme.MinimalistStatusSuccess
import com.example.ui.theme.MinimalistStatusWarning
import com.example.ui.theme.MinimalistSurface
import com.example.ui.theme.MinimalistTextMuted
import com.example.ui.theme.MinimalistTextPrimary
import com.example.ui.theme.MinimalistTextSecondary
import com.example.viewmodel.AttendanceViewModel

@Composable
fun CameraScannerScreen(
    viewModel: AttendanceViewModel,
    onNavigateToRegistration: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val activeSession by viewModel.activeSession.collectAsState()
    val scanState by viewModel.scanState.collectAsState()
    var useFrontCamera by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MinimalistBackground)
            .padding(16.dp)
    ) {
        // Clean Minimalism Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SYSTEM ACTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MinimalistTextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = activeSession?.courseName ?: "Recognition Room 102",
                    style = MaterialTheme.typography.titleMedium,
                    color = MinimalistTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Switch Camera Button
                IconButton(
                    onClick = { useFrontCamera = !useFrontCamera },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MinimalistSurface)
                        .border(1.dp, MinimalistBorder, CircleShape)
                        .testTag("flip_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = MinimalistPurplePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Avatar pill
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MinimalistPurpleContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AD",
                        style = MaterialTheme.typography.labelMedium,
                        color = MinimalistPurpleText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Camera Frame Viewport Container (Dark Rounded Card with 28.dp corners)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(28.dp))
                .background(CameraViewportBackground)
                .border(2.dp, MinimalistSurface, RoundedCornerShape(28.dp))
        ) {
            if (hasCameraPermission) {
                CameraPreviewView(
                    modifier = Modifier.fillMaxSize(),
                    useFrontCamera = useFrontCamera,
                    onFaceDetected = { face, bitmap ->
                        viewModel.processCameraFrame(face, bitmap)
                    },
                    onNoFace = {
                        viewModel.onNoFaceDetected()
                    }
                )

                BoundingBoxOverlay(
                    modifier = Modifier.fillMaxSize(),
                    boundingBox = scanState.faceBoundingBox,
                    isMatched = scanState.matchedStudent != null,
                    isCooldown = scanState.isCooldownActive
                )

                // Top Floating Pill: Live Badge & Session Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Environment Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Main Entrance • ${activeSession?.classId ?: "CS-2026"}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Live Status Indicator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MinimalistStatusSuccess)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE • 30 FPS",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // Permission Request State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Camera Permission",
                        tint = MinimalistPurplePrimary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Camera Permission Needed",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Grant camera access to perform live face recognition attendance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = MinimalistPurplePrimary),
                        modifier = Modifier.testTag("grant_camera_button")
                    ) {
                        Text("Grant Permission", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Liveness Anti-Spoofing Status Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val (livenessText, livenessColor, livenessIcon) = when (scanState.livenessStatus) {
                LivenessStatus.VERIFIED_LIVE -> Triple("LIVE VERIFIED (Anti-Spoofing Pass)", MinimalistStatusSuccess, Icons.Default.VerifiedUser)
                LivenessStatus.BLINK_DETECTED -> Triple("BLINK DETECTED - Validating...", MinimalistPurplePrimary, Icons.Default.CheckCircle)
                LivenessStatus.WAITING_FOR_BLINK -> Triple("LOOK AT CAMERA & BLINK", MinimalistStatusWarning, Icons.Default.Warning)
                LivenessStatus.SPOOF_SUSPECTED -> Triple("SPOOF PREVENTED (Photo Blocked)", MinimalistStatusDanger, Icons.Default.Security)
                else -> Triple("SEARCHING FOR FACE...", MinimalistTextMuted, Icons.Default.Security)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MinimalistSurface)
                    .border(1.dp, MinimalistBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = livenessIcon,
                        contentDescription = null,
                        tint = livenessColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = livenessText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MinimalistTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Clean Minimalism Bottom Student Match Card
        AnimatedVisibility(
            visible = scanState.faceDetected,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalistSurface),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MinimalistBorder, RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    if (scanState.matchedStudent != null) {
                        val student = scanState.matchedStudent!!
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Student Avatar Box
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MinimalistPurpleLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MinimalistPurpleText,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = student.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MinimalistTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // Match percentage badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MinimalistBlueLight)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${(scanState.confidence * 100).toInt()}% MATCH",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MinimalistBlueText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "ID: ${student.studentId} • Roll: ${student.rollNo}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MinimalistTextSecondary
                                )
                            }

                            // Present Status Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (scanState.isCooldownActive) MinimalistStatusWarning.copy(alpha = 0.15f)
                                        else MinimalistStatusPresentBg
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (scanState.isCooldownActive) "LOGGED" else "PRESENT",
                                    color = if (scanState.isCooldownActive) MinimalistStatusWarning else MinimalistStatusPresentText,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        if (scanState.isCooldownActive) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LockClock,
                                    contentDescription = null,
                                    tint = MinimalistStatusWarning,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Duplicate log prevented. Cooldown: ${scanState.remainingCooldownSeconds}s",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MinimalistTextSecondary
                                )
                            }
                        }
                    } else {
                        // Unrecognized Face Alert
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MinimalistStatusWarning,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Face Detected - Unregistered",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MinimalistTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "No matching student embedding found. Register student first.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MinimalistTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

