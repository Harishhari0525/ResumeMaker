package com.example.resumemaker.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.resumemaker.data.ai.AIManager
import com.example.resumemaker.data.pdf.PdfParser
import com.example.resumemaker.model.TailoredResume
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.resumemaker.util.HtmlEngine
import com.example.resumemaker.util.ResumeStyle
import kotlinx.coroutines.async
import kotlinx.serialization.json.Json
import java.io.File


private val jsonParser = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    prettyPrint = true
}

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Error(val message: String) : UiState()
}

data class AnalysisResult(
    val coverLetter: String = "",
    val atsFeedback: String = ""
)

class ResumeViewModel(
    private val context: Context,
    private val pdfParser: PdfParser,
    private val aiManager: AIManager
) : ViewModel() {

    // UI Loading/Error State
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Data States
    private val _extractedText = MutableStateFlow("")
    val extractedText: StateFlow<String> = _extractedText.asStateFlow()

    private val _jdText = MutableStateFlow("")
    val jdText: StateFlow<String> = _jdText.asStateFlow()

    private val _currentStyle = MutableStateFlow(ResumeStyle.MODERN)
    val currentStyle = _currentStyle.asStateFlow()

    // RESULT: The Generated HTML (Persistent)
    private val _generatedHtml = MutableStateFlow<String?>(null)
    val generatedHtml: StateFlow<String?> = _generatedHtml.asStateFlow()

    // Cache the AI object so we can regenerate HTML locally
    private var lastGeneratedResume: TailoredResume? = null

    private val _analysisResult = MutableStateFlow(AnalysisResult())
    val analysisResult = _analysisResult.asStateFlow()

    // HISTORY LIST
    private val _historyList = MutableStateFlow<List<File>>(emptyList())
    val historyList = _historyList.asStateFlow()

    init {
        loadHistory()
    }

    // --- ACTIONS ---

    fun setStyle(style: ResumeStyle) {
        if (_currentStyle.value != style) {
            _currentStyle.value = style

            // If we have data, regenerate the HTML instantly (Zero Tokens)
            lastGeneratedResume?.let { data ->
                val htmlCode = HtmlEngine.generateHtml(data, style)
                _generatedHtml.value = htmlCode
            }
        }
    }

    fun extractPdf(uri: Uri) {
        // New file uploaded -> Clear previous results
        resetAll()

        viewModelScope.launch {
            try {
                val rawText = pdfParser.extractText(uri)
                val prettyText = cleanPdfText(rawText)
                _extractedText.value = prettyText
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to read PDF: ${e.localizedMessage}")
            }
        }
    }

    fun extractJdFromImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // Convert Uri to Bitmap
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                // Call AI
                val extractedText = aiManager.extractTextFromImage(bitmap)
                if (!extractedText.isNullOrBlank()) {
                    _jdText.value = extractedText
                    _uiState.value = UiState.Idle
                } else {
                    _uiState.value = UiState.Error("Could not read text from image.")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Image error: ${e.message}")
            }
        }
    }

    // --- FEATURE 1 & 2: EXTRAS ---
    fun generateExtras() {
        val resume = lastGeneratedResume ?: return
        val jd = _jdText.value

        viewModelScope.launch {
            // Run parallel
            val coverLetter = async { aiManager.generateCoverLetter(resume, jd) }
            val ats = async { aiManager.evaluateResume(resume, jd) }

            _analysisResult.value = AnalysisResult(
                coverLetter = coverLetter.await() ?: "Failed to generate.",
                atsFeedback = ats.await() ?: "Failed to analyze."
            )
        }
    }

    // --- FEATURE 3: HISTORY (Simple File System) ---
    private fun saveToHistory(data: TailoredResume) {
        viewModelScope.launch {
            try {
                val filename = "resume_${System.currentTimeMillis()}.json"
                val file = File(context.filesDir, filename)

                file.writeText(jsonParser.encodeToString(data))

                loadHistory()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun loadHistory() {
        val files = context.filesDir.listFiles { _, name -> name.startsWith("resume_") }
        _historyList.value = files?.toList()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun updateJdText(text: String) {
        _jdText.value = text
    }

    fun updateExtractedText(text: String) {
        _extractedText.value = text
    }

    fun processWithAI() {
        val resumeText = _extractedText.value
        val jobDescription = _jdText.value

        if (resumeText.isBlank() || jobDescription.isBlank()) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // 1. Sanitize & Trim to save tokens
                val cleanResume = resumeText.take(25000)
                val cleanJd = jobDescription.take(10000)

                // 2. Call AI
                val resumeData = aiManager.tailorResume(cleanResume, cleanJd)

                if (resumeData != null) {
                    lastGeneratedResume = resumeData
                    saveToHistory(resumeData)
                    generateExtras()

                    // 3. Generate HTML locally
                    val htmlCode = HtmlEngine.generateHtml(resumeData, _currentStyle.value)
                    _generatedHtml.value = htmlCode

                    _uiState.value = UiState.Idle
                } else {
                    _uiState.value = UiState.Error("AI returned empty result.")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error: ${e.localizedMessage}")
            }
        }
    }

    fun resetError() {
        _uiState.value = UiState.Idle
    }

    // Clears everything when a NEW file is uploaded
    private fun resetAll() {
        _uiState.value = UiState.Idle
        _generatedHtml.value = null
        lastGeneratedResume = null
        _extractedText.value = ""
    }

    private fun cleanPdfText(text: String): String {
        return text
            .replace(Regex("\\.(?=[A-Z])"), ". ")
            .replace(Regex("(?i)(Experience|Education|Skills|Summary|Projects|Certifications)"), "\n\n$1\n")
            .replace(Regex("(?<=[a-z])\\s+([•-])"), "\n$1")
            .replace("  ", "\n")
            .trim()
    }

    fun updateResumeData(updatedResume: TailoredResume) {
        lastGeneratedResume = updatedResume
        // Regenerate HTML with the new data and current style
        val htmlCode = HtmlEngine.generateHtml(updatedResume, _currentStyle.value)
        _generatedHtml.value = htmlCode
    }

    fun getCurrentResumeData(): TailoredResume? {
        return lastGeneratedResume
    }

    fun loadResumeFromHistory(file: File) {
        viewModelScope.launch {
            try {
                val jsonString = file.readText()
                // FIX: Use the shared 'jsonParser' instance
                val data = jsonParser.decodeFromString<TailoredResume>(jsonString)

                lastGeneratedResume = data
                val htmlCode = HtmlEngine.generateHtml(data, _currentStyle.value)
                _generatedHtml.value = htmlCode

                // Regenerate extras since they aren't saved in JSON yet
                generateExtras()
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = UiState.Error("Failed to load saved resume.")
            }
        }
    }

    fun deleteHistoryItem(file: File) {
        if (file.exists()) {
            file.delete()
            loadHistory()
        }
    }

}

class ResumeViewModelFactory(
    private val context: Context,
    private val parser: PdfParser,
    private val ai: AIManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResumeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ResumeViewModel(context, parser, ai) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}