package com.nestvision.brightsprout

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
// import androidx.appcompat.app.AppCompatActivity // Remove this import
import androidx.fragment.app.FragmentActivity // Add this import
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Assuming Subject and GeminiApiService are defined elsewhere
// e.g., data class Subject(val name: String, /* other properties */)
// class GeminiApiService { /* ... */ }
// Assuming TileAdapter is also defined elsewhere


class SubjectActivity : FragmentActivity() { // To this line
    private lateinit var geminiService: GeminiApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject)

        val classNumber = intent.getStringExtra("class_id") ?: "1"
        geminiService = GeminiApiService(this)

        val recyclerView = findViewById<RecyclerView>(R.id.subject_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, 1)
        val loadingText = findViewById<TextView>(R.id.loading_text)

        // Make sure your TileAdapter is compatible with FragmentActivity context if it had specific AppCompat dependencies.
        // Generally, it should be fine.
        val adapter = TileAdapter<Subject>(emptyList(), { it.name }) { subject ->
            val intent = Intent(this, TopicActivity::class.java)
            intent.putExtra("class_id", classNumber)
            intent.putExtra("subject_name", subject.name)
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        // Load subjects from Gemini API
        geminiService.getSubjects(classNumber) { subjects ->
            runOnUiThread {
                loadingText.visibility = TextView.GONE
                adapter.updateItems(subjects)
                // Consider setting initial focus here for TV navigation after items are loaded
                // For example, if the adapter is not empty:
                // if (subjects.isNotEmpty()) {
                //    recyclerView.requestFocus()
                // }
            }
        }
    }
}