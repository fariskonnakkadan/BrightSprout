package com.nestvision.brightsprout

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin

class ContentActivity : FragmentActivity() {
    private lateinit var geminiService: GeminiApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)

        val classNumber = intent.getStringExtra("class_id") ?: "1"
        val subjectName = intent.getStringExtra("subject_name") ?: ""
        val topicTitle = intent.getStringExtra("topic_title") ?: ""
        val subtopicTitle = intent.getStringExtra("subtopic_title") ?: ""

        // ✅ Pass context here
        geminiService = GeminiApiService(this)

        // UI references
        val topicTitleView = findViewById<TextView>(R.id.topic_title)
        val classSubjectTopicInfo = findViewById<TextView>(R.id.class_subject_topic_info)
        val contentText = findViewById<TextView>(R.id.content_text)
        val loadingLayout = findViewById<LinearLayout>(R.id.loading_layout)
        val backButton = findViewById<Button>(R.id.back_button)

        topicTitleView.text = subtopicTitle
        classSubjectTopicInfo.text = "Class $classNumber - $subjectName - $topicTitle - $subtopicTitle"
        backButton.text = "← Back to Subtopics"
        backButton.setOnClickListener {
            finish()
        }

        // ✅ Enable Table rendering with Markwon plugin
        val markwon = Markwon.builder(this)
            .usePlugin(TablePlugin.create(this)) // table support
            .build()

        // Load and render markdown content
        geminiService.getSubtopicContent(classNumber, subjectName, topicTitle, subtopicTitle) { topicContent ->
            runOnUiThread {
                loadingLayout.visibility = LinearLayout.GONE
                markwon.setMarkdown(contentText, topicContent.content)
                backButton.requestFocus()
            }
        }
    }
}
