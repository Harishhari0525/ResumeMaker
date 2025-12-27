package com.example.resumemaker.util

import com.example.resumemaker.model.TailoredResume

enum class ResumeStyle {
    CLASSIC,        // Serif, Traditional
    MODERN,         // Two-Column, Blue Header
    TECH_MINIMAL,   // Clean, Sans-Serif, High Density (FAANG)
    CREATIVE,       // Purple Accents
    COMPACT,         // 1-Page Optimizer (Aggressive)
    EXECUTIVE
}

object HtmlEngine {

    private fun escape(text: String): String {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;")
    }

    fun generateHtml(data: TailoredResume, style: ResumeStyle): String {
        return when (style) {
            ResumeStyle.MODERN -> generateTwoColumnLayout(data)
            else -> generateSingleColumnLayout(data, style)
        }
    }

    // --- CSS TUNER ---
    private fun getCssVariables(style: ResumeStyle): String {
        return when (style) {
            ResumeStyle.CLASSIC -> ":root { --font-main: 'Merriweather', serif; --color-primary: #000; --color-sec: #333; --font-size-base: 11px; --header-align: center; --border-style: 1px solid #000; --margin-page: 15mm; --line-height: 1.4; }"
            ResumeStyle.TECH_MINIMAL -> ":root { --font-main: 'Inter', sans-serif; --color-primary: #000; --color-sec: #222; --font-size-base: 10pt; --header-align: left; --border-style: 1px solid #999; --margin-page: 10mm; --line-height: 1.28; }"
            ResumeStyle.CREATIVE -> ":root { --font-main: 'Lato', sans-serif; --color-primary: #7c3aed; --color-sec: #4b5563; --font-size-base: 11px; --header-align: left; --border-style: 2px solid #7c3aed; --margin-page: 15mm; --line-height: 1.4; }"
            ResumeStyle.COMPACT -> ":root { --font-main: 'Inter', sans-serif; --color-primary: #000; --color-sec: #222; --font-size-base: 10px; --header-align: center; --border-style: 1px solid #ccc; --margin-page: 8mm; --line-height: 1.25; }"
            ResumeStyle.EXECUTIVE -> ":root { --font-main: 'Georgia', serif; --color-primary: #1a1a1a; --color-sec: #444; --font-size-base: 11pt; --header-align: center; --border-style: 2px solid #000; --margin-page: 18mm; --line-height: 1.5; }"
            else -> ""
        }
    }

    private fun getFontLink(style: ResumeStyle): String {
        return when (style) {
            ResumeStyle.CLASSIC -> "https://fonts.googleapis.com/css2?family=Merriweather:ital,wght@0,300;0,400;0,700;1,300&display=swap"
            ResumeStyle.TECH_MINIMAL, ResumeStyle.COMPACT -> "https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700&display=swap"
            ResumeStyle.CREATIVE -> "https://fonts.googleapis.com/css2?family=Lato:wght@300;400;700&display=swap"
            ResumeStyle.EXECUTIVE -> "https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&display=swap"
            else -> "https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap"
        }
    }

