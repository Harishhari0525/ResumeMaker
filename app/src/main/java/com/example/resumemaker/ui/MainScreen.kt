package com.example.resumemaker.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    // 1. PDF Picker
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.extractPdf(it) }
    }

    // 2. Photo JD Picker
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.extractJdFromImage(it) }
    }

    // --- UI STATES ---
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Resume, 1=CoverLetter, 2=ATS
    val tabs = listOf("Resume PDF", "Cover Letter", "ATS Score")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "AI Resume Maker",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                windowInsets = WindowInsets(top = 0.dp, bottom = 0.dp),
                actions = {
                    IconButton(onClick = { navController.navigate(com.example.resumemaker.HistoryRoute) }) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (extractedText.isNotBlank()) "✅ PDF Loaded" else "Tap to Upload Resume (PDF)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Preview Text Button
            if (extractedText.isNotBlank()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = { showSheet = true }) {
                        Icon(Icons.Default.Description, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Check Extracted Text")
                    }
                }
            }

            // --- 2. JD INPUT (Text + Photo) ---
            OutlinedTextField(
                value = jdText,
                onValueChange = { viewModel.updateJdText(it) },
                label = { Text("Job Description") },
                placeholder = { Text("Paste text or scan image...") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 200.dp),
                shape = ShapeDefaults.Medium,
                trailingIcon = {
                    IconButton(onClick = {
                        photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(Icons.Default.CameraAlt, "Scan Image", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // --- 3. TEMPLATE SELECTOR ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Select Template",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val styles = listOf(
                        Triple(ResumeStyle.MODERN, "Modern", Color(0xFF2196F3)),
                        Triple(ResumeStyle.TECH_MINIMAL, "Tech", Color(0xFF212121)),
                        Triple(ResumeStyle.EXECUTIVE, "Executive", Color(0xFF000000)),
                        Triple(ResumeStyle.CLASSIC, "Classic", Color(0xFF795548)),
                        Triple(ResumeStyle.CREATIVE, "Creative", Color(0xFF9C27B0)),
                        Triple(ResumeStyle.COMPACT, "Compact", Color(0xFF607D8B))
                    )

                    styles.forEach { (style, label, chipColor) ->
                        val isSelected = currentStyle == style
                        val isDark = (chipColor.red * 0.299 + chipColor.green * 0.587 + chipColor.blue * 0.114) < 0.5

                        val contentColor = if (isSelected) {
                            if (isDark) Color.White else Color.Black
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setStyle(style) },
                            enabled = true,
                            label = {
                                Text(
                                    label,
                                    color = contentColor,
                                    fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, null, tint = contentColor) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipColor,
                                selectedLabelColor = contentColor,
                                selectedLeadingIconColor = contentColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if(isSelected) chipColor else MaterialTheme.colorScheme.outline,
                                borderWidth = 1.dp,
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // --- 4. ACTION AREA (Conditional) ---

            if (generatedHtml != null) {
                // === STATE: SUCCESS (TABS) ===

                // FIX 2: Replaced TabRow with PrimaryTabRow
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                    // 'divider' param is removed as it's not supported in PrimaryTabRow
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, maxLines = 1, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> { // TAB: RESUME
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { navController.navigate(com.example.resumemaker.PdfPreviewRoute(generatedHtml!!)) },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = ShapeDefaults.Small
                                ) {
                                    Text("View PDF 📄")
                                }

                                OutlinedButton(
                                    onClick = { navController.navigate(com.example.resumemaker.EditResumeRoute) },
                                    modifier = Modifier.width(80.dp).height(50.dp),
                                    shape = ShapeDefaults.Small
                                ) {
                                    Icon(Icons.Default.Edit, "Edit")
                                }
                            }

                            TextButton(onClick = { viewModel.processWithAI() }, modifier = Modifier.fillMaxWidth()) {
                                Text("Re-run AI Analysis (Consumes Tokens)")
                            }
                        }
                    }

                    1 -> { // TAB: COVER LETTER
                        if (analysisResult.coverLetter.isBlank()) {
                            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else {
                            OutlinedTextField(
                                value = analysisResult.coverLetter,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().height(300.dp),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Cover Letter", analysisResult.coverLetter)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Default.ContentCopy, "Copy")
                                    }
                                }
                            )
                        }
                    }

                    2 -> { // TAB: ATS SCORE
                        if (analysisResult.atsFeedback.isBlank()) {
                            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
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

                Button(
                    onClick = { viewModel.processWithAI() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = ShapeDefaults.Medium,
                    enabled = extractedText.isNotBlank() && jdText.isNotBlank() && uiState !is UiState.Loading
                ) {
                    if (uiState is UiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(16.dp))
                        Text("Forging Resume...")
                    } else {
                        Text("Rewrite Resume with AI", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        // --- ERROR DIALOG ---
        if (uiState is UiState.Error) {
            val errorMsg = (uiState as UiState.Error).message
            AlertDialog(
                onDismissRequest = { viewModel.resetError() },
                confirmButton = { TextButton(onClick = { viewModel.resetError() }) { Text("OK") } },
                title = { Text("Error") },
                text = { Text(errorMsg) },
                containerColor = MaterialTheme.colorScheme.errorContainer,
                titleContentColor = MaterialTheme.colorScheme.onErrorContainer,
                textContentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        // --- TEXT PREVIEW SHEET ---
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Extracted Text", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = extractedText,
                        onValueChange = { viewModel.updateExtractedText(it) },
                        modifier = Modifier.fillMaxWidth().height(400.dp),
                        shape = ShapeDefaults.Small
                    )
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}