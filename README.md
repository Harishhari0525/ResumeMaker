# 🚀 AI Resume Maker

**AI Resume Maker** is a powerful Android application built with **Jetpack Compose** that leverages **Google Gemini 2.5 Flash** to forge the perfect resume.

Stop sending generic resumes. Upload your existing PDF, scan a target Job Description (JD) from a photo or text, and let the AI rewrite your experience using the **STAR method**. The app also generates a tailored **Cover Letter** and provides an **ATS Match Score** to ensure you stand out.

---

## ✨ Features

### 🤖 AI Core (Gemini 2.5 Flash)
* **📄 PDF Text Extraction**: Seamlessly extracts text from your existing PDF resume using `PdfBox-Android`.
* **📸 Photo JD Scanner**: Use the camera to scan Job Descriptions directly from laptop screens or posters (Multimodal AI).
* **✍️ Smart Rewriting**:
    * Rewrites bullet points using the **STAR method** (Situation, Task, Action, Result).
    * **Factual Integrity Engine**: AI is strictly instructed *never* to hallucinate dates, companies, or years of experience.
* **📝 Cover Letter Generator**: Automatically drafts a persuasive cover letter matching the generated resume.
* **📊 ATS Scorer**: Analyzes your resume against the JD and provides a match score (0-100%) with missing keywords.

### 🎨 6 Professional Templates
* **Modern**: Two-column layout with blue accents.
* **Tech (FAANG)**: Dense, single-column, optimized for engineering roles.
* **Executive**: High-end serif typography, elegant spacing.
* **Classic**: Traditional serif layout (Academia/Law).
* **Creative**: Purple accents with modern typography.
* **Compact**: Fits maximum content onto a single page.

### 🛠️ Utilities
* **✏️ Manual Editing**: Typo in the AI output? Edit name, summary, and skills manually before generating the PDF.
* **💾 History & Drafts**: Automatically saves every resume you generate. Reload and edit them anytime.
* **⚡ Instant Style Switching**: Change templates instantly *without* re-running the AI (Zero token usage).
* **🔒 Privacy Focused**: No login required. PDF generation happens locally on the device via `WebView`.

---

## 🛠️ Tech Stack

* **Language**: [Kotlin](https://kotlinlang.org/)
* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material3)
* **Architecture**: MVVM (Model-View-ViewModel)
* **AI Engine**: [Google AI for Android](https://developer.android.com/ai/google-ai-client-sdks/docs/firebase-android) (Firebase SDK)
* **Navigation**: [Type-Safe Compose Navigation](https://developer.android.com/guide/navigation/design/type-safety)
* **Serialization**: Kotlin Serialization (JSON Parsing)
* **PDF Tools**:
    * [PdfBox-Android](https://github.com/TomRoush/PdfBox-Android) (Extraction)
    * Android Native Print Service (Generation)
* **Concurrency**: Kotlin Coroutines & Flow

---

## 📂 Project Structure

```text
com.example.resumemaker
├── data
│   ├── ai
│   │   └── AIManager.kt        # Handles Gemini interactions (Resume, Vision, Cover Letter)
│   └── pdf
│       └── PdfParser.kt        # Extracts raw text from uploaded PDFs
├── model
│   └── TailoredResume.kt       # Data classes for the parsed resume structure
├── ui
│   ├── MainScreen.kt           # Dashboard: Upload, Camera, Style Selector
│   ├── EditResumeScreen.kt     # Editor: Manual corrections for Resume data
│   ├── HistoryScreen.kt        # List of saved/previous resumes
│   ├── PdfPreviewScreen.kt     # WebView wrapper for PDF preview & saving
│   ├── ResumeViewModel.kt      # Manages state, AI calls, and File I/O
│   └── theme                   # Material3 Theme & Typography
└── util
    └── HtmlEngine.kt           # Generates dynamic HTML/CSS for all 6 templates
```

## 🛠️ Setup & Installation

1.  **Clone the repository**:
    ```bash
    e.g., git clone repo_name.git
    ```

2.  **Open in Android Studio**:
    * Android Studio Ladybug or newer is recommended.

3.  **Get an API Key**:
    * Go to [Google AI Studio](https://aistudio.google.com/).
    * Create a new API Key.

4.  **Add Firebase Configuration**:
    * Download your google-services.json file from the Firebase Console.
    * Place the file in the app/ directory of the project.
    * Note: The API Key is automatically read from this file.

5.  **Run the App**:
    * Sync Gradle.
    * Run on an Android Emulator or Physical Device.

---

## 📱 How to Use

1.  **Upload Resume**: Tap the cloud icon to pick your current PDF resume.
2.  **Input Job Description**:
      * **Paste Text**: Copy-paste the JD into the text box.
      * **Scan Photo**: Tap the Camera Icon inside the text box to upload a screenshot or photo of the JD.
3.  **Select Template**: Choose a style (e.g., "Tech" or "Executive").
4.  **Forge Resume**: Tap "**Rewrite Resume with AI**".
5.  **Review Results**:
      * **Resume Tab**: View and Edit the generated resume.
      * **Cover Letter Tab**: Copy the AI-written cover letter.
      * **ATS Score Tab**: Check your match score and missing keywords.
6.  **Export**: Tap "**View PDF**" -> **Save Icon** to download the final PDF.
7.  **History**: Tap the **Clock Icon** in the top bar to view past resumes.

---

## 🤝 Contributing

Contributions are welcome!

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
