package com.example.resumemaker.ui

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPreviewScreen(htmlContent: String) {
    val context = LocalContext.current

    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    var isPageLoaded by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Use the remembered reference
                    webViewRef?.let { view ->
                        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                        val jobName = "Resume_Document"
                        val printAdapter = view.createPrintDocumentAdapter(jobName)

                        printManager.print(
                            jobName,
                            printAdapter,
                            PrintAttributes.Builder().build()
                        )
                    }
                },
                containerColor = if(isPageLoaded) MaterialTheme.colorScheme.primaryContainer else Color.Gray
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save PDF")
            }
        }
    ) { padding ->
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = false
                    webViewClient = object : WebViewClient() {
                        // FIX: Detect when page (and fonts) are ready
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isPageLoaded = true
                        }
                    }
                    loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { view ->
                // Capture the view reference here for the Save button
                webViewRef = view
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}