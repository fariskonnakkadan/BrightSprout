# 🌱 Brightsprout

Brightsprout is an Android TV and mobile-compatible educational app that dynamically generates subject-wise learning modules using the Gemini API by Google. It provides a clean, scalable, and intuitive interface for students from **Class 1 to 12**, and supports **dynamic topic/subtopic expansion**, **interactive tiles**, and **streamlined Gemini model integration**.

---

## 📱 Features

- 📚 Auto-generated learning content for **all classes (1–12)**
- 🧠 Integrates with **Gemini API (Pro, Flash 2.0, etc.)**
- 📋 Dynamic UI for:
  - Subject → Topic → Subtopic navigation
  - Adaptive tile sizing based on text length
- 📺 Works on Android TV & Phone
- 🔧 Gemini API key & model are configurable from the app settings
- 🎯 Layout adapts across screen sizes with autosized text views

---

## 🧪 Screens Overview

- **Subject Screen** – Lists subjects for selected class
- **Topic Screen** – Lists topics under the subject
- **Subtopic Screen** – Fetches content for the selected topic from Gemini
- **Settings Screen** – Lets user manage API keys, choose a Gemini model (e.g., `gemini-pro`, `gemini-1.5-flash`)

---

## ⚙️ How to Set Up & Use

### 1. 🔑 Get a Gemini API Key
- Visit [https://makersuite.google.com/app/apikey](https://makersuite.google.com/app/apikey)
- Copy your API key
- Recommended model: `gemini-1.5-flash-latest` (low latency, fast inference)

### 2. 🧠 Configure in App
- Open the app and go to the ⚙️ **Settings** screen
- Tap **Add Key**
- Provide:
  - **Key Name**: A label to identify the key (e.g., `My Flash Key`)
  - **API Key**: The actual Gemini key
- After saving, select the key from the dropdown
- Choose a Gemini model from the list (`gemini-pro`, `gemini-1.5-flash-latest`, etc.)

> ✅ Tip: You can save **multiple keys** and switch between them anytime.

---

## 🧱 UI Details

- ✅ All text views auto-resize to fit content
- ✅ Cards/tile heights auto-adjust based on content (no overflow)
- ✅ Grid layout with `spanCount = 1` ensures one tile per row on phone/tablet
- 🧩 Content tiles are scrollable, focusable (TV) and clickable
- ✨ Dynamic layout updates on class selection without app reload

---

## 📦 Tech Stack

- Kotlin + Android SDK
- Gemini API (via HTTP POST)
- `SharedPreferences` for storing key/model selection
- OkHttp for API communication
- CardView + RecyclerView + GridLayout
- Compatible with Android API 21+

---

## 🧰 Developer Notes

### File Structure
- `SettingsActivity.kt`: Handles Gemini API key storage & model selection
- `GeminiApiService.kt`: Manages Gemini requests (prompt → content)
- `MainActivity.kt`: Entry point and navigation
- `TileAdapters.kt`: Handles subject/topic/subtopic list rendering

### Storage Location
- SharedPrefs file: `gemini_prefs`
  - Key: `api_key` – stores selected API key
  - Key: `model_name` – stores selected model name

---

## 🚀 Best Practices

| Task                          | Recommendation                          |
|-------------------------------|------------------------------------------|
| API Model                     | `gemini-1.5-flash-latest` (fast + cheap) |
| API Key Storage               | Done in-app via Settings screen          |
| UI Performance                | Avoid fixed heights, allow wrap_content  |
| Android TV Compatibility      | All screens keyboard/focus-aware         |
| Font Handling                 | Uses `autoSizeTextType="uniform"`        |

---

## 📸 Screenshots

_Coming soon..._

---

## 🤝 Contributions

Pull requests are welcome! If you’d like to contribute (e.g., add class 13+, support image generation, or add audio), feel free to fork and submit PRs.

---

## 📄 License

MIT License © 2025 Faris Mohammed

---

## 🙏 Credits

- [Google Gemini API](https://ai.google.dev/)
- Android Dev Community
