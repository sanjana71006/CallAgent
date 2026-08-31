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
    add_bullet(doc, "Active Collections:", "users (stores registered profiles, GPS location, courier addresses, instructions, and settings), spamnumbers (global community threat database)")
    add_bullet(doc, "Indexes:", "users.email_1 (unique), users.userId_1 (unique), spamnumbers.phoneNumber_1 (unique indexed phone)")

    # 12. MONGODB DOCUMENT STRUCTURE
    add_h1(doc, "12. MONGODB DOCUMENT STRUCTURE & SCHEMA")
    add_p(doc, "Below is the verified JSON document structure for the users and spamnumbers collections in MongoDB Atlas:")
    add_code_block(
        doc,
"""// Collection: users
{
  "_id": "66ce381a89f41b120c89a4e2",
  "userId": "usr_aee79ffe-8888-4601-8f79-c9e9c535e810",
  "name": "Sanjana",
  "email": "sanjana@callmate.ai",
  "phoneNumber": "+919440886543",
  "gender": "Female",
  "location": {
    "latitude": 16.2888,
    "longitude": 80.4256,
    "address": "Hanuman Nagar, Guntur, Andhra Pradesh, 522001, India",
    "accuracy": 241,
    "updatedAt": "2026-08-31T16:00:36.000Z"
  },
  "addresses": [
    {
      "id": "addr_live_auto",
      "label": "📍 Live Current Location",
      "fullAddress": "Hanuman Nagar, Guntur, Andhra Pradesh, 522001, India",
      "coordinates": { "lat": 16.2888, "lng": 80.4256 }
    },
    {
      "id": "addr_892348",
      "label": "🏢 Work Hub",
      "fullAddress": "Tower 4, Mindspace Tech Park, HITEC City, Hyderabad",
      "additionalDetails": "Drop at 5th floor reception"
    }
  ],
  "instructions": [
    {
      "id": "inst_1",
      "title": "Delivery Couriers",
      "prompt": "Ask for delivery package tracking number and tell driver to leave parcel at front door.",
      "enabled": true
    }
  ],
  "accountStatus": "ACTIVE",
  "createdAt": "2026-08-27T16:32:17.346Z",
  "updatedAt": "2026-08-31T16:00:36.000Z"
}

// Collection: spamnumbers (Crowdsourced Threat Database)
{
  "_id": "66ce89b21f98e2340b12a991",
  "phoneNumber": "+1 (555) 382-9012",
  "reportCount": 14,
  "spamScore": 92,
  "category": "FINANCIAL_SCAM",
  "callerName": "Bajaj Quick Loan Agent",
  "reporters": ["usr_aee79ffe-8888-4601-8f79-c9e9c535e810"],
  "lastReportedAt": "2026-08-31T15:49:18.950Z"
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

// 4. Query live location & addresses
db.users.find({ email: "sanjana@callmate.ai" }, { location: 1, addresses: 1 });

// 5. Query crowdsourced spam numbers with report count >= 5
db.spamnumbers.find({ reportCount: { $gte: 5 } }).sort({ reportCount: -1 });

// 6. Report/Increment spam score for a telephone number
db.spamnumbers.updateOne(
  { phoneNumber: "+15553829012" },
  { $inc: { reportCount: 1, spamScore: 5 }, $set: { lastReportedAt: new Date() } },
  { upsert: true }
);"""
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
            ["PUT", "/api/users/me", "Bearer", "Updates Name, Phone, Gender. Body: {name, phoneNumber, gender}"],
            ["PUT", "/api/users/location", "Bearer", "Updates GPS & auto-reverse geocoded street address. Body: {latitude, longitude, address, accuracy}"],
            ["GET", "/api/users/addresses", "Bearer", "Fetches all saved courier drop-off addresses from MongoDB Atlas"],
            ["POST", "/api/users/addresses", "Bearer", "Saves new delivery address to MongoDB. Body: {label, fullAddress, additionalDetails}"],
            ["DELETE", "/api/users/addresses/:id", "Bearer", "Removes saved address from MongoDB Atlas"],
            ["POST", "/api/users/instructions", "Bearer", "Saves custom AI screening rule. Body: {title, prompt, tag}"],
            ["DELETE", "/api/users/instructions/:id", "Bearer", "Removes screening rule from cloud"],
            ["POST", "/api/spam/report", "Bearer", "Reports number to global community spam database. Body: {phoneNumber, category, reason}"],
            ["GET", "/api/spam/check/:phoneNumber", "No", "Checks if number is flagged as community spam. Returns {isSpam, reportCount, spamScore}"],
            ["GET", "/api/spam/my-spam", "Bearer", "Fetches all spam numbers flagged by current authenticated user"],
            ["GET", "/api/health", "No", "System health check returning database connection status and shard host"],
            ["POST", "/api/v1/ai/chat", "No", "Google Gemini 3.6 Flash live screening engine reply. Body: {call_id, conversation}"],
            ["POST", "/api/v1/ai/classify", "No", "Multi-category intent classifier. Body: {call_id, conversation}"],
            ["POST", "/api/v1/ai/summarize", "No", "Post-call structured summary generation. Body: {call_id, conversation, caller_name}"],
            ["GET", "/api/v1/health", "No", "FastAPI health check reporting Gemini AI engine status"]
        ]
    )

    # 15. ANDROID API COMMUNICATION
    add_h1(doc, "15. ANDROID CLIENT API COMMUNICATION LAYER")
    add_p(
        doc,
        "The Android client coordinates network operations through Retrofit interfaces, OkHttp client interceptors, "
        "and Kotlin Coroutines Flow within the AuthRepositoryImpl and CallRepositoryImpl repositories."
    )
    add_bullet(doc, "Base URLs:", "Cloud Auth & MongoDB: http://10.0.2.2:5000/api/ | AI Screening Engine: http://10.0.2.2:8000/api/v1/")
    add_bullet(doc, "AuthInterceptor:", "Dynamically extracts the stored JWT token from TokenManager and injects Authorization: Bearer <token> into request headers.")
    add_bullet(doc, "Error Handling:", "HttpExceptions are caught in repository functions and mapped to user-friendly Resource.Error(message) objects.")

    # 16. THE YOU SECTION
    add_h1(doc, "16. THE 'YOU' SECTION (PROFILE & SETTINGS)")
    add_p(doc, "The 'You' section is CallMate AI's settings and user management dashboard. All subsections are fully implemented:")
    add_bullet(doc, "1. Profile Header:", "Displays avatar, full name, phone number, and pencil icon to open PersonalDetailsScreen.")
    add_bullet(doc, "2. Tell CallMate (Master Toggle):", "Master switch enabling or pausing all automated AI call screening.")
    add_bullet(doc, "3. Your Instructions:", "Interactive manager where users add (+ Add New Screening Instruction), edit, and delete custom rules with cloud sync to MongoDB.")
    add_bullet(doc, "4. Assistant Health Check:", "Interactive connectivity screen testing latency against http://10.0.2.2:8000 and reporting backend health.")
    add_bullet(doc, "5. Silent Mode:", "Granular switchboard allowing users to auto-silence telemarketers, known spam numbers, and unverified callers.")
    add_bullet(doc, "6. Voice & Language:", "Voice customization screen with pitch and speed sliders, language picker, and TTS sample preview.")
    add_bullet(doc, "7. Your Addresses & Live GPS:", "Displays real-time reverse-geocoded street address (OpenStreetMap Nominatim) and supports custom courier drop-off locations.")
    add_bullet(doc, "8. Invite a Friend:", "Instantly copies official GitHub repository link (https://github.com/sanjana71006/CallAgent) and opens Android share sheet.")
    add_bullet(doc, "9. Account Data & Sign Out:", "Shows cloud account status (ACTIVE), MongoDB storage indicator, Sign Out confirmation, and Delete Account trigger.")

    # 17. SPAM CALL DETECTION
    add_h1(doc, "17. SPAM CALL DETECTION ARCHITECTURE")
    add_p(
        doc,
        "CallMate AI implements a multi-tier defense architecture combining local contact whitelisting, "
        "crowdsourced community threat databases (MongoDB Atlas), and Gemini LLM intent classification."
    )
    add_code_block(
        doc,
"""Incoming Call Event (Phone Number + Caller Name)
        |
        v
[Phone Contacts Lookup (ContactsContract)]
  - Is caller saved in Address Book? -> Direct Ring (Bypass Screening)
        |
        v [Unknown Number]
[Global Spam Threat Check (GET /api/spam/check/:phoneNumber)]
  - Is caller reported in MongoDB? -> Threat Badge (🚨 Likely Spam)
        |
        v
[AI Intent Classifier (/api/v1/ai/classify - Gemini 3.6 Flash)]
  - Evaluates conversation for loan scams, telemarketing, robocalls
        |
        v
[Post-Call User Spam Reporting (POST /api/spam/report)]
"""
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
