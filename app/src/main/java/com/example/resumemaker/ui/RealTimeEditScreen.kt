package com.example.resumemaker.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.resumemaker.util.HtmlEngine
import kotlinx.coroutines.delay

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

    // Live Preview State
    var htmlContent by remember { mutableStateOf("") }
    val currentStyle by viewModel.currentStyle.collectAsState()

    // DEBOUNCE LOGIC: Update HTML only when typing stops for 800ms
    LaunchedEffect(name, summary, skillsText) {
        delay(800)
        val updatedResume = originalData.copy(
            name = name,
            summary = summary,
            skills = skillsText.split("\n").filter { it.isNotBlank() }
        )
        // Generate HTML locally for preview
        htmlContent = HtmlEngine.generateHtml(updatedResume, currentStyle)
    }

    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
        topBar = {
            TopAppBar(
                title = { Text("Live Editor") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                                 },
                windowInsets = WindowInsets(top = 0.dp, bottom = 0.dp),
                actions = {
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
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {

            // 1. EDITOR AREA (Top 50%)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Edit Info", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("Summary") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                OutlinedTextField(value = skillsText, onValueChange = { skillsText = it }, label = { Text("Skills (New line per skill)") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
            }

            HorizontalDivider(thickness = 4.dp, color = MaterialTheme.colorScheme.primaryContainer)

            // 2. LIVE PREVIEW AREA (Bottom 50%)
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
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
                        }
                    )
                } else {
                    CircularProgressIndicator(Modifier.align(androidx.compose.ui.Alignment.Center))
                }

                // Overlay Label
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd).padding(8.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Live Preview", color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}