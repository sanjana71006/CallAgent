from doc_builder.core import (
    add_h1, add_h2, add_h3, add_p, add_bullet,
    add_callout, add_code_block, add_table
)

def build_sec19_to_27(doc):
    # 19. CALL FLOW
    add_h1(doc, "19. CELLULAR CALL FLOW & TELECOM SCREENING")
    add_p(
        doc,
        "CallMate AI models call state transitions using an event-driven state machine managed by SimulatorCallProvider and CallViewModel:"
    )
    add_bullet(doc, "State 1: INCOMING", "Call banner pops up displaying caller name, phone number, and screening options.")
    add_bullet(doc, "State 2: SCREENED (AI Active)", "AI assistant answers with synthesized voice; live transcription displays dialogue bubbles.")
    add_bullet(doc, "State 3: TAKEN_OVER", "User taps 'Take Call', muting AI synthesis and opening direct audio communications.")
    add_bullet(doc, "State 4: COMPLETED / SUMMARIZED", "Call terminates, triggering post-call summarization and Room database insertion.")

    add_callout(
        doc,
        "AI call answering on real carrier cellular calls is planned / not yet implemented. Real cellular calls currently bypass the app without the Telecom Framework integration.",
        title="CARRIER TELEPHONY NOTICE",
        status="info"
    )

    # 20. AI FUNCTIONALITY
    add_h1(doc, "20. AI SCREENING ENGINE & LLM PROCESSING")
    add_p(
        doc,
        "The AI engine operates via a Python FastAPI service located in backend/app/ with a dual-provider architecture:"
    )
    add_bullet(doc, "Ollama Local LLM Provider:", "Connects to a locally running Ollama daemon (http://localhost:11434) using the lightweight qwen2.5:0.5b model for low-latency inference.")
    add_bullet(doc, "Mock AI Provider (Resilient Fallback):", "If Ollama is offline or degraded, the system automatically falls back to MockAIProvider to generate realistic conversational screening turns.")
    add_bullet(doc, "Text-to-Speech (TTS):", "Integrated via Android's native TextToSpeech engine in TextToSpeechManager.kt.")
    add_bullet(doc, "Speech-to-Text (STT):", "Integrated via Android's SpeechRecognizer in SpeechToTextManager.kt.")

    # 21. OFFLINE FUNCTIONALITY
    add_h1(doc, "21. OFFLINE CAPABILITIES & STORAGE MATRIX")
    add_p(doc, "CallMate AI is built on an offline-first foundation. The matrix below specifies internet requirements for every feature:")
    add_table(
        doc,
        [2.2, 1.4, 2.9],
        ["Feature / Subsystem", "Offline State", "Internet Dependency Rationale"],
        [
            ["Local Call History", "YES", "Persisted in local Room SQLite database"],
            ["Verbatim Transcripts", "YES", "Persisted in local Room SQLite database"],
            ["Addresses Management", "YES", "Full CRUD operations operate 100% locally in Room"],
            ["Assistant Instructions", "YES", "Screening rules stored locally in assistant_instructions"],
            ["Voice Pitch & Speed Settings", "YES", "Stored in Room and applied via on-device Android TTS"],
            ["Silent Mode Switches", "YES", "Heuristic rules evaluated on-device without cloud"],
            ["User Registration", "NO", "Requires connection to Node.js backend & MongoDB Atlas"],
            ["User Login", "NO", "Requires password verification against MongoDB Atlas"],
            ["Cloud Account Deletion", "NO", "Requires DELETE /api/users/me on cloud backend"],
            ["AI Screening (Ollama Local)", "YES", "Operates offline if Ollama server is hosted on device/LAN"],
            ["AI Screening (Cloud LLM)", "NO", "Requires network connection if cloud AI API is configured"]
        ]
    )

    # 22. DATA FLOW DIAGRAMS
    add_h1(doc, "22. COMPLETE SYSTEM DATA FLOW DIAGRAMS")
    add_p(doc, "Below are architectural data flow diagrams for core system events:")
    add_h2(doc, "22.1 Registration & Account Creation Flow")
    add_code_block(
        doc,
"""User Inputs [Name, Email, Phone, Password] 
       ---> [RegisterScreen.kt]
       ---> [AuthViewModel.kt]
       ---> [AuthRepositoryImpl.kt]
       ---> [Retrofit POST /api/auth/register]
       ---> [Express authController.js]
       ---> [bcrypt Salt & Hash]
       ---> [MongoDB Atlas users.insertOne()]
       ---> [JWT Token Minted]
       ---> [Android TokenManager.saveToken()]
       ---> [Room user_profile.insert()]"""
    )

    add_h2(doc, "22.2 Call Screening & Post-Call Summarization Flow")
    add_code_block(
        doc,
"""Incoming Call Triggered 
       ---> [SimulatorCallProvider.startIncomingCall()]
       ---> [IncomingCallScreen.kt: 'Let AI Screen']
       ---> [LiveCallScreen.kt Audio Loop]
       ---> [Retrofit POST /api/v1/ai/chat]
       ---> [FastAPI ai_service.py -> Ollama/Mock]
       ---> [TextToSpeechManager.speakText()]
       ---> [Call Ends -> POST /api/v1/ai/summarize]
       ---> [CallDao.insertCall() + TranscriptDao.insertTranscripts()]
       ---> [HomeScreen.kt (Chats List Updated)]"""
    )

    # 23. PROJECT DIRECTORY STRUCTURE
    add_h1(doc, "23. PROJECT DIRECTORY STRUCTURE & ORGANIZATION")
    add_p(doc, "The CallMate AI codebase is cleanly structured across Android client and backend modules:")
    add_code_block(
        doc,
"""CallMate AI/
├── android/                             # Android Client Project
│   ├── app/
│   │   ├── build.gradle.kts             # Android build configuration & dependencies
│   │   └── src/main/
│   │       ├── AndroidManifest.xml      # App permissions and activity declarations
│   │       ├── java/com/callmate/ai/
│   │       │   ├── CallMateApp.kt       # Application class (DB initialization)
│   │       │   ├── MainActivity.kt      # Main entry point activity
│   │       │   ├── core/                # Audio, telephony, and Compose themes
│   │       │   ├── data/
│   │       │   │   ├── local/           # Room Database, 11 Entities, 6 DAOs
│   │       │   │   ├── remote/          # Retrofit API services and DTOs
│   │       │   │   └── repository/      # Repository implementations
│   │       │   ├── domain/              # Domain models and business interfaces
│   │       │   └── presentation/        # Jetpack Compose UI screens and ViewModels
│   │       └── res/                     # Strings, drawables, launcher icons
├── backend/                             # Backend Services
│   ├── src/                             # Node.js Express Authentication Backend
│   │   ├── config/                      # MongoDB Atlas connection (database.js)
│   │   ├── controllers/                 # authController.js, userController.js
│   │   ├── middleware/                  # authMiddleware.js (JWT validation)
│   │   ├── models/                      # User.js (Mongoose User Schema)
│   │   ├── routes/                      # authRoutes.js, userRoutes.js
│   │   ├── services/                    # authService.js, userStore.js
│   │   ├── tests/                       # test_server.js (17 integration tests)
│   │   ├── package.json                 # Node dependencies and test scripts
│   │   └── .env                         # Server configuration & MongoDB URI
│   └── app/                             # Python FastAPI AI Screening Backend
│       ├── api/v1/                      # Chat, classify, summarize endpoints
│       ├── core/                        # Settings & logging configuration
│       ├── services/                    # Ollama & Mock AI providers
│       ├── static/                      # Web simulator (index.html)
│       └── main.py                      # FastAPI application entry point
└── README.md                            # Project overview and quick start"""
    )

    # 24. IMPORTANT FILES
    add_h1(doc, "24. IMPORTANT SOURCE CODE FILES DIRECTORY")
    add_p(doc, "The table below catalogs the most critical source code files across the system:")
    add_table(
        doc,
        [2.2, 1.8, 2.5],
        ["File Path", "Subsystem", "Core Responsibilities & Important Symbols"],
        [
            ["CallMateDatabase.kt", "Android Database", "Room database definition with 11 entities, fallback migrations"],
            ["NavGraph.kt", "Android Navigation", "Compose Navigation router handling Auth and Main tab graphs"],
            ["AuthViewModel.kt", "Android Auth", "Manages AuthState (Loading, Authenticated, Unauthenticated)"],
            ["SettingsViewModel.kt", "Android Settings", "Binds Room local settings flows to Compose UI"],
            ["SimulatorCallProvider.kt", "Android Telephony", "Simulates incoming calls, state transitions, and audio screening"],
            ["server.js", "Node.js Backend", "Express server setup, CORS configuration, route mounting"],
            ["database.js", "Node.js Config", "Mongoose connection to MongoDB Atlas with offline resilience"],
            ["authController.js", "Node.js Controller", "register(), login(), logout(), getMe() implementations"],
            ["userStore.js", "Node.js Service", "Unified persistence layer mediating between Atlas and local cache"],
            ["User.js", "Node.js Model", "Mongoose schema with bcrypt password hashing pre-save hooks"],
            ["ai_service.py", "FastAPI Service", "Coordinates prompt execution against Ollama and Mock providers"],
            ["index.html", "Web Simulator", "Interactive phone simulator previewing full app in desktop browsers"]
        ]
    )

    # 25. DEPENDENCIES
    add_h1(doc, "25. DEPENDENCIES & LIBRARY MANIFEST")
    add_p(doc, "The table below lists all primary third-party libraries and runtime dependencies:")
    add_table(
        doc,
        [1.8, 1.2, 1.2, 2.3],
        ["Library Name", "Version", "Subsystem", "Purpose"],
        [
            ["androidx.compose.bom", "2024.09.00", "Android", "Jetpack Compose bill of materials for consistent UI versions"],
            ["androidx.room", "2.6.1", "Android", "Room SQLite ORM and KSP annotation processor"],
            ["androidx.navigation", "2.8.0", "Android", "Jetpack Compose Navigation component"],
            ["com.squareup.retrofit2", "2.11.0", "Android", "REST client and Gson converter for HTTP networking"],
            ["com.squareup.okhttp3", "4.12.0", "Android", "HTTP client engine and logging interceptor"],
            ["express", "4.19.2", "Node.js", "Web framework for authentication endpoints"],
            ["mongoose", "8.5.1", "Node.js", "MongoDB object modeling for Node.js"],
            ["jsonwebtoken", "9.0.2", "Node.js", "JSON Web Token creation and verification"],
            ["bcryptjs", "2.4.3", "Node.js", "Password salting and hashing library"],
            ["fastapi", "0.111.0", "Python", "High-performance asynchronous Python web framework"],
            ["uvicorn", "0.30.1", "Python", "Lightning-fast ASGI server implementation"]
        ]
    )

    # 26. ANDROID PERMISSIONS
    add_h1(doc, "26. ANDROID SYSTEM PERMISSIONS ANALYSIS")
    add_p(doc, "The table below details all permissions declared in AndroidManifest.xml and their operational justification:")
    add_table(
        doc,
        [1.8, 1.2, 3.5],
        ["Permission Name", "Type", "Operational Justification & Sensitivity"],
        [
            ["android.permission.RECORD_AUDIO", "Dangerous", "Required to capture user voice during live screening audio interaction"],
            ["android.permission.INTERNET", "Normal", "Required for API communication with cloud backend and AI engine"],
            ["android.permission.POST_NOTIFICATIONS", "Dangerous", "Required on Android 13+ (API 33+) to alert user of screened calls"],
            ["READ_PHONE_STATE (Removed)", "Excluded", "Explicitly removed via tools:node='remove' to preserve user privacy"]
        ]
    )

    # 27. SETUP GUIDE
    add_h1(doc, "27. COMPREHENSIVE DEVELOPER SETUP GUIDE")
    add_p(doc, "Follow the exact steps below to set up and run the entire CallMate AI environment on a developer machine:")

    add_h2(doc, "27.1 Prerequisites")
    add_bullet(doc, "Java Development Kit:", "JDK 17 or higher")
    add_bullet(doc, "Android Studio:", "Android Studio Ladybug (2024.2+) or newer with Android SDK 35")
    add_bullet(doc, "Node.js:", "Node.js v18.0.0 or higher with npm")
    add_bullet(doc, "Python:", "Python 3.10 or higher with pip")

    add_h2(doc, "27.2 Backend Installation & Startup")
    add_code_block(
        doc,
"""# 1. Open Terminal and navigate to backend directory
cd "backend"

# 2. Install Node.js dependencies
npm install

# 3. Create .env file with your MongoDB Atlas URI
# MONGODB_URI=mongodb+srv://<user>:<password>@cluster0.kukhjf4.mongodb.net/callmate_ai
# JWT_SECRET=callmate_ai_jwt_dev_secret_2026
# PORT=5000

# 4. Start Node.js Express server
npm start
# Server listens on http://localhost:5000

# 5. In a second terminal, install Python dependencies and start FastAPI server
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
# AI Engine listens on http://localhost:8000"""
    )

    add_h2(doc, "27.3 Android Application Compilation & Launch")
    add_code_block(
        doc,
"""# 1. Open project in Android Studio or terminal
cd "android"

# 2. Compile debug build
./gradlew assembleDebug

# 3. Install onto running emulator or USB-connected device
./gradlew installDebug

# 4. Launch main activity
adb shell am start -n com.callmate.ai/.MainActivity"""
    )
