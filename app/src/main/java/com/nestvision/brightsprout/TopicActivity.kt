package com.nestvision.brightsprout

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TopicActivity : FragmentActivity() {
    private lateinit var geminiService: GeminiApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic)

        val classNumber = intent.getStringExtra("class_id") ?: "1"
        val subjectName = intent.getStringExtra("subject_name") ?: ""

        geminiService = GeminiApiService(this)

        // Update UI
        val classSubjectInfo = findViewById<TextView>(R.id.class_subject_info)
        classSubjectInfo.text = "Class $classNumber - $subjectName"

        val recyclerView = findViewById<RecyclerView>(R.id.topic_recycler_view)
        val loadingText = findViewById<TextView>(R.id.loading_text)

        recyclerView.layoutManager = GridLayoutManager(this, 1) // 3 columns for topics

        val adapter = TileAdapter<Topic>(emptyList(), { it.title }) { topic ->
            // CHANGED: Navigate to SubtopicActivity instead of ContentActivity
            val intent = Intent(this, SubtopicActivity::class.java)
            intent.putExtra("class_id", classNumber)
            intent.putExtra("subject_name", subjectName)
            intent.putExtra("topic_title", topic.title)
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        // Load topics from Gemini API
        geminiService.getTopics(classNumber, subjectName) { topics ->
            runOnUiThread {
                loadingText.visibility = TextView.GONE
                adapter.updateItems(topics)
                recyclerView.requestFocus() // Set focus for TV navigation
            }
        }
    }
}