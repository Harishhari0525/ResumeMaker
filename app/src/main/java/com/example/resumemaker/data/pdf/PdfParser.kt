package com.example.resumemaker.data.pdf

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class PdfParser(private val context: Context) {

    init {
        // Essential: Initialize PDFBox for Android context
        PDFBoxResourceLoader.init(context)
    }

    suspend fun extractText(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                // Load the document from the stream
                PDDocument.load(inputStream).use { document ->
                    val stripper = PDFTextStripper().apply {
                        sortByPosition = true // Keeps the text in logical reading order
                    }
                    return@withContext stripper.getText(document)
                }
            } ?: "Error: Could not open file stream."
        } catch (e: IOException) {
            "Error reading PDF: ${e.localizedMessage}"
        } catch (e: Exception) {
            "Unknown error: ${e.localizedMessage}"
        }
    }
}