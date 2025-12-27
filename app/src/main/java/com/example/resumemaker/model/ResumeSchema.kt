package com.example.resumemaker.model

import kotlinx.serialization.Serializable

@Serializable
data class TailoredResume(
    val name: String,
    val contactInfo: String,
    val summary: String,
    val experience: List<WorkHistory>,
    val projects: List<Project>,
    val education: List<Education>,
    val skills: List<String>
)

@Serializable
data class WorkHistory(
    val company: String,
    val role: String,
    val duration: String,
    val location: String,
    val bulletPoints: List<String>
)

// New Classes
@Serializable
data class Project(
    val title: String,
    val technologies: String,
    val bulletPoints: List<String>
)

@Serializable
data class Education(
    val school: String,
    val degree: String,
    val year: String
)