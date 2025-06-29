package com.nestvision.brightsprout

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.class_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, 1)

        val classList = listOf(
            // School Classes
            "Class 1", "Class 2", "Class 3", "Class 4", "Class 5", "Class 6",
            "Class 7", "Class 8", "Class 9", "Class 10", "Class 11", "Class 12",
            // Engineering (BTech / BE)
            "BTech Computer Science", "BTech Electronics & Communication", "BTech Electrical Engineering",
            "BTech Mechanical Engineering", "BTech Civil Engineering", "BTech Information Technology",
            "BTech Artificial Intelligence", "BTech Data Science", "BE Instrumentation Engineering",
            // Arts, Science, Commerce (UG)
            "BA English", "BA Economics", "BA History", "BA Political Science", "BA Psychology",
            "BSc Physics", "BSc Chemistry", "BSc Mathematics", "BSc Computer Science", "BSc Zoology",
            "BSc Botany", "BSc Biotechnology", "BSc Microbiology", "BSc Environmental Science",
            "BCom General", "BCom Finance", "BCom Computer Applications",
            // Management, CS, Law
            "BBA", "BBM", "BMS", "BCA", "LLB",
            // PG Engineering/Science
            "MTech Computer Science", "MTech VLSI", "MTech Structural Engineering",
            "MSc Physics", "MSc Chemistry", "MSc Mathematics", "MSc Data Science",
            // PG Arts, Management, Law
            "MA English", "MA Economics", "MA Psychology", "MCom", "MBA", "MCA", "LLM",
            // Competitive Exams
            "NEET", "JEE", "GATE", "UPSC", "CAT", "NET", "CA Foundation", "CS Executive", "CLAT",
            // Medical UG
            "MBBS", "BDS", "BAMS", "BHMS", "BUMS", "BSMS",
            "BSc Nursing", "BPT (Physiotherapy)", "BPharm", "BMLT (Medical Lab Technology)", "BOT (Occupational Therapy)",
            "BSc Radiology", "BSc Anesthesia Technology", "BSc Optometry", "BSc Dialysis Technology",
            // Medical PG
            "MD General Medicine", "MD Pediatrics", "MD Dermatology", "MD Radiology", "MD Psychiatry",
            "MS General Surgery", "MS Orthopedics", "MS ENT", "MS Ophthalmology",
            "MDS (Dental Surgery)", "MPharm", "MPT (Physiotherapy)", "MSc Nursing", "MSc Medical Biochemistry",
            // Allied Health Diplomas
            "Diploma in Nursing", "Diploma in Medical Lab Technology", "Diploma in Radiology", "Diploma in Pharmacy"
        )

        val classes = classList.map { name ->
            ClassItem(id = name)
        }

        val adapter = TileAdapter(classes, { it.id }) { classItem ->
            val intent = Intent(this, SubjectActivity::class.java)
            intent.putExtra("class_id", classItem.id)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
        recyclerView.requestFocus()

        // Settings button handler → Launch dedicated activity
        findViewById<TextView>(R.id.settings_button).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
