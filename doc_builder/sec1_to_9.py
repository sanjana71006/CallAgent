from doc_builder.core import (
    add_h1, add_h2, add_h3, add_p, add_bullet,
    add_callout, add_code_block, add_table, add_title_page
)

def build_sec1_to_9(doc):
    add_title_page(doc)

    # Table of Contents
    add_h1(doc, "TABLE OF CONTENTS")
    toc_items = [
        "1. Project Overview",
        "2. Main Features & Implementation Status",
        "3. Technology Stack Specification",
        "4. System Architecture & Layer Breakdown",
        "5. Complete Application Workflow",
        "6. User Registration (Signup) Workflow",
        "7. Authentication (Login) Workflow",
        "8. Logout & Session Termination Workflow",
        "9. Authentication & Security Architecture",
        "10. Local Database Architecture (Room & SQLite)",
        "11. Cloud Database Architecture (MongoDB Atlas)",
        "12. MongoDB Document Structure & Schema",
        "13. MongoDB Operations & Query Cookbook",
        "14. Backend REST API Documentation",
        "15. Android Client API Communication Layer",
        "16. The 'You' Section (Profile & Settings)",
        "17. Spam Call Detection Architecture",
        "18. Spam Number Database Specification",
        "19. Cellular Call Flow & Telecom Screening",
        "20. AI Screening Engine & LLM Processing",
        "21. Offline Capabilities & Local Storage Matrix",
        "22. Complete System Data Flow Diagrams",
        "23. Project Directory Structure & Organization",
        "24. Important Source Code Files Directory",
        "25. Dependencies & Library Manifest",
        "26. Android System Permissions Analysis",
        "27. Comprehensive Developer Setup Guide",
        "28. MongoDB Atlas Cloud Database Setup",
        "29. Quality Assurance & Testing Checklist",
        "30. System Error Handling & Resilience",
        "31. Honest Limitations & Feature Constraints",
        "32. Future Development & Strategic Roadmap",
        "33. Google Play Store Readiness Assessment",
        "34. Security Audit Checklist",
        "35. Project Quick Reference Card",
        "36. Technical Glossary & Definitions"
    ]
    for item in toc_items:
        add_bullet(doc, item.split(".")[0] + ".", item.split(".", 1)[1].strip())

    doc.add_page_break()

    # 1. PROJECT OVERVIEW
    add_h1(doc, "1. PROJECT OVERVIEW")
    add_p(
        doc,
        "CallMate AI is a modern, privacy-first mobile call assistant designed to empower users with intelligent call screening, "
        "automated caller dialogs, and instant post-call structured intelligence. In an era where telemarketing spam, fraudulent robocalls, "
        "and unsolicited interruptions disrupt daily work and personal life, CallMate AI acts as a smart autonomous shield between the cellular network and the user."
    )
    add_h2(doc, "1.1 The Core Problem Solved")
    add_p(
        doc,
        "Traditional mobile phone dialers force users into a binary decision: answer an unknown number and risk wasting time on aggressive sales pitches, "
        "or reject the call and risk missing important communications such as courier package deliveries, recruiter interview confirmations, or banking alerts. "
        "CallMate AI eliminates this dilemma by providing an automated AI assistant that answers unknown callers, conducts polite conversational inquiry to determine their identity and purpose, "
        "and presents the user with real-time verbatim transcripts and executive summaries."
    )
    add_h2(doc, "1.2 Target Audience & User Personas")
    add_bullet(doc, "Busy Professionals & Executives:", "Individuals who receive frequent calls while in meetings, driving, or coding, and require call summaries without answering.")
    add_bullet(doc, "Students & Academic Researchers:", "Users who need distraction-free study blocks while staying available for delivery drivers or campus administrative notices.")
    add_bullet(doc, "Privacy-Conscious Individuals:", "Users who demand that their voice recordings, call logs, and personal addresses never leak to untrusted third parties.")
    add_bullet(doc, "Elderly & Non-Technical Users:", "Individuals vulnerable to financial scams, phishing pitches, and high-pressure telemarketing tactics.")

    add_h2(doc, "1.3 How CallMate AI Differs from Standard Phone Apps")
    add_table(
        doc,
        [1.8, 2.3, 2.4],
        ["Feature / Dimension", "Standard Dialer Apps", "CallMate AI Assistant"],
        [
            ["Call Reception", "Rings loudly; requires immediate human intervention", "Autonomous AI answers and screens on user's behalf"],
            ["Unknown Numbers", "Blind voice connection with no prior context", "Polite automated interview to extract caller name and intent"],
            ["Spam Defense", "Basic blacklists or static crowdsourced labels", "Dynamic heuristic rules + LLM intent classification"],
            ["Data Privacy", "Call metadata frequently shared with advertisers", "Strict offline-first local Room DB; cloud DB holds account only"],
            ["Post-Call Recap", "Raw audio recording or basic duration log", "Structured bullet points (Purpose, Action, Category, Key Details)"]
        ]
    )

    # 2. MAIN FEATURES
    add_h1(doc, "2. MAIN FEATURES & IMPLEMENTATION STATUS")
    add_p(
        doc,
        "The following matrix summarizes the implementation status of all major features across the CallMate AI client and backend systems. "
        "Each entry reflects verified source code in the repository."
    )
    add_table(
        doc,
        [1.8, 1.4, 1.5, 1.8],
        ["Feature Name", "Status", "Access Point", "Internal Mechanism"],
        [
            ["User Registration", "IMPLEMENTED", "Sign Up Screen", "Express /api/auth/register + MongoDB Atlas + bcrypt"],
            ["User Login & JWT", "IMPLEMENTED", "Login Screen", "Express /api/auth/login + JWT 30-day token generation"],
            ["Sign Out Session", "IMPLEMENTED", "You -> Sign Out", "Clears JWT from Room DataStore/localStorage; returns to Login"],
            ["Account Deletion", "IMPLEMENTED", "You -> Account Data", "DELETE /api/users/me -> removes user from MongoDB Atlas"],
            ["Personal Details Edit", "IMPLEMENTED", "You -> Personal Details", "Edits Name, Phone, Gender -> persists in Room user_profile"],
            ["AI Master Switch", "IMPLEMENTED", "Chats & You headers", "Toggles Assistant status -> persists in assistant_settings table"],
            ["Your Instructions", "IMPLEMENTED", "You -> Instructions", "Custom prompt rules for delivery, recruiters, loans in Room"],
            ["Health Check Ping", "IMPLEMENTED", "You -> Health Check", "Real-time HTTP GET latency ping to backend service"],
            ["Silent Mode Filtering", "IMPLEMENTED", "You -> Silent Mode", "Toggles for telemarketing, scam, unknown spam filtering"],
            ["Voice & Language", "IMPLEMENTED", "You -> Voice & Lang", "Android TTS pitch & speed sliders with live voice sample"],
            ["Delivery Addresses", "IMPLEMENTED", "You -> Addresses", "CRUD Room database for Home, Work, College addresses"],
            ["WhatsApp Updates", "IMPLEMENTED", "You -> WhatsApp", "Configuration switches for alert categories in Room"],
            ["Account Data Card", "IMPLEMENTED", "You -> Account Data", "Displays cloud identity, MongoDB storage badge, active status"],
            ["Help Center FAQ", "IMPLEMENTED", "You -> Help Center", "Accordion FAQs explaining screening, takeover, and privacy"],
            ["Theme Switching", "IMPLEMENTED", "Top bar toggle button", "Toggles Light / Dark mode themes dynamically"],
            ["Call Transcripts", "IMPLEMENTED", "Detail Screen", "Stores caller & AI dialogue turns in Room transcripts entity"],
            ["Post-Call Summaries", "IMPLEMENTED", "Detail Screen", "AI extracted Purpose, Recommended Action, Key Details"],
            ["Simulated Screening", "IMPLEMENTED", "Chats -> Simulate", "SimulatorCallProvider mock engine with audio & live feed"],
            ["CallScreeningService", "PLANNED / NOT YET IMPLEMENTED", "Android System Telecom", "Native Android cellular call interception pipeline"],
            ["Indian Spam DB", "NOT IMPLEMENTED", "Cloud / Local DB", "Real verified Indian spam database (currently synthetic)"]
        ]
    )

    # 3. TECHNOLOGY STACK
    add_h1(doc, "3. TECHNOLOGY STACK SPECIFICATION")
    add_p(doc, "The table below details every technology, framework, and library actively utilized across the CallMate AI codebase.")
    add_table(
        doc,
        [1.4, 1.6, 1.5, 2.0],
        ["Technology", "Category", "Where Used", "Architectural Rationale"],
        [
            ["Kotlin 2.0.21", "Programming Language", "Android Application", "Modern, expressive, null-safe language for Android"],
            ["Jetpack Compose", "UI Framework", "Android UI Layer", "Declarative, reactive UI toolkit with Material 3 styling"],
            ["Android Room 2.6.1", "Local SQLite ORM", "Android Data Layer", "Robust, type-safe SQLite abstraction for offline persistence"],
            ["Coroutines & Flow", "Asynchronous Framework", "Android Architecture", "Non-blocking reactive state management across layers"],
            ["Retrofit 2.11.0", "HTTP Client", "Android Network Layer", "Type-safe REST client for Node.js and FastAPI backends"],
            ["OkHttp 4.12.0", "HTTP Engine", "Android Network Layer", "Handles connection pooling, logging, and auth headers"],
            ["DataStore / Prefs", "Key-Value Storage", "Android App Prefs", "Stores session tokens, theme state, and onboarding flags"],
            ["Node.js 18+", "JavaScript Runtime", "Cloud Backend Server", "Fast, asynchronous runtime for auth and user management"],
            ["Express.js 4.19.2", "Backend Web Framework", "Cloud Backend Server", "Lightweight, scalable REST API router and middleware"],
            ["MongoDB Atlas", "Cloud NoSQL Database", "Cloud User Storage", "Distributed cloud document database for user accounts"],
            ["Mongoose 8.5.1", "MongoDB ODM", "Node.js Backend", "Schema validation, indexing, and connection management"],
            ["jsonwebtoken 9.0", "Security Token", "Node.js Backend", "Stateless, signed JWT authentication for mobile sessions"],
            ["bcryptjs 2.4.3", "Cryptographic Hashing", "Node.js Backend", "Adaptive one-way salted hashing for user passwords"],
            ["FastAPI 0.111.0", "Python Web Framework", "AI Screening Backend", "High-performance asynchronous Python API for LLM inference"],
            ["Ollama / Qwen2.5", "Local LLM Provider", "AI Engine", "On-device or local private LLM inference for screening"],
            ["Android TTS & STT", "Speech Synthesis & Rec", "Android Core Audio", "Native Android speech-to-text and text-to-speech engines"]
        ]
    )

    # 4. SYSTEM ARCHITECTURE
    add_h1(doc, "4. SYSTEM ARCHITECTURE & LAYER BREAKDOWN")
    add_p(
        doc,
        "CallMate AI utilizes a decoupled, three-tier architecture that enforces a strict privacy boundary between local device data and cloud user accounts:"
    )
    add_code_block(
        doc,
"""+-----------------------------------------------------------------------------------+
|                           CALLMATE AI CLIENT (ANDROID)                            |
|                                                                                   |
|  [ Jetpack Compose UI ] <---> [ ViewModels ] <---> [ Repositories ]               |
|                                                          |                        |
|                     +------------------------------------+--------------------+   |
|                     |                                                         |   |
|                     v                                                         v   |
|       [ Room SQLite Local Database ]                              [ Retrofit REST API ]
|       (Calls, Transcripts, Summaries,                                         |   |
|        Addresses, Instructions, Settings)                                     |   |
+-------------------------------------------------------------------------------|---+
                                                                                |
                       +--------------------------------------------------------+
                       |                                                        |
                       v (Port 5000)                                            v (Port 8000)
+---------------------------------------------+        +---------------------------------------------+
|    CLOUD AUTH BACKEND (NODE.JS + EXPRESS)   |        |       AI SCREENING ENGINE (FASTAPI + LLM)   |
|                                             |        |                                             |
|  - Registration, Login, Session Verification|        |  - Real-Time Dialog Screening (/chat)       |
|  - JWT Token Minting & Validation           |        |  - Intent Classification (/classify)       |
|  - User Profile & Cloud Account Lifecycle   |        |  - Post-Call Summarization (/summarize)     |
|                      |                      |        |  - Ollama Engine (qwen2.5:0.5b) / Fallback  |
|                      v                      |        +---------------------------------------------+
|          [ MongoDB Atlas Cluster ]          |
|          (users collection)                 |
+---------------------------------------------+"""
    )
    add_h2(doc, "4.1 Architectural Layer Details")
    add_bullet(doc, "Presentation Layer (Jetpack Compose):", "Renders declarative screens, observes UI states from ViewModels, and provides tactile user feedback.")
    add_bullet(doc, "ViewModel Layer (Architecture Components):", "Encapsulates screen business logic, manages Coroutine scopes, and exposes StateFlow objects.")
    add_bullet(doc, "Repository Layer (Clean Architecture):", "Acts as the single source of truth, mediating between local Room cache and remote network endpoints.")
    add_bullet(doc, "Local Storage Layer (Room + SQLite):", "Stores all sensitive user data (calls, audio transcripts, addresses, custom instructions) securely on the device.")
    add_bullet(doc, "Network Layer (Retrofit + OkHttp):", "Executes asynchronous HTTP requests with automatic Bearer token injection via AuthInterceptor.")
    add_bullet(doc, "Cloud Backend Layer (Express.js):", "Provides secure, stateless REST endpoints for identity management, validation, and account deletion.")
    add_bullet(doc, "Cloud Database Layer (MongoDB Atlas):", "Stores encrypted user credentials and account metadata in a managed cloud cluster.")
    add_bullet(doc, "AI Inference Layer (Python FastAPI):", "Executes prompt templates against local LLM instances (Ollama) with automatic fallback.")

    # 5. APPLICATION WORKFLOW
    add_h1(doc, "5. COMPLETE APPLICATION WORKFLOW")
    add_p(doc, "The end-to-end operational lifecycle of CallMate AI proceeds through the following sequential stages:")
    add_bullet(doc, "A. Application Launch & Splash Screen:", "App initializes CallMateDatabase, reads DataStore token, and checks session validity.")
    add_bullet(doc, "B. Initial Routing:", "If a valid JWT token exists, navigates directly to Home (Chats); otherwise, navigates to LoginScreen.")
    add_bullet(doc, "C. User Registration (Signup):", "New user provides Name, Email, Phone, and Password. Backend hashes password, inserts into MongoDB Atlas, and returns JWT.")
    add_bullet(doc, "D. User Authentication (Login):", "Existing user enters credentials. Backend verifies bcrypt hash, returns JWT, and client saves token.")
    add_bullet(doc, "E. Main Hub (Chats & You Navigation):", "User navigates between recent screened calls (Chats tab) and preferences (You tab).")
    add_bullet(doc, "F. Incoming Call Simulation:", "Simulator generates incoming call event with caller name and phone number.")
    add_bullet(doc, "G. AI Call Screening:", "AI answers call, greets caller, collects caller purpose, and displays verbatim speech bubbles in real time.")
    add_bullet(doc, "H. User Intervention (Takeover):", "User can tap 'Take Call' at any time to transition from automated screening to live direct conversation.")
    add_bullet(doc, "I. Post-Call Analysis & Summarization:", "FastAPI engine processes transcript to extract caller purpose, category, and action item.")
    add_bullet(doc, "J. Local Persistence:", "Room database saves CallEntity, TranscriptEntities, and CallSummaryEntity locally on the device.")
    add_bullet(doc, "K. 'You' Section Management:", "User customizes screening prompts, tests voice synthesis, configures addresses, and checks backend health.")
    add_bullet(doc, "L. Account Sign Out:", "User initiates logout; client wipes session token from DataStore and routes to LoginScreen.")
    add_bullet(doc, "M. Account Deletion:", "User requests deletion; backend deletes user record from MongoDB Atlas, client clears local session.")

    # 6. SIGNUP WORKFLOW
    add_h1(doc, "6. USER REGISTRATION (SIGNUP) WORKFLOW")
    add_p(doc, "The registration workflow guarantees data integrity and security through multi-stage validation:")
    add_code_block(
        doc,
"""User enters Name, Email, Phone, Password, Confirm Password
  |
  v
[Android Client Validation]
  - Name is non-empty and <= 50 chars
  - Email matches regex ^\\S+@\\S+\\.\\S+$
  - Password length >= 6 characters
  - Password equals Confirm Password
  |
  v
POST /api/auth/register (JSON Payload)
  |
  v
[Express Backend Controller]
  - Normalizes email to lowercase
  - Checks duplicate email in MongoDB Atlas (User.findOne)
  - Hashes password using bcrypt (10 salt rounds)
  - Generates unique userId (usr_<UUID>)
  - Inserts document into MongoDB users collection
  |
  v
[JWT Minting] -> Signs JWT containing userId and email (expires in 30 days)
  |
  v
[Android Client Response Handler]
  - Stores JWT token in DataStore / TokenManager
  - Initializes local UserProfileEntity in Room
  - Navigates user directly to HomeScreen (Chats)"""
    )

    # 7. LOGIN WORKFLOW
    add_h1(doc, "7. AUTHENTICATION (LOGIN) WORKFLOW")
    add_p(doc, "The login workflow verifies credentials against MongoDB Atlas and establishes a persistent mobile session:")
    add_code_block(
        doc,
"""User enters Email and Password
  |
  v
[Android Client Validation] -> Checks non-empty email and password
  |
  v
POST /api/auth/login (JSON Payload)
  |
  v
[Express Backend Controller]
  - Queries MongoDB Atlas: User.findOne({ email }).select('+password +passwordHash')
  - Validates accountStatus is ACTIVE (rejects DELETED accounts)
  - Compares candidate password against bcrypt hash using bcrypt.compare()
  - Updates user.lastLogin timestamp in MongoDB
  |
  v
[JWT Minting] -> Issues signed session token
  |
  v
[Android Client Response Handler]
  - Saves token to TokenManager
  - Updates local profile cache
  - Transitions UI state to AuthState.Authenticated
  - Opens HomeScreen"""
    )

    # 8. LOGOUT WORKFLOW
    add_h1(doc, "8. LOGOUT & SESSION TERMINATION WORKFLOW")
    add_p(
        doc,
        "When a user signs out, CallMate AI strictly adheres to its privacy-first architecture: "
        "the cloud session token is discarded, but local call history and settings remain preserved in the local encrypted Room database."
    )
    add_bullet(doc, "1. User Action:", "User taps 'Sign Out' in SettingsScreen or AccountDataScreen.")
    add_bullet(doc, "2. Confirmation Dialog:", "A modal dialog prompts: 'Are you sure you want to sign out?' to prevent accidental logouts.")
    add_bullet(doc, "3. State Reset:", "AuthViewModel calls TokenManager.clearToken() and updates auth state to AuthState.Unauthenticated.")
    add_bullet(doc, "4. Local Cache Preservation:", "Calls, transcripts, addresses, and voice preferences remain in Room SQLite for offline access.")
    add_bullet(doc, "5. UI Navigation:", "NavGraph pops the back stack and resets the root destination to Screen.Login.")

    # 9. AUTHENTICATION AND SECURITY
    add_h1(doc, "9. AUTHENTICATION & SECURITY ARCHITECTURE")
    add_p(doc, "Security in CallMate AI is implemented at multiple levels across the Android client and cloud backend:")
    add_bullet(doc, "Password Hashing:", "Passwords are never stored in plaintext. They are salted and hashed using bcrypt (10 rounds) before hitting MongoDB.")
    add_bullet(doc, "JWT Stateless Sessions:", "Cloud endpoints use signed JSON Web Tokens with HS256 signatures, validated via authMiddleware.")
    add_bullet(doc, "Data Segregation:", "MongoDB Atlas holds only user account identity data. No calls, transcripts, or personal addresses are uploaded.")
    add_bullet(doc, "Protected Endpoints:", "All /api/users/* routes require an 'Authorization: Bearer <token>' header.")
    add_bullet(doc, "Environment Variable Protection:", "All database URIs, JWT secrets, and port bindings are loaded from .env and excluded from source control.")

    add_callout(
        doc,
        "In current development builds, android:usesCleartextTraffic='true' is enabled in AndroidManifest.xml to permit local emulator "
        "loopback testing (http://10.0.2.2:5000 and http://10.0.2.2:8000). For production release, HTTPS/TLS encryption and Android Network Security Config must be enforced.",
        title="SECURITY CONSIDERATION",
        status="warning"
    )
