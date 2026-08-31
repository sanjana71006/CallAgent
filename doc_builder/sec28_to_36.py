from doc_builder.core import (
    add_h1, add_h2, add_h3, add_p, add_bullet,
    add_callout, add_code_block, add_table
)

def build_sec28_to_36(doc):
    # 28. MONGODB ATLAS SETUP
    add_h1(doc, "28. MONGODB ATLAS CLOUD DATABASE SETUP")
    add_p(doc, "To configure a fresh MongoDB Atlas cloud database instance:")
    add_bullet(doc, "1. Create Atlas Account:", "Sign up at mongodb.com and create a free M0 cluster.")
    add_bullet(doc, "2. Create Database User:", "Create a database user with readWrite privileges.")
    add_bullet(doc, "3. Configure Network Access:", "Add your current IP address (or 0.0.0.0/0 for testing) to the Network Access IP Access List.")
    add_bullet(doc, "4. Obtain Connection URI:", "Copy the mongodb+srv:// connection string.")
    add_bullet(doc, "5. Populate .env File:", "Set MONGODB_URI=mongodb+srv://<user>:<pass>@<cluster>.mongodb.net/callmate_ai in backend/.env.")

    # 29. TESTING CHECKLIST
    add_h1(doc, "29. QUALITY ASSURANCE & TESTING CHECKLIST")
    add_p(doc, "The CallMate AI test suite includes automated integration tests and manual UI verification checklists:")
    add_bullet(doc, "Automated Backend Test Suite (backend/tests/test_server.js):", "17/17 Passing Tests covering health checks, registration validations, bcrypt verification, duplicate email rejection, session isolation, profile updates, and account deletion.")
    add_bullet(doc, "Manual Authentication Checklist:", "Sign up with valid data -> Verified. Duplicate email -> Rejected. Invalid email -> Rejected. Short password -> Rejected. Password mismatch -> Rejected. Sign out -> Redirects to login.")
    add_bullet(doc, "Local Persistence Checklist:", "Add new delivery address -> Persists across app restarts. Toggle silent mode rules -> Persists. Edit custom prompt instructions -> Persists in Room.")
    add_bullet(doc, "Offline Resilience Checklist:", "Disable WiFi/Cellular -> App launches, loads call history, edits addresses, and runs locally without crashing.")

    # 30. ERROR HANDLING
    add_h1(doc, "30. SYSTEM ERROR HANDLING & RESILIENCE")
    add_bullet(doc, "Client Input Validation:", "Immediate field-level error messages in Compose UI preventing malformed requests.")
    add_bullet(doc, "Backend Schema Validation:", "Express controllers validate data formats before database queries.")
    add_bullet(doc, "Database Connection Fallback:", "If MongoDB Atlas connection is interrupted, the Node.js server gracefully falls back to local in-memory store.")
    add_bullet(doc, "AI Engine Fallback:", "If local Ollama LLM is unreachable, FastAPI automatically delegates to MockAIProvider.")

    # 31. LIMITATIONS
    add_h1(doc, "31. HONEST LIMITATIONS & FEATURE CONSTRAINTS")
    add_p(doc, "The table below categorizes the implementation maturity of all system capabilities:")
    add_table(
        doc,
        [2.0, 1.5, 3.0],
        ["System Capability", "Maturity Status", "Engineering Notes & Constraints"],
        [
            ["Local Room Storage", "IMPLEMENTED", "11 SQLite entities fully functional with reactive Flow queries"],
            ["User Auth & Session", "IMPLEMENTED", "Bcrypt hashing, JWT generation, and MongoDB Atlas persistence active"],
            ["Contacts Whitelisting", "IMPLEMENTED", "Queries device ContactsContract to let known contacts ring directly"],
            ["Live GPS & Addresses", "IMPLEMENTED", "Silent GPS capture + OpenStreetMap reverse-geocoding street address"],
            ["Global Spam Registry", "IMPLEMENTED", "Crowdsourced SpamNumber model in MongoDB Atlas with UI Spam filter"],
            ["Gemini AI Screening", "IMPLEMENTED", "Google Gemini 3.6 Flash (gemini-3.6-flash) cloud inference active"],
            ["'You' Settings Hub", "IMPLEMENTED", "All settings screens, address manager, and instructions manager active"],
            ["Cellular CallScreening", "PLANNED", "Real cellular carrier interception via Telecom framework is planned"]
        ]
    )

    # 32. FUTURE DEVELOPMENT
    add_h1(doc, "32. FUTURE DEVELOPMENT & STRATEGIC ROADMAP")
    add_bullet(doc, "Phase 1: Android Telecom Integration:", "Implement Android CallScreeningService to intercept incoming carrier cellular calls.")
    add_bullet(doc, "Phase 2: National Registry Synergies:", "Integrate verified national DND/TRAI registries with real-time reputation lookups.")
    add_bullet(doc, "Phase 3: Multilingual Voice Models:", "Add speech recognition and synthesis support for major regional Indian languages (Hindi, Telugu, Tamil).")
    add_bullet(doc, "Phase 4: Cloud Sync Engine:", "Implement optional end-to-end encrypted backup of call summaries to user cloud storage.")

    # 33. PLAY STORE READINESS
    add_h1(doc, "33. GOOGLE PLAY STORE READINESS ASSESSMENT")
    add_p(doc, "To release CallMate AI on the Google Play Store, the following production checklist must be completed:")
    add_bullet(doc, "Package Name & Versioning:", "com.callmate.ai with versionCode 1, versionName '1.0.0' configured in build.gradle.kts.")
    add_bullet(doc, "Permission Declarations:", "READ_CONTACTS (Whitelisting known callers), ACCESS_FINE_LOCATION (Delivery guidance), RECORD_AUDIO (Voice screening).")
    add_bullet(doc, "Privacy Policy Hosting:", "A published Privacy Policy URL detailing microphone, contact list, and account data handling must be linked.")
    add_bullet(doc, "Cleartext Traffic Enforcement:", "Disable android:usesCleartextTraffic='true' and enforce HTTPS with production domain certificate.")
    add_bullet(doc, "Account Deletion Compliance:", "Satisfied: DELETE /api/users/me allows users to permanently erase their cloud account.")
    add_bullet(doc, "ProGuard & R8 Obfuscation:", "Enable isMinifyEnabled = true in build.gradle.kts for release builds.")

    # 34. SECURITY AUDIT CHECKLIST
    add_h1(doc, "34. SECURITY AUDIT CHECKLIST")
    add_table(
        doc,
        [2.8, 1.5, 2.2],
        ["Security Dimension", "Audit Status", "Technical Verification"],
        [
            ["Password Salting & Hashing", "IMPLEMENTED", "Bcrypt with 10 salt rounds used before storage"],
            ["Stateless Token Authentication", "IMPLEMENTED", "Signed HS256 JWT tokens with 30-day expiration"],
            ["Protected API Routes", "IMPLEMENTED", "Bearer auth middleware guards all private endpoints"],
            ["Credential Protection", "IMPLEMENTED", "Environment variables used; zero hardcoded secrets"],
            ["Zero Plaintext Password Exposure", "IMPLEMENTED", "toJSON() pre-hook strips password hashes from JSON"],
            ["Data Segregation Boundary", "IMPLEMENTED", "Sensitive call transcripts remain strictly on local device"]
        ]
    )

    # 35. QUICK REFERENCE
    add_h1(doc, "35. PROJECT QUICK REFERENCE CARD")
    add_p(doc, "A high-level summary of the CallMate AI technical stack:")
    add_bullet(doc, "Project Name:", "CallMate AI")
    add_bullet(doc, "Official GitHub Repository:", "https://github.com/sanjana71006/CallAgent")
    add_bullet(doc, "150-Char Synopsis:", "CallMate AI is a privacy-first call assistant that screens unknown calls, transcribes voice live, blocks spam, and guides couriers with Google Gemini.")
    add_bullet(doc, "Standalone APK File:", "CallMate_AI_v1.0.0_debug.apk (18.8 MB)")
    add_bullet(doc, "Android Package:", "com.callmate.ai")
    add_bullet(doc, "Android Framework:", "Jetpack Compose + Kotlin Coroutines + Room SQLite (Version 3)")
    add_bullet(doc, "Cloud Backend:", "Node.js (Express) on Port 5000 (Auth, Location, Addresses, Global Spam)")
    add_bullet(doc, "AI Screening Server:", "Python (FastAPI) on Port 8000 (Google Gemini 3.6 Flash / Ollama / Mock)")
    add_bullet(doc, "Cloud Database:", "MongoDB Atlas (callmate_ai cluster)")
    add_bullet(doc, "Local Database:", "Room SQLite (callmate_ai.db, 11 entities)")
    add_bullet(doc, "Current Status:", "Fully functional native Android client with Contacts & GPS + Cloud Backend + Gemini AI")

    # 36. GLOSSARY
    add_h1(doc, "36. TECHNICAL GLOSSARY & DEFINITIONS")
    glossary_terms = [
        ("API (Application Programming Interface)", "A software intermediary that allows two applications to talk to each other."),
        ("REST (Representational State Transfer)", "An architectural style for network communications using standard HTTP methods."),
        ("JWT (JSON Web Token)", "A compact, URL-safe means of representing claims to be transferred between two parties securely."),
        ("Room Database", "An Android Jetpack persistence library providing an abstraction layer over SQLite."),
        ("DAO (Data Access Object)", "An interface providing database query and mutation methods for Room entities."),
        ("ViewModel", "An Android architecture component designed to store and manage UI-related data across lifecycle changes."),
        ("Jetpack Compose", "Android's modern declarative toolkit for building native user interfaces."),
        ("CallScreeningService", "An Android Telecom framework service allowing apps to allow, block, or silence incoming calls."),
        ("TTS (Text-to-Speech)", "Software that converts written textual strings into spoken acoustic voice audio."),
        ("STT (Speech-to-Text)", "Software that transcribes incoming acoustic voice streams into written text."),
        ("LLM (Large Language Model)", "An artificial intelligence model trained on extensive datasets to generate natural language."),
        ("Ollama", "A lightweight local runtime enabling private execution of open-source LLMs on local hardware.")
    ]
    for term, definition in glossary_terms:
        add_bullet(doc, term + ":", definition)
