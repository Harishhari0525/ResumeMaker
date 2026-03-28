package com.example.resumemaker.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.resumemaker.util.HtmlEngine

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealTimeEditScreen(
    viewModel: ResumeViewModel,
    onNavigateBack: () -> Unit
) {
    val originalData = remember { viewModel.getCurrentResumeData() }
    if (originalData == null) {
        onNavigateBack()
        return
    }

    // Editable States
    var name by remember { mutableStateOf(originalData.name) }
    var summary by remember { mutableStateOf(originalData.summary) }
    var skillsText by remember { mutableStateOf(originalData.skills.joinToString("\n")) }

    // Toggle States
    var isPreviewMode by remember { mutableStateOf(false) }
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    var isDarkMode by remember { mutableStateOf(isSystemDark) } // Default to system theme

    // Live Preview State
    var htmlContent by remember { mutableStateOf("") }
    val currentStyle by viewModel.currentStyle.collectAsState()

    // Generate HTML when typing stops or dark mode toggles
    LaunchedEffect(name, summary, skillsText, isDarkMode) {
        kotlinx.coroutines.delay(800)
        val updatedResume = originalData.copy(
            name = name,
            summary = summary,
            skills = skillsText.split("\n").filter { it.isNotBlank() }
        )
        // Pass the Dark Mode flag to the engine
        htmlContent = HtmlEngine.generateHtml(updatedResume, currentStyle, isDarkMode)
    }

    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
        topBar = {
            TopAppBar(
                title = { Text(if (isPreviewMode) "Preview" else "Live Editor") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                windowInsets = WindowInsets(top = 0.dp, bottom = 0.dp),
                actions = {

                    if (isPreviewMode) {
                        IconButton(onClick = { isDarkMode = !isDarkMode }) {
                            Icon(
                                if (isDarkMode) Icons.Default.Visibility else Icons.Default.Visibility,
                                contentDescription = "Toggle Dark Mode"
                            )
                        }
                    }
                    // Save Button
                    IconButton(onClick = {
                        val finalResume = originalData.copy(
                            name = name,
                            summary = summary,
                            skills = skillsText.split("\n").filter { it.isNotBlank() }
                        )
                        viewModel.updateResumeData(finalResume)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Save, "Save")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { isPreviewMode = !isPreviewMode },
                icon = {
                    Icon(
                        if (isPreviewMode) Icons.Default.Edit else Icons.Default.Visibility,
                        contentDescription = "Toggle View"
                    )
                },
                text = { Text(if (isPreviewMode) "Back to Editor" else "Show Preview") }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {

            if (!isPreviewMode) {
                // 1. FULL SCREEN EDITOR
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Edit Info", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("Summary") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    OutlinedTextField(value = skillsText, onValueChange = { skillsText = it }, label = { Text("Skills (New line per skill)") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
                    Spacer(Modifier.height(80.dp)) // Padding for the FAB
                }
            } else {
                // 2. FULL SCREEN PREVIEW
                if (htmlContent.isNotEmpty()) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                settings.builtInZoomControls = true
                                settings.displayZoomControls = false
                                webViewClient = WebViewClient()
                            }
                        },
                        update = { webView ->
                            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
        }
    }
}