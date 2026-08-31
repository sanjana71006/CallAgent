from doc_builder.core import (
    add_h1, add_h2, add_h3, add_p, add_bullet,
    add_callout, add_code_block, add_table
)

def build_sec10_to_18(doc):
    # 10. LOCAL DATABASE (ROOM + SQLITE)
    add_h1(doc, "10. LOCAL DATABASE ARCHITECTURE (ROOM & SQLITE)")
    add_p(
        doc,
        "The Android client utilizes an encrypted Room database (callmate_ai.db) composed of 11 entities and 6 Data Access Objects (DAOs). "
        "Below is the complete database schema specification."
    )

    add_h2(doc, "10.1 Room Database Entities Specification")
    add_table(
        doc,
        [1.8, 1.4, 1.2, 2.1],
        ["Entity Class Name", "Table Name", "Primary Key", "Core Fields & Purpose"],
        [
            ["CallEntity", "calls", "id (String)", "phoneNumber, callerName, timestamp, durationSeconds, status, category, importance, isSpam"],
            ["TranscriptEntity", "transcripts", "id (Long Auto)", "callId (FK -> calls.id CASCADE), speaker (AI/CALLER), message, timestamp"],
            ["CallSummaryEntity", "call_summaries", "id (String)", "callId (FK -> calls.id CASCADE), purpose, importantInfo, recommendedAction, category"],
            ["UserProfileEntity", "user_profile", "id (String)", "name, gender, avatarUri, phoneNumber, email, isCloudSynced, updatedAt"],
            ["AddressEntity", "addresses", "id (String)", "label (Home/Work/College/Other), addressName, fullAddress, additionalDetails, updatedAt"],
            ["AssistantSettingsEntity", "assistant_settings", "id (String)", "assistantEnabled, assistantName, greeting, autoScreenUnknown, autoScreenSpam, backendUrl"],
            ["AssistantInstructionsEntity", "assistant_instructions", "id (String)", "instructions (Prompt text for delivery, recruiters, loans, personal rules)"],
            ["VoiceSettingsEntity", "voice_settings", "id (String)", "language (en-US), voiceId, speechRate (0.5-2.0f), speechPitch (0.5-2.0f)"],
            ["SilentModeSettingsEntity", "silent_mode_settings", "id (String)", "enabled, silenceTelemarketing, silenceSpam, silenceUnknown, silencePotentialScam"],
            ["NotificationSettingsEntity", "notification_settings", "id (String)", "assistantUpdates, importantAlerts, featureUpdates, promotionalUpdates"],
            ["AppPreferencesEntity", "app_preferences", "id (String)", "themeMode (SYSTEM/LIGHT/DARK), onboardingCompleted, selectedTab, token"]
        ]
    )

    add_h2(doc, "10.2 Room Data Access Objects (DAOs)")
    add_bullet(doc, "CallDao:", "getAllCalls(): Flow<List<CallEntity>>, getRecentCalls(limit), getCallById(id), insertCall(call), deleteCallById(id), clearAllCalls().")
    add_bullet(doc, "TranscriptDao:", "getTranscriptsForCall(callId): Flow<List<TranscriptEntity>>, insertTranscript(msg), insertTranscripts(list), deleteTranscriptsForCall(callId).")
    add_bullet(doc, "CallSummaryDao:", "getSummaryForCall(callId): Flow<CallSummaryEntity?>, insertSummary(summary), deleteSummaryByCallId(callId).")
    add_bullet(doc, "UserProfileDao:", "getUserProfile(): Flow<UserProfileEntity?>, getUserProfileSync(), insertOrUpdate(profile), deleteProfile().")
    add_bullet(doc, "AddressDao:", "getAllAddresses(): Flow<List<AddressEntity>>, getAddressById(id), insertAddress(address), updateAddress(address), deleteAddressById(id), clearAllAddresses().")
    add_bullet(doc, "LocalSettingsDao:", "getAssistantSettings(), saveAssistantSettings(), getInstructions(), saveInstructions(), getVoiceSettings(), saveVoiceSettings(), getSilentModeSettings(), saveSilentModeSettings(), getNotificationSettings(), saveNotificationSettings(), clearAllSettings().")

    # 11. MONGODB DATABASE
    add_h1(doc, "11. CLOUD DATABASE ARCHITECTURE (MONGODB ATLAS)")
    add_p(
        doc,
        "CallMate AI connects its Node.js backend to MongoDB Atlas for cloud account management. "
        "The database strictly stores account information, isolating cloud storage from local device telephony logs."
    )
    add_bullet(doc, "Database Name:", "callmate_ai")
    add_bullet(doc, "Hosting Platform:", "MongoDB Atlas (Multi-cloud shard cluster)")
    add_bullet(doc, "Connection Library:", "Mongoose ODM with serverSelectionTimeoutMS = 4000ms and bufferCommands = false")
    add_bullet(doc, "Active Collections:", "users (stores registered user accounts and authentication credentials)")
    add_bullet(doc, "Indexes:", "email_1 (unique index for fast lookup and duplicate prevention), userId_1 (unique indexed UUID)")

    # 12. MONGODB DOCUMENT STRUCTURE
    add_h1(doc, "12. MONGODB DOCUMENT STRUCTURE & SCHEMA")
    add_p(doc, "Below is the verified JSON document structure for the users collection:")
    add_code_block(
        doc,
"""{
  "_id": "66ce381a89f41b120c89a4e2",
  "userId": "usr_aee79ffe-8888-4601-8f79-c9e9c535e810",
  "name": "Sanjana",
  "email": "sanjana@callmate.ai",
  "password": "$2a$10$7vN3XGg5d9u1b.k8wP2qEe6h1Y8j3kL4m5n6o7p8q9r0s1t2u3v4w",
  "passwordHash": "$2a$10$7vN3XGg5d9u1b.k8wP2qEe6h1Y8j3kL4m5n6o7p8q9r0s1t2u3v4w",
  "phoneNumber": "+919440886543",
  "accountStatus": "ACTIVE",
  "appVersion": "1.0.0",
  "lastLogin": "2026-08-31T15:04:33.957Z",
  "createdAt": "2026-08-27T16:32:17.346Z",
  "updatedAt": "2026-08-31T15:04:33.957Z",
  "__v": 0
}"""
    )

    # 13. MONGODB QUERIES
    add_h1(doc, "13. MONGODB OPERATIONS & QUERY COOKBOOK")
    add_p(doc, "The following commands provide standard database administration queries for MongoDB Shell (mongosh) and MongoDB Compass:")
    add_code_block(
        doc,
"""// 1. Select the CallMate AI database
use callmate_ai;

// 2. View all collections
show collections;

// 3. Find user by email
db.users.findOne({ email: "sanjana@callmate.ai" }, { password: 0, passwordHash: 0 });

// 4. Count active registered users
db.users.countDocuments({ accountStatus: "ACTIVE" });

// 5. Update user phone number
db.users.updateOne(
  { userId: "usr_aee79ffe-8888-4601-8f79-c9e9c535e810" },
  { $set: { phoneNumber: "+919440886543", updatedAt: new Date() } }
);

// 6. Soft-delete user account
db.users.updateOne(
  { email: "user@example.com" },
  { $set: { accountStatus: "DELETED", updatedAt: new Date() } }
);

// 7. [CAUTION: DESTRUCTIVE] Permanently remove user by userId
db.users.deleteOne({ userId: "usr_aee79ffe-8888-4601-8f79-c9e9c535e810" });"""
    )

    # 14. BACKEND API DOCUMENTATION
    add_h1(doc, "14. BACKEND REST API DOCUMENTATION")
    add_p(doc, "The table below documents all active REST API endpoints across the Node.js Cloud Backend (Port 5000) and Python AI Engine (Port 8000).")
    add_table(
        doc,
        [1.0, 2.0, 1.1, 2.4],
        ["Method", "Endpoint Path", "Auth", "Description & Payload"],
        [
            ["POST", "/api/auth/register", "No", "Registers new user. Body: {name, email, password, confirmPassword, phoneNumber}"],
            ["POST", "/api/auth/login", "No", "Authenticates user. Body: {email, password}. Returns {token, user}"],
            ["POST", "/api/auth/logout", "No", "Logs out user session and confirms invalidation"],
            ["GET", "/api/auth/me", "Bearer", "Returns current authenticated user session data"],
            ["GET", "/api/users/me", "Bearer", "Fetches full user profile details from database"],
            ["PUT", "/api/users/me", "Bearer", "Updates Name or Phone. Body: {name, phoneNumber}"],
            ["DELETE", "/api/users/me", "Bearer", "Permanently deletes user account from MongoDB Atlas"],
            ["GET", "/api/health", "No", "System health check returning database status and uptime"],
            ["POST", "/api/v1/ai/chat", "No", "Generates screening reply. Body: {call_id, conversation}"],
            ["POST", "/api/v1/ai/classify", "No", "Classifies call category and intent. Body: {call_id, conversation}"],
            ["POST", "/api/v1/ai/summarize", "No", "Generates post-call structured summary. Body: {call_id, conversation, caller_name}"],
            ["GET", "/api/v1/health", "No", "FastAPI health check with AI provider status and latency"]
        ]
    )

    # 15. ANDROID API COMMUNICATION
    add_h1(doc, "15. ANDROID CLIENT API COMMUNICATION LAYER")
    add_p(
        doc,
        "The Android client coordinates network operations through Retrofit interfaces, OkHttp client interceptors, "
        "and Kotlin Coroutines Flow within the AuthRepositoryImpl and CallRepositoryImpl repositories."
    )
    add_bullet(doc, "Base URLs:", "Cloud Auth: http://10.0.2.2:5000/api/ | AI Screening Engine: http://10.0.2.2:8000/api/v1/")
    add_bullet(doc, "AuthInterceptor:", "Dynamically extracts the stored JWT token from TokenManager and injects Authorization: Bearer <token> into request headers.")
    add_bullet(doc, "Error Handling:", "HttpExceptions are caught in repository functions and mapped to user-friendly Resource.Error(message) objects.")

    # 16. THE YOU SECTION
    add_h1(doc, "16. THE 'YOU' SECTION (PROFILE & SETTINGS)")
    add_p(doc, "The 'You' section is CallMate AI's settings and user management dashboard. All 9 subsections are fully implemented:")
    add_bullet(doc, "1. Profile Header:", "Displays avatar, full name, phone number, and pencil icon to open PersonalDetailsScreen.")
    add_bullet(doc, "2. Tell CallMate (Master Toggle):", "Master switch enabling or pausing all automated AI call screening.")
    add_bullet(doc, "3. Your Instructions:", "Full editable prompt screen where users configure screening rules for courier drivers, job recruiters, and promotional loans.")
    add_bullet(doc, "4. Assistant Health Check:", "Interactive connectivity screen that tests latency against http://10.0.2.2:8000 and reports backend health.")
    add_bullet(doc, "5. Silent Mode:", "Granular switchboard allowing users to auto-silence telemarketers, known spam numbers, and unverified callers.")
    add_bullet(doc, "6. Voice & Language:", "Voice customization screen with pitch (0.5x - 2.0x) and speed sliders, language picker, and TTS sample player.")
    add_bullet(doc, "7. Your Addresses:", "Complete CRUD address manager supporting Home, Office, College, and Other labels with delivery driver notes.")
    add_bullet(doc, "8. WhatsApp Updates:", "Notification preference toggles for instant recap alerts, urgent call alerts, and feature announcements.")
    add_bullet(doc, "9. Account Data & Sign Out:", "Shows cloud account status (ACTIVE), MongoDB storage indicator, prominent Sign Out button, and Delete Account trigger.")

    # 17. SPAM CALL DETECTION
    add_h1(doc, "17. SPAM CALL DETECTION ARCHITECTURE")
    add_p(
        doc,
        "CallMate AI currently implements spam detection using a hybrid rule-based and simulated classification pipeline. "
        "When a call arrives, the system normalizes the telephone number, checks user-defined Silent Mode filtering rules, and triggers AI classification."
    )
    add_code_block(
        doc,
"""Incoming Call Event (Phone Number + Caller Name)
        |
        v
[Phone Normalization] -> Strips non-digits, normalizes country codes
        |
        v
[Silent Mode Heuristic Check (Room)]
  - Is caller flagged as Telemarketer? -> Silence
  - Is caller flagged as Potential Scam? -> Silence
  - Is caller flagged as Unknown Spam? -> Auto-Screen with AI
        |
        v
[AI Intent Classifier (/api/v1/ai/classify)]
  - Evaluates initial greeting & caller response
  - Returns Category (SPAM, DELIVERY, RECRUITER, BANK, UNKNOWN)
  - Computes Confidence Score (0.00 - 1.00)
        |
        v
[Action Decision: Allow / Mute / Screen / Decline]"""
    )
    add_callout(
        doc,
        "Android's native CallScreeningService integration for intercepting carrier-level cellular calls is Planned / Not Yet Implemented. "
        "Current spam screening executes through the integrated call simulation engine.",
        title="IMPLEMENTATION STATUS",
        status="warning"
    )

    # 18. SPAM DATABASE
    add_h1(doc, "18. SPAM NUMBER DATABASE SPECIFICATION")
    add_p(
        doc,
        "The spam detection data model supports phone number normalization, spam category tags, confidence scores, and report tallies."
    )
    add_callout(
        doc,
        "No verified production Indian spam-number dataset is currently integrated. All spam numbers currently present in tests are TEST DATA ONLY.",
        title="DATASET STATUS",
        status="warning"
    )
