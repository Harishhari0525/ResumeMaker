package com.example.resumemaker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.resumemaker.model.Education
import com.example.resumemaker.model.Project
import com.example.resumemaker.model.WorkHistory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditResumeScreen(
    viewModel: ResumeViewModel,
    onNavigateBack: () -> Unit
) {
    val originalData = remember { viewModel.getCurrentResumeData() }

    if (originalData == null) {
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }

    // Hold the entire state of the resume being edited
    var resumeState by remember { mutableStateOf(originalData) }

    // Skills are easier to edit as a single block of text separated by newlines
    var skillsText by remember { mutableStateOf(originalData.skills.joinToString("\n")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Full Editor") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Apply the skills text back into the list before saving
                        val finalResume = resumeState.copy(
                            skills = skillsText.split("\n").filter { it.isNotBlank() }
                        )
                        viewModel.updateResumeData(finalResume)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Save, "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // --- 1. BASIC INFO ---
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Basic Details", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = resumeState.name,
                    onValueChange = { resumeState = resumeState.copy(name = it) },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = resumeState.contactInfo,
                    onValueChange = { resumeState = resumeState.copy(contactInfo = it) },
                    label = { Text("Contact Info (Phone | Link | Email)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = resumeState.summary,
                    onValueChange = { resumeState = resumeState.copy(summary = it) },
                    label = { Text("Professional Summary") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
                OutlinedTextField(
                    value = skillsText,
                    onValueChange = { skillsText = it },
                    label = { Text("Skills (One per line)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 6
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // --- 2. EXPERIENCE ---
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Experience", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    TextButton(onClick = {
                        val newJob = WorkHistory("", "", "", "", emptyList())
                        resumeState = resumeState.copy(experience = resumeState.experience + newJob)
                    }) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Job")
                    }
                }

                resumeState.experience.forEachIndexed { index, job ->
                    JobEditorCard(
                        job = job,
                        onUpdate = { updatedJob ->
                            val newList = resumeState.experience.toMutableList().apply { this[index] = updatedJob }
                            resumeState = resumeState.copy(experience = newList)
                        },
                        onDelete = {
                            val newList = resumeState.experience.toMutableList().apply { removeAt(index) }
                            resumeState = resumeState.copy(experience = newList)
                        }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // --- 3. PROJECTS ---
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Projects", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    TextButton(onClick = {
                        val newProject = Project("", "", emptyList())
                        resumeState = resumeState.copy(projects = resumeState.projects + newProject)
                    }) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Project")
                    }
                }

                resumeState.projects.forEachIndexed { index, proj ->
                    ProjectEditorCard(
                        project = proj,
                        onUpdate = { updatedProj ->
                            val newList = resumeState.projects.toMutableList().apply { this[index] = updatedProj }
                            resumeState = resumeState.copy(projects = newList)
                        },
                        onDelete = {
                            val newList = resumeState.projects.toMutableList().apply { removeAt(index) }
                            resumeState = resumeState.copy(projects = newList)
                        }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // --- 4. EDUCATION ---
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Education", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    TextButton(onClick = {
                        val newEdu = Education("", "", "")
                        resumeState = resumeState.copy(education = resumeState.education + newEdu)
                    }) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Education")
                    }
                }

                resumeState.education.forEachIndexed { index, edu ->
                    EducationEditorCard(
                        education = edu,
                        onUpdate = { updatedEdu ->
                            val newList = resumeState.education.toMutableList().apply { this[index] = updatedEdu }
                            resumeState = resumeState.copy(education = newList)
                        },
                        onDelete = {
                            val newList = resumeState.education.toMutableList().apply { removeAt(index) }
                            resumeState = resumeState.copy(education = newList)
                        }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// --- SUB-COMPONENTS FOR CLEAN UI ---

@Composable
fun JobEditorCard(job: WorkHistory, onUpdate: (WorkHistory) -> Unit, onDelete: () -> Unit) {
    var bulletsText by remember(job.bulletPoints) { mutableStateOf(job.bulletPoints.joinToString("\n")) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Job Details", fontWeight = FontWeight.Bold)
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
            OutlinedTextField(value = job.company, onValueChange = { onUpdate(job.copy(company = it)) }, label = { Text("Company") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = job.role, onValueChange = { onUpdate(job.copy(role = it)) }, label = { Text("Job Title") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = job.duration, onValueChange = { onUpdate(job.copy(duration = it)) }, label = { Text("Dates") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = job.location, onValueChange = { onUpdate(job.copy(location = it)) }, label = { Text("Location") }, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(
                value = bulletsText,
                onValueChange = {
                    bulletsText = it
                    onUpdate(job.copy(bulletPoints = it.split("\n").filter { line -> line.isNotBlank() }))
                },
                label = { Text("Bullet Points (One per line)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )
        }
    }
}

@Composable
fun ProjectEditorCard(project: Project, onUpdate: (Project) -> Unit, onDelete: () -> Unit) {
    var bulletsText by remember(project.bulletPoints) { mutableStateOf(project.bulletPoints.joinToString("\n")) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Project Details", fontWeight = FontWeight.Bold)
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
            OutlinedTextField(value = project.title, onValueChange = { onUpdate(project.copy(title = it)) }, label = { Text("Project Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = project.technologies, onValueChange = { onUpdate(project.copy(technologies = it)) }, label = { Text("Technologies Used") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = bulletsText,
                onValueChange = {
                    bulletsText = it
                    onUpdate(project.copy(bulletPoints = it.split("\n").filter { line -> line.isNotBlank() }))
                },
                label = { Text("Bullet Points (One per line)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
    }
}

@Composable
fun EducationEditorCard(education: Education, onUpdate: (Education) -> Unit, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Education Details", fontWeight = FontWeight.Bold)
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
            OutlinedTextField(value = education.school, onValueChange = { onUpdate(education.copy(school = it)) }, label = { Text("School / University") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = education.degree, onValueChange = { onUpdate(education.copy(degree = it)) }, label = { Text("Degree") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = education.year, onValueChange = { onUpdate(education.copy(year = it)) }, label = { Text("Graduation Year") }, modifier = Modifier.fillMaxWidth())
        }
    }
}