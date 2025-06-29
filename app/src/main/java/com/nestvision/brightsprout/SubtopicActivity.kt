package com.nestvision.brightsprout

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SubtopicActivity : FragmentActivity() {
    private lateinit var geminiService: GeminiApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subtopic)

        val classNumber = intent.getStringExtra("class_id") ?: "1"
        val subjectName = intent.getStringExtra("subject_name") ?: ""
        val topicTitle = intent.getStringExtra("topic_title") ?: ""

        geminiService = GeminiApiService(this)

        // Update UI
        val classSubjectTopicInfo = findViewById<TextView>(R.id.class_subject_topic_info)
        classSubjectTopicInfo.text = "Class $classNumber - $subjectName - $topicTitle"

        val recyclerView = findViewById<RecyclerView>(R.id.subtopic_recycler_view)
        val loadingText = findViewById<TextView>(R.id.loading_text)

        recyclerView.layoutManager = GridLayoutManager(this, 1) // 3 columns for subtopics

        val adapter = TileAdapter<Subtopic>(emptyList(), { it.title }) { subtopic ->
            val intent = Intent(this, ContentActivity::class.java)
            intent.putExtra("class_id", classNumber)
            intent.putExtra("subject_name", subjectName)
            intent.putExtra("topic_title", topicTitle)
            intent.putExtra("subtopic_title", subtopic.title)
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        // Load subtopics from Gemini API
        geminiService.getSubtopics(classNumber, subjectName, topicTitle) { subtopics ->
            runOnUiThread {
                loadingText.visibility = TextView.GONE
                adapter.updateItems(subtopics)
                recyclerView.requestFocus() // Set focus for TV navigation
            }
        }
    }
}