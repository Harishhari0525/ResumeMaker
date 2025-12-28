package com.example.resumemaker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.resumemaker.data.local.JobApplication
import com.example.resumemaker.data.local.JobStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobTrackerScreen(
    viewModel: ResumeViewModel,
    onNavigateBack: () -> Unit
) {
    val jobs by viewModel.allJobs.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Application Tracker") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                windowInsets = WindowInsets(top = 0.dp, bottom = 0.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add Job")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(jobs) { job ->
                JobCard(job, viewModel)
            }
        }

        if (showAddDialog) {
            AddJobDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { company, role ->
                    viewModel.addJobApplication(company, role)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun JobCard(job: JobApplication, viewModel: ResumeViewModel) {
    var expanded by remember { mutableStateOf(false) }

    // Background colors for the card
    val cardBgColor = when(job.status) {
        JobStatus.APPLIED -> Color(0xFFE3F2FD) // Light Blue
        JobStatus.INTERVIEWING -> Color(0xFFFFF3E0) // Light Orange
        JobStatus.OFFER -> Color(0xFFE8F5E9) // Light Green
        JobStatus.REJECTED -> Color(0xFFFFEBEE) // Light Red
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor,
            contentColor = Color.Black // <--- FIX: FORCE BLACK TEXT ON LIGHT CARDS
        ),
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.companyName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = job.jobTitle,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Status Badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = Color.Black.copy(alpha = 0.1f) // Darker pill for contrast
                ) {
                    Text(
                        text = job.status.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (expanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.Black.copy(alpha = 0.1f)
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Status Change Buttons (Mini)
                    Row {
                        JobStatus.entries.forEach { status ->
                            if (status != job.status) {
                                TextButton(onClick = { viewModel.updateJobStatus(job, status) }) {
                                    Text(
                                        status.name.take(3),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Black.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    IconButton(onClick = { viewModel.deleteJob(job) }) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

@Composable
fun AddJobDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var company by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Track New Application") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Company Name") })
                OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Job Role") })
            }
        },
        confirmButton = {
            Button(onClick = { if(company.isNotBlank()) onAdd(company, role) }) { Text("Track") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}