package com.nestvision.brightsprout

import android.content.Context
import org.json.JSONObject

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import com.google.gson.Gson
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiApiService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    private fun getUserConfiguredApiKey(): String {
        val prefs = context.getSharedPreferences("gemini_config", Context.MODE_PRIVATE)
        val selectedKeyName = prefs.getString("selected_key_name", null)
        val keysJson = prefs.getString("keys", "{}")
        return try {
            val keyMap = JSONObject(keysJson ?: "{}")
            selectedKeyName?.let { keyMap.optString(it, "") } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun getUserSelectedModel(): String {
        val prefs = context.getSharedPreferences("gemini_config", Context.MODE_PRIVATE)
        return prefs.getString("model", "gemini-pro") ?: "gemini-pro"
    }

    private fun getApiUrl(): String {
        val model = getUserSelectedModel()
        return "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
    }

    data class GeminiRequest(
        val contents: List<Content>,
        val generationConfig: GenerationConfig? = null
    )

    data class GenerationConfig(
        val responseMimeType: String,
        val responseSchema: ResponseSchema
    )

    data class ResponseSchema(
        val type: String,
        val items: SchemaItems? = null,
        val properties: Map<String, SchemaProperty>? = null
    )

    data class SchemaItems(
        val type: String,
        val properties: Map<String, SchemaProperty>
    )

    data class SchemaProperty(
        val type: String,
        val description: String? = null
    )

    data class Content(
        val parts: List<Part>
    )

    data class Part(
        val text: String
    )

    data class GeminiResponse(
        val candidates: List<Candidate>
    )

    data class Candidate(
        val content: Content
    )

    data class SubjectResponse(val name: String, val description: String)
    data class TopicResponse(val name: String, val description: String)
    data class SubtopicResponse(val name: String, val description: String)

    private fun makeGeminiRequest(prompt: String, callback: (String?) -> Unit) {
        val requestBody = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(prompt))))
        )

        val json = gson.toJson(requestBody)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = RequestBody.create(mediaType, json)

        val request = Request.Builder()
            .url("${getApiUrl()}?key=${getUserConfiguredApiKey()}")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        println("🔹 [GeminiAPI] Prompt:\n$prompt")
        println("🔹 [GeminiAPI] Request JSON: $json")
        Log.d("GeminiAPI", "Request JSON: $json")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("GeminiAPI", "API call failed", e)
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { resp ->
                    if (resp.isSuccessful) {
                        val responseBody = resp.body?.string()
                        Log.d("GeminiAPI", "Raw response: $responseBody")

                        try {
                            val geminiResponse = gson.fromJson(responseBody, GeminiResponse::class.java)
                            val text = geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                            Log.d("GeminiAPI", "Parsed response text: $text")
                            callback(text)
                        } catch (e: Exception) {
                            Log.e("GeminiAPI", "Parsing error", e)
                            callback(null)
                        }
                    } else {
                        Log.e("GeminiAPI", "API error: ${resp.code} - ${resp.message}")
                        callback(null)
                    }
                }
            }
        })
    }

    fun getSubjects(classNumber: String, callback: (List<Subject>) -> Unit) {
        val classNumberNumeric = classNumber.removePrefix("Class ").toIntOrNull()
        val isSchoolClass = classNumberNumeric != null && classNumberNumeric in 1..12
        val educationLevel = classNumber.trim()

        val ageNote = if (isSchoolClass) " (age approximately ${classNumberNumeric!! + 5} years)" else ""

        val syllabusNote = if (isSchoolClass)
            "following the CBSE/NCERT curriculum"
        else
            "following standard Indian university-level curriculum for $educationLevel"

        val prompt = """
Generate a list of subjects for $educationLevel $syllabusNote.$ageNote

Respond with just the subject names, one per line. Do not include any numbering, bullet points, or descriptions.
    """.trimIndent()

        makeGeminiRequest(prompt) { response ->
            response?.let { textResponse ->
                try {
                    val subjects = textResponse
                        .lines()
                        .mapNotNull { line ->
                            val name = line.trim()
                            if (name.isNotEmpty()) Subject(name, "") else null
                        }

                    if (subjects.size >= 6) {
                        callback(subjects)
                    } else {
                        getDefaultSubjects(classNumber, callback)
                    }
                } catch (e: Exception) {
                    getDefaultSubjects(classNumber, callback)
                }
            } ?: run {
                getDefaultSubjects(classNumber, callback)
            }
        }
    }




    fun getTopics(classNumber: String, subject: String, callback: (List<Topic>) -> Unit) {
        val classNumberNumeric = classNumber.removePrefix("Class ").toIntOrNull()
        val isSchoolClass = classNumberNumeric != null && classNumberNumeric in 1..12
        val educationLevel = classNumber.trim()

        val syllabusNote = if (isSchoolClass)
            "based on the CBSE/NCERT curriculum"
        else
            "based on standard Indian university-level curriculum for $educationLevel"

        val prompt = """
Generate a list of main topics for the subject "$subject" suitable for $educationLevel $syllabusNote.

Only include the topic names, one per line. Do not include descriptions, numbering, or bullet points.

Ensure the topics:
- Follow a logical order from basic to advanced
- Are age-appropriate for $educationLevel students
- Cover the full syllabus for the subject "$subject"
    """.trimIndent()

        makeGeminiRequest(prompt) { response ->
            response?.let { textResponse ->
                try {
                    val topics = textResponse
                        .lines()
                        .mapNotNull { line ->
                            val name = line.trim()
                            if (name.isNotEmpty()) Topic(name, "") else null
                        }

                    if (topics.size >= 6) {
                        callback(topics)
                    } else {
                        getDefaultTopics(classNumber, subject, callback)
                    }
                } catch (e: Exception) {
                    getDefaultTopics(classNumber, subject, callback)
                }
            } ?: run {
                getDefaultTopics(classNumber, subject, callback)
            }
        }
    }



    fun getSubtopics(classNumber: String, subject: String, topic: String, callback: (List<Subtopic>) -> Unit) {
        val classNumberNumeric = classNumber.removePrefix("Class ").toIntOrNull()
        val isSchoolClass = classNumberNumeric != null && classNumberNumeric in 1..12
        val educationLevel = classNumber.trim()

        val syllabusNote = if (isSchoolClass)
            "following the CBSE/NCERT curriculum"
        else
            "following standard Indian university-level curriculum for $educationLevel"

        val prompt = """
Break down the topic "$topic" from the subject "$subject" for $educationLevel students, $syllabusNote.

Only include subtopic names, one per line. Do not include any descriptions, numbering, or bullet points.

Ensure the subtopics:
- Progress from basic to advanced concepts
- Are clearly titled so students understand the focus
- Cover both theory and practice
- Fully represent the depth of the topic "$topic" for $educationLevel
    """.trimIndent()

        makeGeminiRequest(prompt) { response ->
            response?.let { textResponse ->
                try {
                    val subtopics = textResponse
                        .lines()
                        .mapNotNull { line ->
                            val name = line.trim()
                            if (name.isNotEmpty()) Subtopic(name, "") else null
                        }

                    if (subtopics.size >= 4) {
                        callback(subtopics)
                    } else {
                        getDefaultSubtopics(classNumber, subject, topic, callback)
                    }
                } catch (e: Exception) {
                    getDefaultSubtopics(classNumber, subject, topic, callback)
                }
            } ?: run {
                getDefaultSubtopics(classNumber, subject, topic, callback)
            }
        }
    }


    fun getSubtopicContent(
        classNumber: String,
        subject: String,
        topic: String,
        subtopic: String,
        callback: (TopicContent) -> Unit
    ) {
        val classNumberNumeric = classNumber.removePrefix("Class ").toIntOrNull()
        val isSchoolClass = classNumberNumeric != null && classNumberNumeric in 1..12
        val educationLevel = classNumber.trim()

        val syllabusNote = if (isSchoolClass)
            "based on the CBSE/NCERT curriculum"
        else
            "based on a standard Indian university-level curriculum for $educationLevel"

        val prompt = """
Create detailed and engaging learning material for $educationLevel students, $syllabusNote.

Subject: $subject  
Topic: $topic  
Subtopic: $subtopic  

Ensure the content:
- Explains the concepts clearly from basics to advanced
- Includes examples, applications, and review points
- Is suitable for the learning level of $educationLevel students
- Covers both theoretical and practical understanding
    """.trimIndent()

        makeGeminiRequest(prompt) { response ->
            response?.let { content ->
                val topicContent = TopicContent(subtopic, content)
                callback(topicContent)
            } ?: run {
                // Fallback content
                val fallbackContent = TopicContent(
                    subtopic,
                    "Detailed content for '$subtopic' under '$topic' in $subject for $educationLevel.\n\n" +
                            "This comprehensive lesson will help you understand:\n" +
                            "• Core concepts and definitions\n" +
                            "• Step-by-step examples and solutions\n" +
                            "• Real-world applications\n" +
                            "• Practice problems to test your understanding\n\n" +
                            "The content is specifically designed for $educationLevel students to make learning engaging and effective."
                )
                callback(fallbackContent)
            }
        }
    }


    // Text parsing helper methods
    private fun parseSubjectsFromText(text: String): List<Subject> {
        return text.trim().split("\n")
            .filter { it.isNotBlank() && it.contains("|") }
            .map { line ->
                val parts = line.split("|", limit = 2)
                if (parts.size == 2) {
                    Subject(parts[0].trim(), parts[1].trim())
                } else {
                    Subject(parts[0].trim(), "")
                }
            }
    }

    private fun parseTopicsFromText(text: String): List<Topic> {
        return text.trim().split("\n")
            .filter { it.isNotBlank() && it.contains("|") }
            .map { line ->
                val parts = line.split("|", limit = 2)
                if (parts.size == 2) {
                    Topic(parts[0].trim(), parts[1].trim())
                } else {
                    Topic(parts[0].trim(), "")
                }
            }
    }

    private fun parseSubtopicsFromText(text: String): List<Subtopic> {
        return text.trim().split("\n")
            .filter { it.isNotBlank() && it.contains("|") }
            .map { line ->
                val parts = line.split("|", limit = 2)
                if (parts.size == 2) {
                    Subtopic(parts[0].trim(), parts[1].trim())
                } else {
                    Subtopic(parts[0].trim(), "")
                }
            }
    }

    // Keep original fallback methods unchanged
    private fun getDefaultSubjects(classNumber: String, callback: (List<Subject>) -> Unit) {
        val subjects = listOf(
            Subject("Mathematics", "Learn numbers, algebra, geometry and develop problem-solving skills essential for daily life and future studies."),
            Subject("Science", "Explore physics, chemistry and biology to understand the natural world and scientific principles around us."),
            Subject("English", "Develop reading, writing and communication skills through literature, grammar and creative expression."),
            Subject("Social Studies", "Study history, geography and civics to understand our society, culture and the world we live in."),
            Subject("Hindi", "Learn Hindi language, literature and develop communication skills in our national language."),
            Subject("Computer Science", "Introduction to computers, basic programming and digital literacy for the modern world."),
            Subject("Art & Craft", "Express creativity through drawing, painting and various art forms while developing artistic skills."),
            Subject("Physical Education", "Stay healthy and fit through sports, exercises and learn the importance of physical wellness.")
        )
        callback(subjects)
    }

    private fun getDefaultTopics(classNumber: String, subject: String, callback: (List<Topic>) -> Unit) {
        val topics = listOf(
            Topic("Introduction to $subject", "Basic concepts and fundamental principles of $subject for Class $classNumber students."),
            Topic("Core Concepts", "Essential topics that form the foundation of $subject learning."),
            Topic("Practical Applications", "Real-world uses and applications of $subject in daily life."),
            Topic("Problem Solving", "Techniques and methods for solving problems related to $subject."),
            Topic("Advanced Topics", "More challenging concepts to deepen understanding of $subject."),
            Topic("Review and Practice", "Comprehensive review and practice exercises for better understanding.")
        )
        callback(topics)
    }

    private fun getDefaultSubtopics(classNumber: String, subject: String, topic: String, callback: (List<Subtopic>) -> Unit) {
        val subtopics = listOf(
            Subtopic("Introduction and Overview", "Basic introduction to the main concepts of $topic."),
            Subtopic("Key Concepts and Definitions", "Important terms and fundamental ideas you need to know."),
            Subtopic("Step-by-Step Examples", "Detailed examples with complete solutions and explanations."),
            Subtopic("Practice and Application", "Hands-on practice problems to apply what you've learned."),
            Subtopic("Real-World Connections", "How this topic relates to everyday life and practical situations."),
            Subtopic("Summary and Review", "Key points review and preparation for assessment.")
        )
        callback(subtopics)
    }
}