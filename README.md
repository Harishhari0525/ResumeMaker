# 🚀 AI Resume Maker (Android)

**AI Resume Maker** is a modern Android application built with **Jetpack Compose** that leverages Generative AI to tailor your resume for specific job descriptions. 

Stop sending generic resumes. Upload your existing PDF, provide a target Job Description (JD), and let the AI rewrite your experience using the **STAR method**, optimized for ATS systems. Generate professional, print-ready PDFs in seconds with multiple style options.

---

## ✨ Features

* **📄 PDF Text Extraction**: Seamlessly extracts text from your existing PDF resume using `PDFBox`.
* **🤖 AI-Powered Tailoring**: Uses **Google Gemini AI** to rewrite your resume content.
    * Aligns skills and experience with the target JD.
    * Rewrites bullet points using the **STAR method** (Situation, Task, Action, Result).
    * Sanitizes PII (Phone/Email) before processing for privacy.
* **🎨 5 Professional Templates**:
    * **Modern**: Two-column layout with blue accents (Standard Corporate).
    * **Tech (FAANG)**: Dense, single-column, skills-first layout (Optimized for Engineering).
    * **Classic**: Serif fonts, traditional layout (great for Academia/Law).
    * **Creative**: Purple accents with modern typography.
    * **Compact**: Aggressive margin/font optimization to fit content on 1 page.
* **⚡ Instant Style Switching**: Switch between templates instantly **without** re-running the AI analysis (saves API tokens and time).
* **🖨️ Native PDF Export**: Generates high-quality HTML internally and converts it to PDF using Android's native `PrintManager`.
* **🔒 Privacy Focused**: No login required. All PDF generation happens locally on the device via `WebView`.

---

## 🛠️ Tech Stack

* **Language**: [Kotlin](https://kotlinlang.org/)
* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material3)
* **Architecture**: MVVM (Model-View-ViewModel)
* **AI Engine**: [Google Generative AI SDK](https://ai.google.dev/) (Gemini)
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
│   │   └── AIManager.kt       # Handles Gemini API calls & prompt engineering
│   └── pdf
│       └── PdfParser.kt       # Extracts raw text from uploaded PDFs
├── model
│   └── TailoredResume.kt      # Data classes for the parsed resume structure
├── ui
│   ├── MainScreen.kt          # Primary UI: Upload, JD Input, & Style Selector
│   ├── PdfPreviewScreen.kt    # WebView wrapper for PDF preview & saving
│   └── ResumeViewModel.kt     # Manages UI state, data persistence, & logic
└── util
    └── HtmlEngine.kt          # The Core Engine: Generates dynamic HTML/CSS for all 5 templates
```

## 🛠️ Setup & Installation

1.  **Clone the repository**:
    ```bash
    git clone [https://github.com/yourusername/ai-resume-maker.git](https://github.com/yourusername/ai-resume-maker.git)
    ```

2.  **Open in Android Studio**:
    * Android Studio Ladybug or newer is recommended.

3.  **Get an API Key**:
    * Go to [Google AI Studio](https://aistudio.google.com/).
    * Create a new API Key.

4.  **Configure API Key**:
    * Open `local.properties` in your project root.
    * Add the following line:
      ```properties
      GEMINI_API_KEY=your_api_key_here
      ```
    * *Note: Ensure your `AIManager.kt` reads this key via `BuildConfig`.*

5.  **Run the App**:
    * Sync Gradle.
    * Run on an Android Emulator or Physical Device.

---

## 📱 How to Use

1.  **Upload Resume**: Tap the upload area to select your current PDF resume.
2.  **Preview Text**: (Optional) Check the extracted text to ensure the PDF was parsed correctly.
3.  **Enter JD**: Paste the Job Description of the role you are applying for.
4.  **Select Style**: Choose a template (e.g., "Tech (FAANG)" or "Modern").
5.  **Rewrite**: Tap **"Rewrite Resume"**. The AI will analyze and restructure your data.
6.  **View & Save**:
    * Once finished, tap **"View Generated PDF"**.
    * Review the document in the preview screen.
    * Tap the **Save (Floppy Disk)** icon to save it as a PDF file to your device.
7.  **Change Style**: Don't like the look? Go back, tap a different style chip (e.g., "Classic"), and the PDF updates **instantly** without using more AI tokens.

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
