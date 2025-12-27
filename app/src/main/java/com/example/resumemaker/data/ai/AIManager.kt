package com.example.resumemaker.data.ai

import android.graphics.Bitmap
import com.example.resumemaker.model.TailoredResume
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.content
import kotlinx.serialization.json.Json

private val jsonParser = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

class AIManager {
    private val model  = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel(
            modelName = "gemini-2.5-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                // We define the schema so the AI knows exactly what JSON to build
                responseSchema = Schema.obj(
                    mapOf(
                        "name" to Schema.string(),
                        "contactInfo" to Schema.string(),
                        "summary" to Schema.string(),

                        // Experience
                        "experience" to Schema.array(
                            Schema.obj(
                                mapOf(
                                    "company" to Schema.string(),
                                    "role" to Schema.string(),
                                    "duration" to Schema.string(),
                                    "location" to Schema.string(),
                                    "bulletPoints" to Schema.array(Schema.string())
                                )
                            )
                        ),

                        // Projects (NEW)
                        "projects" to Schema.array(
                            Schema.obj(
                                mapOf(
                                    "title" to Schema.string(),
                                    "technologies" to Schema.string(),
                                    "bulletPoints" to Schema.array(Schema.string())
                                )
                            )
                        ),

                        // Education (NEW)
                        "education" to Schema.array(
                            Schema.obj(
                                mapOf(
                                    "school" to Schema.string(),
                                    "degree" to Schema.string(),
                                    "year" to Schema.string()
                                )
                            )
                        ),

                        "skills" to Schema.array(Schema.string())
                    )
                )
            },
            systemInstruction = content {
                text("""
                    You are an expert ATS Resume Writer and Career Coach. 
                    Your goal is to rewrite the user's resume to strictly align with the provided Target Job Description (JD).
                    
                    CRITICAL RULES:
                    1. DO NOT just copy the original bullet points. You MUST REWRITE them.
                    2. Use the "STAR" method (Situation, Task, Action, Result) for every bullet point.
                    3. Prioritize keywords from the JD. If the JD asks for "Kotlin", ensure "Kotlin" appears in the bullet points.
                    4. Use strong Action Verbs (e.g., "Architected," "Orchestrated," "Reduced," "Accelerated").
                    5. QUANTIFY results where possible (e.g., "reduced latency by 40%", "managed team of 5").
                    6. If the original resume lacks specific details needed for the JD, optimize the phrasing but DO NOT HALLUCINATE (do not invent fake companies or dates).
                    7. Keep the "Summary" punchy and focused on the value proposition for THIS specific job.
                    8. FORMATTING SKILLS:
                        The "skills" list MUST be categorized.
                        Example Output format for skills list:
                        [
                            "Languages: Kotlin, Java, Python, SQL",
                            "DevOps: Jenkins, Docker, Kubernetes",
                            "Monitoring: Splunk, AppDynamics"
                        ]
                        Do not just output a flat list of keywords. Group them.
                    9. **FACTUAL INTEGRITY (CRITICAL)**: 
                        - DO NOT change the user's Total Years of Experience (YOE). If they have 5 years and JD asks for 3, KEEP "5 years".
                        - DO NOT change Company Names, Job Titles, or Dates of Employment.
                        - DO NOT invent skills or degrees the user does not have.
                        - You may *emphasize* relevant experience, but do not *delete* or *alter* the truth.
                    
                    Output the result strictly in the requested JSON schema.
                """.trimIndent())
            }
        )

    suspend fun tailorResume(rawResume: String, jobDescription: String): TailoredResume? {
        val prompt = "Resume: $rawResume \n\n Job Description: $jobDescription"
        return try {
            val response = model.generateContent(prompt)
            val textResponse = response.text ?: return null

            // This call right here clears the 'never used' warning
            parseAIResponse(textResponse)
        } catch (e: Exception) {
            android.util.Log.e("ResumeAI", "Generation Failed: ${e.message}")
            null
        }
    }

    fun parseAIResponse(rawResponse: String): TailoredResume? {
        return try {
            // Remove markdown code blocks if present
            val cleanJson = rawResponse
                .trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()

            jsonParser.decodeFromString<TailoredResume>(cleanJson)
        } catch (e: Exception) {
            android.util.Log.e("ResumeAI", "Parsing Failed: ${e.message}")
            null
        }
    }

    suspend fun extractTextFromImage(bitmap: Bitmap): String? {
        val prompt = "Analyze this image. If it contains a Job Description, extract the text exactly. If not, return 'No text found'."

        return try {
            val response = model.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )
            response.text
        } catch (_: Exception) {
            null
        }
    }

    private val textModel = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel(
            modelName = "gemini-2.5-flash"
            // No generationConfig = Defaults to Plain Text
        )

    // 3. Cover Letter Generator (New)
    suspend fun generateCoverLetter(resume: TailoredResume, jd: String): String? {
        val prompt = """
            Write a persuasive cover letter for ${resume.name} applying for the role described below.
            Use the candidate's experience to prove they are a perfect match.
            Keep it professional, concise, and standard business format.
            
            RESUME SUMMARY: ${resume.summary}
            SKILLS: ${resume.skills.joinToString()}
            JOB DESCRIPTION: ${jd.take(2000)}
            
            Output ONLY the cover letter text.
        """.trimIndent()

        return try {
            val response = textModel.generateContent(prompt)
            response.text
        } catch (_: Exception) { null }
    }

    // 4. ATS Score (New)
    suspend fun evaluateResume(resume: TailoredResume, jd: String): String? {
        val prompt = """
            Act as an ATS Scanner. Compare the Resume against the JD.
            1. Give a Match Score (0-100%).
            2. List 3 Missing Keywords.
            3. Give 1 sentence of improvement advice.
            
            RESUME: ${resume.summary} + ${resume.skills}
            JD: ${jd.take(2000)}
            
            Output format:
            Score: 85/100
            Missing: Kotlin, AWS, CI/CD
            Advice: Add more metrics to your experience.
        """.trimIndent()

        return try {
            val response = textModel.generateContent(prompt)
            response.text
        } catch (_: Exception) { null }
    }

}