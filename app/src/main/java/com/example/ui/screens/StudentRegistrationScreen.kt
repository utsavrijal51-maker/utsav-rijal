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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonPin
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
import com.example.ui.theme.MinimalistStatusDanger
import com.example.ui.theme.MinimalistStatusSuccess
import com.example.ui.theme.MinimalistSurface
import com.example.ui.theme.MinimalistSurfaceVariant
import com.example.ui.theme.MinimalistTextMuted
import com.example.ui.theme.MinimalistTextPrimary
import com.example.ui.theme.MinimalistTextSecondary
import com.example.viewmodel.AttendanceViewModel

@Composable
fun StudentRegistrationScreen(
    viewModel: AttendanceViewModel
) {
    var studentId by remember { mutableStateOf("STU-${(104..999).random()}") }
    var name by remember { mutableStateOf("") }
    var rollNo by remember { mutableStateOf("") }
    var classId by remember { mutableStateOf("CS-2026") }

    val allStudents by viewModel.allStudents.collectAsState()
    val registrationEmbeddings by viewModel.registrationEmbeddings.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MinimalistBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
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
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = MinimalistPurpleText,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Student Registration Module",
                        style = MaterialTheme.typography.titleLarge,
                        color = MinimalistTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Capture 128-d face embeddings and map profile to directory",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalistTextSecondary
                    )
                }
            }
        }

        item {
            // Registration Form Card
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
                        text = "1. Student Profile Details",
                        style = MaterialTheme.typography.titleMedium,
                        color = MinimalistPurplePrimary,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = studentId,
                        onValueChange = { studentId = it },
                        label = { Text("Student ID") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = MinimalistPurplePrimary) },
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
                            .testTag("student_id_input")
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.PersonPin, contentDescription = null, tint = MinimalistPurplePrimary) },
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
                            .testTag("student_name_input")
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = rollNo,
                            onValueChange = { rollNo = it },
                            label = { Text("Roll No") },
                            leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null, tint = MinimalistPurplePrimary) },
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
                                .weight(1f)
                                .testTag("roll_no_input")
                        )

                        OutlinedTextField(
                            value = classId,
                            onValueChange = { classId = it },
                            label = { Text("Class ID") },
                            leadingIcon = { Icon(Icons.Default.Class, contentDescription = null, tint = MinimalistPurplePrimary) },
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
                                .weight(1f)
                                .testTag("class_id_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "2. Facial Embeddings Capture (3–5 Sample Vectors)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MinimalistPurplePrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (registrationEmbeddings.isNotEmpty()) MinimalistStatusSuccess else MinimalistTextMuted
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Captured: ${registrationEmbeddings.size} Vectors",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MinimalistTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                val sampleVector = FloatArray(128) { (it * 0.019f + studentId.hashCode() % 50 * 0.01f) % 0.8f }
                                com.example.data.vision.FaceEmbeddingExtractor.normalize(sampleVector)
                                viewModel.addSampleEmbeddingVector(sampleVector)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MinimalistBlueContainer),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_sample_button")
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = MinimalistBlueText)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sample +", color = MinimalistBlueText, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.submitStudentRegistration(
                                studentId = studentId,
                                name = name,
                                rollNo = rollNo,
                                classId = classId,
                                photoPath = null
                            ) {
                                studentId = "STU-${(104..999).random()}"
                                name = ""
                                rollNo = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MinimalistPurplePrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("register_student_submit_button")
                    ) {
                        Text(
                            text = "SAVE STUDENT PROFILE",
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
                text = "Registered Directory (${allStudents.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MinimalistTextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(allStudents) { student ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalistSurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MinimalistBorder, RoundedCornerShape(20.dp))
                    .testTag("student_item_${student.studentId}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MinimalistPurpleContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = student.name.take(1).uppercase(),
                                color = MinimalistPurpleText,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = student.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MinimalistTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ID: ${student.studentId} • Roll: ${student.rollNo} • Class: ${student.classId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MinimalistTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            viewModel.deleteStudent(student.studentId)
                        },
                        modifier = Modifier.testTag("delete_student_${student.studentId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Student",
                            tint = MinimalistStatusDanger.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
