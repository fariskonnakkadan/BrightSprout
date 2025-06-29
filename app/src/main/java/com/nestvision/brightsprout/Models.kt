package com.nestvision.brightsprout

data class ClassItem(
    val id: String,
)

data class Subject(
    val name: String,
    val description: String
)

data class Topic(
    val title: String,
    val description: String
)

// NEW: Add Subtopic model
data class Subtopic(
    val title: String,
    val description: String
)

data class TopicContent(
    val title: String,
    val content: String,
    val examples: List<String> = emptyList()
)