    // --- SINGLE COLUMN GENERATOR ---
    private fun generateSingleColumnLayout(data: TailoredResume, style: ResumeStyle): String {
        val cssVars = getCssVariables(style)
        val fontLink = getFontLink(style)

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <link href="$fontLink" rel="stylesheet">
            <style>
                $cssVars
                
                @page { 
                    size: A4; 
                    margin: var(--margin-page); 
                }
                
                * { box-sizing: border-box; }
                
                body {
                    font-family: var(--font-main);
                    margin: 0; 
                    padding: 0;
                    color: var(--color-sec);
                    line-height: var(--line-height);
                    font-size: var(--font-size-base);
                    background: #fff;
                }

                h1 { font-size: 2.2em; color: var(--color-primary); margin: 0 0 2px 0; text-transform: uppercase; text-align: var(--header-align); font-weight: 700; letter-spacing: 0.5px; }
                .role { text-align: var(--header-align); font-size: 1.1em; color: var(--color-sec); margin-bottom: 4px; font-weight: 500;}
                .contact-info { margin-bottom: 10px; text-align: var(--header-align); font-size: 0.9em; padding-bottom: 5px; }
                
                h2 {
                    font-size: 1.0em;
                    text-transform: uppercase;
                    border-bottom: var(--border-style);
                    padding-bottom: 2px;
                    margin-top: 10px;
                    margin-bottom: 6px;
                    color: var(--color-primary);
                    letter-spacing: 0.5px;
                    font-weight: 700;
                }

                /* FIX: Allow page breaks inside the block, but keep header together */
                .job-block { 
                    margin-bottom: 8px; 
                    /* page-break-inside: avoid;  <-- REMOVED THIS */
                }
                
                .job-header-group {
                    page-break-inside: avoid; /* Keep Title + Company together */
                    page-break-after: avoid;  /* Don't break right after the header */
                }
                
                .job-header { display: flex; justify-content: space-between; font-weight: 700; color: #000; font-size: 1.0em; }
                .job-sub { display: flex; justify-content: space-between; font-style: italic; margin-bottom: 2px; font-size: 0.95em; }
                
                ul { margin: 2px 0 0 14px; padding: 0; }
                li { margin-bottom: 1px; text-align: justify; }
                
                .skills-section { margin-top: 4px; line-height: 1.4; }
                .skill-cat { font-weight: 700; color: var(--color-primary); }
            </style>
        </head>
        <body>
            <h1>${escape(data.name)}</h1>
            <div class="role">${escape(data.experience.firstOrNull()?.role ?: "Candidate")}</div>
            <div class="contact-info">
                ${data.contactInfo.split("|").joinToString(" • ") { escape(it.trim()) }}
            </div>

            <h2>Professional Summary</h2>
            <p style="margin-top:2px; text-align:justify;">${escape(data.summary)}</p>

            <h2>Technical Skills</h2>
            <div class="skills-section">
                 ${data.skills.joinToString("<br>") { skill ->
            val parts = skill.split(":", limit = 2)
            if (parts.size > 1) "<span class='skill-cat'>${escape(parts[0].trim())}:</span> ${escape(parts[1].trim())}"
            else "• ${escape(skill)}"
        }}
            </div>

            <h2>Experience</h2>
            ${data.experience.joinToString("") { job ->
            """
                <div class="job-block">
                    <div class="job-header-group">
                        <div class="job-header"><span>${escape(job.company)}</span><span>${escape(job.duration)}</span></div>
                        <div class="job-sub"><span>${escape(job.role)}</span><span>${escape(job.location)}</span></div>
                    </div>
                    <ul>${job.bulletPoints.joinToString("") { "<li>${escape(it)}</li>" }}</ul>
                </div>
                """
        }}

            <h2>Personal Projects</h2>
            ${data.projects.joinToString("") { proj ->
            """
                <div class="job-block">
                    <div class="job-header-group">
                        <div class="job-header"><span>${escape(proj.title)}</span></div>
                        <div class="job-sub"><span>${escape(proj.technologies)}</span></div>
                    </div>
                    <ul>${proj.bulletPoints.joinToString("") { "<li>${escape(it)}</li>" }}</ul>
                </div>
                """
        }}

            <h2>Education</h2>
            ${data.education.joinToString("") { edu ->
            """
                <div class="job-block">
                    <div class="job-header"><span>${escape(edu.school)}</span><span>${escape(edu.year)}</span></div>
                    <div>${escape(edu.degree)}</div>
                </div>
                """
        }}
        </body>
        </html>
        """.trimIndent()
    }

    // --- TWO COLUMN GENERATOR (MODERN) ---
    private fun generateTwoColumnLayout(data: TailoredResume): String {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700&display=swap" rel="stylesheet">
            <style>
                @page { size: A4; margin: 10mm; } 
                * { box-sizing: border-box; }
                body { font-family: 'Inter', sans-serif; margin: 0; padding: 0; color: #1f2937; line-height: 1.4; font-size: 11px; background: #fff; }
                
                .header-container { padding: 10px 0 20px 0; border-bottom: 2px solid #e5e7eb; margin-bottom: 15px; }
                .name { font-size: 24px; font-weight: 700; color: #111827; margin-bottom: 2px; text-transform: uppercase; }
                .role { font-size: 14px; color: #4b5563; font-weight: 500; margin-bottom: 8px; }
                .contact-bar { display: flex; gap: 12px; font-size: 10.5px; color: #6b7280; flex-wrap: wrap; }
                
                .main-grid { display: grid; grid-template-columns: 68% 30%; gap: 2%; min-height: 90vh; }
                .col-left { }
                .col-right { background-color: #f9fafb; padding: 10px; border-radius: 4px; height: fit-content; }
                
                h2 { font-size: 12px; font-weight: 700; color: #374151; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 8px; border-bottom: 1px solid #d1d5db; padding-bottom: 3px; margin-top: 0; }
                .section { margin-bottom: 15px; }
                
                /* FIX: Allow breaks inside, but keep header together */
                .job-block { 
                    margin-bottom: 10px; 
                    /* page-break-inside: avoid; <-- REMOVED */
                }
                .job-header-group {
                    page-break-inside: avoid;
                    page-break-after: avoid;
                }

                .job-title { font-weight: 700; font-size: 12px; color: #000; }
                .company-row { display: flex; justify-content: space-between; margin-bottom: 2px; font-size: 11px; color: #4b5563; }
                
                ul { margin: 2px 0 0 12px; padding: 0; }
                li { margin-bottom: 2px; text-align: justify; }
                
                .skill-block { margin-bottom: 8px; }
                .skill-cat { font-weight: 700; display: block; margin-bottom: 1px; font-size: 10.5px; }
            </style>
        </head>
        <body>
            <div class="header-container">
                <div class="name">${escape(data.name)}</div>
                <div class="role">${escape(data.experience.firstOrNull()?.role ?: "Candidate")}</div>
                <div class="contact-bar">${data.contactInfo.split("|").joinToString(" • ") { escape(it.trim()) }}</div>
            </div>
            <div class="main-grid">
                <div class="col-left">
                    <div class="section"><h2>Profile</h2><p style="text-align:justify;">${escape(data.summary)}</p></div>
                    <div class="section"><h2>Experience</h2>
                    ${data.experience.joinToString("") { job ->
            """<div class="job-block">
                        <div class="job-header-group">
                            <div class="job-title">${escape(job.role)}</div>
                            <div class="company-row"><span>${escape(job.company)}</span><span>${escape(job.duration)}</span></div>
                        </div>
                        <ul>${job.bulletPoints.joinToString("") { "<li>${escape(it)}</li>" }}</ul></div>"""
        }}</div>
                    <div class="section"><h2>Projects</h2>
                    ${data.projects.joinToString("") { proj ->
            """<div class="job-block">
                         <div class="job-header-group">
                            <div class="job-title">${escape(proj.title)}</div>
                            <div class="company-row"><span>${escape(proj.technologies)}</span></div>
                         </div>
                        <ul>${proj.bulletPoints.joinToString("") { "<li>${escape(it)}</li>" }}</ul></div>"""
        }}</div>
                </div>
                <div class="col-right">
                    <div class="section"><h2>Skills</h2>
                    ${data.skills.joinToString("") { skill ->
            val parts = skill.split(":", limit = 2)
            if (parts.size > 1) """<div class="skill-block"><span class="skill-cat">${escape(parts[0])}</span><span>${escape(parts[1])}</span></div>"""
            else """<div>• ${escape(skill)}</div>"""
        }}</div>
                    <div class="section"><h2>Education</h2>
                     ${data.education.joinToString("") { edu ->
            """<div style="margin-bottom:10px"><b>${escape(edu.degree)}</b><br><i>${escape(edu.school)}</i><br><small>${escape(edu.year)}</small></div>"""
        }}</div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
}