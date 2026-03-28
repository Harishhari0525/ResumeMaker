package com.example.resumemaker.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.example.resumemaker.model.TailoredResume
import com.example.resumemaker.util.ResumeStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ResumeViewModel,
    navController: androidx.navigation.NavController
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val extractedText by viewModel.extractedText.collectAsState()
    val jdText by viewModel.jdText.collectAsState()
    val currentStyle by viewModel.currentStyle.collectAsState()
    val generatedHtml by viewModel.generatedHtml.collectAsState()
    val analysisResult by viewModel.analysisResult.collectAsState()

    // --- LAUNCHERS ---
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.extractPdf(it) }
    }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.extractJdFromImage(it) }
    }

    // --- UI STATES ---
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var styleToPreview by remember { mutableStateOf<ResumeStyle?>(null) } // NEW: Tracks which style is being previewed

    val tabs = listOf("Resume PDF", "Cover Letter", "ATS Score")

    fun launchEmailIntent(context: Context, resumeName: String, jobTitle: String, coverLetter: String) {
        val subject = "Application for $jobTitle - $resumeName"
        val body = coverLetter.ifBlank {
            "Dear Hiring Manager,\n\nPlease find attached my resume for the $jobTitle position.\n\nBest regards,\n$resumeName"
        }
        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
            putExtra(android.content.Intent.EXTRA_TEXT, body)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "No email app configured", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AI Resume Maker", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
                windowInsets = WindowInsets(top = 0.dp, bottom = 0.dp),
                actions = {
                    IconButton(onClick = { navController.navigate(com.example.resumemaker.HistoryRoute) }) { Icon(Icons.Default.History, "History") }
                    IconButton(onClick = { navController.navigate(com.example.resumemaker.JobTrackerRoute) }) { Icon(Icons.Default.Work, "Jobs") }
                    IconButton(onClick = { navController.navigate(com.example.resumemaker.SettingsRoute) }) { Icon(Icons.Default.Settings, "Settings") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- 1. UPLOAD AREA ---
            Surface(
                onClick = { pdfLauncher.launch("application/pdf") },
                shape = ShapeDefaults.Medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().height(100.dp)
            ) {
                Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    Text(if (extractedText.isNotBlank()) "✅ PDF Loaded" else "Tap to Upload Resume (PDF)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }

            if (extractedText.isNotBlank()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = { showSheet = true }) {
                        Icon(Icons.Default.Description, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Check Extracted Text")
                    }
                }
            }

            // --- 2. JD INPUT ---
            OutlinedTextField(
                value = jdText,
                onValueChange = { viewModel.updateJdText(it) },
                label = { Text("Job Description") },
                placeholder = { Text("Paste text or scan image...") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 200.dp),
                shape = ShapeDefaults.Medium,
                trailingIcon = {
                    IconButton(onClick = { photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        Icon(Icons.Default.CameraAlt, "Scan Image", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // --- 3. ACTION AREA (Conditional) ---
            if (generatedHtml != null) {
                // === STATE: SUCCESS ===

                // NEW: Template Selector moved here!
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Text(
                        "Select Template",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        val styles = listOf(
                            Triple(ResumeStyle.MODERN, "Modern", Color(0xFF2196F3)),
                            Triple(ResumeStyle.TECH_MINIMAL, "Tech", Color(0xFF212121)),
                            Triple(ResumeStyle.EXECUTIVE, "Executive", Color(0xFF000000)),
                            Triple(ResumeStyle.CLASSIC, "Classic", Color(0xFF795548)),
                            Triple(ResumeStyle.CREATIVE, "Creative", Color(0xFF9C27B0)),
                            Triple(ResumeStyle.COMPACT, "Compact", Color(0xFF607D8B))
                        )

                        items(styles.size) { index ->
                            val (style, label, chipColor) = styles[index]
                            TemplatePreviewCard(
                                label = label,
                                themeColor = chipColor,
                                isSelected = currentStyle == style,
                                style = style,
                                onClick = { viewModel.setStyle(style) },
                                onPreview = { styleToPreview = style } // Triggers the preview dialog
                            )
                        }
                    }
                }

                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title, maxLines = 1, style = MaterialTheme.typography.bodySmall) })
                    }
                }

                Spacer(Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> { // TAB: RESUME
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { navController.navigate(com.example.resumemaker.PdfPreviewRoute(generatedHtml!!)) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = ShapeDefaults.Medium,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) { Text("📄 View Generated PDF", style = MaterialTheme.typography.titleMedium) }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { navController.navigate(com.example.resumemaker.EditResumeRoute) }, modifier = Modifier.weight(1f).height(50.dp), shape = ShapeDefaults.Small, contentPadding = PaddingValues(horizontal = 8.dp)) {
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Edit", maxLines = 1, style = MaterialTheme.typography.labelLarge)
                                }
                                OutlinedButton(onClick = { navController.navigate(com.example.resumemaker.RealTimeEditRoute) }, modifier = Modifier.weight(1f).height(50.dp), shape = ShapeDefaults.Small, contentPadding = PaddingValues(horizontal = 8.dp)) {
                                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Live", maxLines = 1, style = MaterialTheme.typography.labelLarge)
                                }
                                OutlinedButton(
                                    onClick = {
                                        val name = viewModel.getCurrentResumeData()?.name ?: "Candidate"
                                        launchEmailIntent(context, name, "Open Role", analysisResult.coverLetter)
                                    },
                                    modifier = Modifier.weight(1f).height(50.dp), shape = ShapeDefaults.Small, contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(Icons.Default.Email, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Email", maxLines = 1, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                            TextButton(onClick = { viewModel.processWithAI() }, modifier = Modifier.fillMaxWidth()) { Text("Re-run AI Analysis (Consumes Tokens)") }
                        }
                    }
                    1 -> { // TAB: COVER LETTER
                        if (analysisResult.coverLetter.isBlank()) { Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                        else {
                            OutlinedTextField(
                                value = analysisResult.coverLetter, onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth().height(300.dp),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Cover Letter", analysisResult.coverLetter))
                                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                    }) { Icon(Icons.Default.ContentCopy, "Copy") }
                                }
                            )
                        }
                    }
                    2 -> { // TAB: ATS SCORE
                        if (analysisResult.atsFeedback.isBlank()) { Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                        else {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("ATS Feedback", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(8.dp))
                                    Text(analysisResult.atsFeedback)
                                }
                            }
                        }
                    }
                }
            } else {
                // === STATE: READY TO FORGE ===
                if (uiState is UiState.Loading) {
                    // Show the dynamic progressive loading card
                    val loadingMsg = (uiState as UiState.Loading).message

                    Card(
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = ShapeDefaults.Medium
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                strokeWidth = 3.dp
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = loadingMsg, // <--- This is what makes it update!
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    // Show the standard ready button
                    Button(
                        onClick = { viewModel.processWithAI() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = ShapeDefaults.Medium,
                        enabled = extractedText.isNotBlank() && jdText.isNotBlank()
                    ) {
                        Text("Rewrite Resume with AI", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }

        // --- ERROR DIALOG ---
        if (uiState is UiState.Error) {
            AlertDialog(
                onDismissRequest = { viewModel.resetError() },
                confirmButton = { TextButton(onClick = { viewModel.resetError() }) { Text("OK") } },
                title = { Text("Error") }, text = { Text((uiState as UiState.Error).message) },
                containerColor = MaterialTheme.colorScheme.errorContainer, titleContentColor = MaterialTheme.colorScheme.onErrorContainer, textContentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        // --- TEXT PREVIEW SHEET ---
        if (showSheet) {
            ModalBottomSheet(onDismissRequest = { showSheet = false }, sheetState = sheetState) {
                Column(Modifier.padding(16.dp)) {
                    Text("Extracted Text", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = extractedText, onValueChange = { viewModel.updateExtractedText(it) }, modifier = Modifier.fillMaxWidth().height(400.dp), shape = ShapeDefaults.Small)
                    Spacer(Modifier.height(32.dp))
                }
            }
        }

        // --- TEMPLATE PREVIEW DIALOG ---
        styleToPreview?.let { style ->
            val previewData = viewModel.getCurrentResumeData()
            if (previewData != null) {
                TemplatePreviewDialog(
                    style = style,
                    resumeData = previewData,
                    onDismiss = { styleToPreview = null },
                    onSelect = {
                        viewModel.setStyle(style)
                        styleToPreview = null
                    }
                )
            }
        }
    }
}

@Composable
fun TemplatePreviewCard(
    label: String,
    themeColor: Color,
    isSelected: Boolean,
    style: ResumeStyle,
    onClick: () -> Unit,
    onPreview: () -> Unit // NEW Parameter for Eye Icon
) {
    val bgColor = if (isSelected) themeColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) themeColor else MaterialTheme.colorScheme.outlineVariant
    val lineContentColor = Color.Gray

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = Modifier.width(100.dp).height(130.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mini Wireframe Graphic
                Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.White, ShapeDefaults.Small).padding(6.dp)) {
                    if (style == ResumeStyle.MODERN) {
                        Row(Modifier.fillMaxSize()) {
                            Column(Modifier.weight(0.65f)) {
                                Box(Modifier.fillMaxWidth(0.9f).height(6.dp).background(themeColor))
                                Spacer(Modifier.height(4.dp))
                                Box(Modifier.fillMaxWidth(0.5f).height(3.dp).background(themeColor.copy(alpha = 0.5f)))
                                Spacer(Modifier.height(6.dp))
                                Box(Modifier.fillMaxWidth().height(2.dp).background(lineContentColor))
                                Spacer(Modifier.height(2.dp))
                                Box(Modifier.fillMaxWidth().height(2.dp).background(lineContentColor))
                                Spacer(Modifier.height(4.dp))
                                Box(Modifier.fillMaxWidth().height(2.dp).background(lineContentColor))
                                Spacer(Modifier.height(2.dp))
                                Box(Modifier.fillMaxWidth(0.8f).height(2.dp).background(lineContentColor))
                            }
                            Spacer(Modifier.width(4.dp))
                            Column(Modifier.weight(0.35f).fillMaxHeight().background(Color(0xFFF3F4F6)).padding(2.dp)) {
                                Box(Modifier.fillMaxWidth().height(3.dp).background(themeColor.copy(alpha=0.7f)))
                                Spacer(Modifier.height(3.dp))
                                Box(Modifier.fillMaxWidth().height(2.dp).background(lineContentColor))
                                Spacer(Modifier.height(2.dp))
                                Box(Modifier.fillMaxWidth().height(2.dp).background(lineContentColor))
                            }
                        }
                    } else {
                        val align = if (style == ResumeStyle.EXECUTIVE || style == ResumeStyle.CLASSIC) Alignment.CenterHorizontally else Alignment.Start
                        Column(Modifier.fillMaxSize(), horizontalAlignment = align) {
                            Box(Modifier.fillMaxWidth(if(align == Alignment.CenterHorizontally) 0.7f else 0.9f).height(6.dp).background(themeColor))
                            Spacer(Modifier.height(3.dp))
                            Box(Modifier.fillMaxWidth(if(align == Alignment.CenterHorizontally) 0.4f else 0.5f).height(3.dp).background(lineContentColor))
                            Spacer(Modifier.height(6.dp))
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                                Box(Modifier.fillMaxWidth(0.3f).height(3.dp).background(themeColor.copy(alpha=0.7f)))
                                Spacer(Modifier.height(2.dp))
                                Box(Modifier.fillMaxWidth().height(2.dp).background(lineContentColor))
                                Spacer(Modifier.height(2.dp))
                                Box(Modifier.fillMaxWidth(0.9f).height(2.dp).background(lineContentColor))
                                Spacer(Modifier.height(4.dp))
                                Box(Modifier.fillMaxWidth(0.3f).height(3.dp).background(themeColor.copy(alpha=0.7f)))
                                Spacer(Modifier.height(2.dp))
                                Box(Modifier.fillMaxWidth().height(2.dp).background(lineContentColor))
                                Spacer(Modifier.height(2.dp))
                                Box(Modifier.fillMaxWidth(0.8f).height(2.dp).background(lineContentColor))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) themeColor else MaterialTheme.colorScheme.onSurface)
            }

            // Preview Button overlay
            IconButton(
                onClick = onPreview,
                modifier = Modifier.align(Alignment.TopEnd).size(32.dp).padding(4.dp)
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = "Preview", modifier = Modifier.padding(4.dp).size(16.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun TemplatePreviewDialog(
    style: ResumeStyle,
    resumeData: TailoredResume,
    onDismiss: () -> Unit,
    onSelect: () -> Unit
) {
    val htmlContent = remember(style, resumeData) {
        com.example.resumemaker.util.HtmlEngine.generateHtml(resumeData, style)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false, // Allows us to go edge-to-edge
            dismissOnClickOutside = true
        )
    ) {
        // 2. Custom Surface that takes up 96% of the screen width and height
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // HEADER
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "${style.name} Layout",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pinch to zoom in for details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // WEBVIEW BODY (Takes up all remaining space)
                Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.LightGray.copy(alpha = 0.3f))) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = false
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true

                                // Ensure Pinch-to-Zoom is enabled and smooth
                                settings.setSupportZoom(true)
                                settings.builtInZoomControls = true
                                settings.displayZoomControls = false
                            }
                        },
                        update = { view ->
                            view.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // FOOTER ACTIONS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = {
                            onSelect()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Use This Layout")
                    }
                }
            }
        }
    }
}