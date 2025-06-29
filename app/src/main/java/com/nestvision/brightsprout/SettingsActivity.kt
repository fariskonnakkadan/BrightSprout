package com.nestvision.brightsprout

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.fragment.app.FragmentActivity
import okhttp3.*
import java.io.IOException
import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class SettingsActivity : FragmentActivity() {

    private lateinit var keySpinner: Spinner
    private lateinit var addKeyButton: Button
    private lateinit var deleteKeyButton: Button
    private lateinit var modelSpinner: Spinner
    private lateinit var saveModelButton: Button
    private lateinit var testButton: Button
    private lateinit var keyNameInput: EditText
    private lateinit var apiKeyInput: EditText
    private lateinit var homeButton: Button

    private val models = listOf(
        // Gemini 2.5
        "gemini-2.5-pro",
        "gemini-2.5-flash",
        "gemini-2.5-flash-lite-preview-06-17",
        "gemini-2.5-flash-preview-native-audio-dialog",
        "gemini-2.5-flash-exp-native-audio-thinking-dialog",
        "gemini-2.5-flash-preview-tts",
        "gemini-2.5-pro-preview-tts",
        "gemini-live-2.5-flash-preview",

        // Gemini 2.0
        "gemini-2.0-flash",
        "gemini-2.0-flash-preview-image-generation",
        "gemini-2.0-flash-lite",
        "gemini-2.0-flash-live-001",

        // Gemini 1.5
        "gemini-1.5-flash",
        "gemini-1.5-flash-8b",
        "gemini-1.5-pro",

        // Embedding
        "gemini-embedding-exp",

        // Imagen
        "imagen-4.0-generate-preview-06-06",
        "imagen-4.0-ultra-generate-preview-06-06",
        "imagen-3.0-generate-002",

        // Veo
        "veo-2.0-generate-001"
    )


    private lateinit var prefs: SharedPreferences
    private var apiKeyMap: MutableMap<String, String> = mutableMapOf()
    private var selectedKeyName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        keySpinner = findViewById(R.id.key_spinner)
        addKeyButton = findViewById(R.id.add_key_button)
        deleteKeyButton = findViewById(R.id.delete_key_button)
        modelSpinner = findViewById(R.id.model_spinner)
        saveModelButton = findViewById(R.id.save_model_button)
        testButton = findViewById(R.id.test_button)
        keyNameInput = findViewById(R.id.api_key_name_input)
        apiKeyInput = findViewById(R.id.api_key_input)
        homeButton = findViewById(R.id.home_button)

        homeButton.setOnClickListener {
            finish() // or launch MainActivity if you prefer
        }

        prefs = getSharedPreferences("gemini_config", Context.MODE_PRIVATE)
        loadApiKeys()
        setupModelSpinner()

        addKeyButton.setOnClickListener {
            val name = keyNameInput.text.toString().trim()
            val key = apiKeyInput.text.toString().trim()

            if (name.isNotBlank() && key.isNotBlank()) {
                apiKeyMap[name] = key
                selectedKeyName = name
                saveApiKeys()
                loadApiKeys()
                Toast.makeText(this, "API Key Added", Toast.LENGTH_SHORT).show()
                keyNameInput.text.clear()
                apiKeyInput.text.clear()
            } else {
                Toast.makeText(this, "Both fields are required", Toast.LENGTH_SHORT).show()
            }
        }

        deleteKeyButton.setOnClickListener { deleteSelectedKey() }

        saveModelButton.setOnClickListener {
            val model = modelSpinner.selectedItem.toString()
            prefs.edit().putString("model", model).apply()
            Toast.makeText(this, "Model saved", Toast.LENGTH_SHORT).show()
        }

        testButton.setOnClickListener {
            val key = apiKeyMap[selectedKeyName]
            val model = modelSpinner.selectedItem.toString()
            if (key == null) {
                Toast.makeText(this, "Select a valid API key", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            testApiKey(key, model)
        }
    }

    private fun loadApiKeys() {
        apiKeyMap.clear()
        val raw = prefs.getString("keys", "{}")
        val json = JSONObject(raw ?: "{}")
        for (k in json.keys()) {
            apiKeyMap[k] = json.getString(k)
        }

        selectedKeyName = prefs.getString("selected_key_name", apiKeyMap.keys.firstOrNull())

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, apiKeyMap.keys.toList())
        keySpinner.adapter = adapter
        keySpinner.setSelection(apiKeyMap.keys.indexOf(selectedKeyName).coerceAtLeast(0))
        keySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                selectedKeyName = apiKeyMap.keys.toList()[pos]
                prefs.edit().putString("selected_key_name", selectedKeyName).apply()
            }

            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
    }

    private fun saveApiKeys() {
        val json = JSONObject(apiKeyMap as Map<*, *>)
        prefs.edit().putString("keys", json.toString()).apply()
    }

    private fun deleteSelectedKey() {
        selectedKeyName?.let {
            apiKeyMap.remove(it)
            if (apiKeyMap.isNotEmpty()) {
                selectedKeyName = apiKeyMap.keys.first()
            } else {
                selectedKeyName = null
            }
            saveApiKeys()
            loadApiKeys()
        }
    }

    private fun setupModelSpinner() {
        val savedModel = prefs.getString("model", models[0]) ?: models[0]
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, models)
        modelSpinner.adapter = adapter
        modelSpinner.setSelection(models.indexOf(savedModel).coerceAtLeast(0))
    }

    private fun testApiKey(apiKey: String, model: String) {
        val url =
            "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val json = """{"contents":[{"parts":[{"text":"Say Hello"}]}]}"""

        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), json))
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@SettingsActivity, "Test failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful && responseBody != null) {
                        Toast.makeText(this@SettingsActivity, "Success:\n$responseBody", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(
                            this@SettingsActivity,
                            "Error ${response.code}:\n${responseBody ?: response.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }

}
