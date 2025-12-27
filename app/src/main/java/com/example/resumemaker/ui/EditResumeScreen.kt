package com.example.resumemaker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.resumemaker.model.TailoredResume

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditResumeScreen(
    viewModel: ResumeViewModel,
    onNavigateBack: () -> Unit
) {
    val originalData = remember { viewModel.getCurrentResumeData() }

    // If no data, go back immediately
    if (originalData == null) {
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }

    // Mutable States for fields
    var name by remember { mutableStateOf(originalData.name) }
    var contact by remember { mutableStateOf(originalData.contactInfo) }
    var summary by remember { mutableStateOf(originalData.summary) }

    // Skills are a list, we join them into a string for editing
    var skillsText by remember { mutableStateOf(originalData.skills.joinToString("\n")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Details") },
                actions = {
                    IconButton(onClick = {
                        // Reconstruct the object
                        val updatedResume = originalData.copy(
                            name = name,
                            contactInfo = contact,
                            summary = summary,
                            skills = skillsText.split("\n").filter { it.isNotBlank() }
                        )
                        // Save and Regenerate
                        viewModel.updateResumeData(updatedResume)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Save, "Save Changes")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = contact,
                onValueChange = { contact = it },
                label = { Text("Contact Info (Phone | Link | Email)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = summary,
                onValueChange = { summary = it },
                label = { Text("Professional Summary") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5
            )

            OutlinedTextField(
                value = skillsText,
                onValueChange = { skillsText = it },
                label = { Text("Skills (One per line)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 8
            )

            Text(
                "To edit Experience or Education, please modify the original PDF or Job Description and regenerate.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}