# 🛡️ CallMate AI — Privacy-First Agentic Call Assistant

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android%20%7C%20Web-blue?style=for-the-badge&logo=android" alt="Platform" />
  <img src="https://img.shields.io/badge/AI%20Engine-Google%20Gemini%203.6%20Flash-4285F4?style=for-the-badge&logo=google" alt="Gemini AI" />
  <img src="https://img.shields.io/badge/Database-MongoDB%20Atlas-47A248?style=for-the-badge&logo=mongodb" alt="MongoDB Atlas" />
  <img src="https://img.shields.io/badge/Backend-FastAPI%20%26%20Node.js-009688?style=for-the-badge&logo=fastapi" alt="Backend" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose" alt="Compose UI" />
</p>

---

## 📲 Download CallMate AI Android App

Get the latest build directly and install it on any Android device (Android 8.0+):

👉 **[📥 Download CallMate_AI_v1.0.0_debug.apk](./CallMate_AI_v1.0.0_debug.apk)** *(18.8 MB)*

```bash
# Direct clone & install via ADB
git clone https://github.com/sanjana71006/CallAgent.git
cd CallAgent
adb install CallMate_AI_v1.0.0_debug.apk
```

---

## 🌟 Overview

**CallMate AI** is an intelligent, real-time agentic phone call assistant that screens incoming phone calls from unknown numbers, transcribes live conversations, filters telemarketing scams, and coordinates package deliveries while preserving 100% data privacy.

```
                    ┌────────────────────────┐
                    │  Incoming Phone Call   │
                    └───────────┬────────────┘
                                │
                 Is number in Phone Contacts?
                                │
               ┌────────────────┴────────────────┐
             YES                                NO
               │                                 │
     [ Saved Contact ]                 [ Unknown / Spam ]
   • Displays Contact Name           • Gemini AI Assistant answers
   • Rings directly to you           • Asks: Name & Purpose
   • Bypasses AI screening           • Live Speech-to-Text transcript
                                     • Lets you "Take Call" anytime
```

---

## 🚀 Key Features

### 1. 🤖 Live Agentic Call Screening (Google Gemini `gemini-3.6-flash`)
- **Natural Two-Way Voice:** Greets callers politely, inquires about their identity and intent, and answers follow-up questions intelligently.
- **Real-Time Live Transcription:** Streams transcripts word-for-word onto your phone screen as the caller speaks.
- **One-Tap Call Takeover:** Take over the live call at any second with zero latency.
- **Automated Structured Summaries:** Instantly categorizes calls into `DELIVERY`, `RECRUITMENT`, `BANKING`, `SPAM`, or `EMERGENCY` with action items.

### 2. 👥 Native Phone Contacts Integration
- **Contacts Whitelist:** Integrates with Android's `ContactsContract` API. Calls from known family, friends, and colleagues bypass screening and ring directly.
- **Automatic Caller Identification:** Displays real contact names while automatically routing unknown numbers to the AI screening engine.

### 3. 📍 Automatic Live GPS & Street Address Resolution
- **Silent Background GPS Tracking:** Seamlessly captures device coordinates when allowed.
- **Reverse Geocoding:** Automatically translates raw latitude/longitude into human-readable street names, neighbourhoods, cities, and pincodes (e.g. *Hanuman Nagar, Guntur, Andhra Pradesh 522001*).
- **Courier Delivery Guidance:** When delivery drivers call, the assistant provides your exact drop-off address and notes automatically.

### 4. 🚨 Global Crowdsourced Spam Network & UI Filtering
- **Community Threat Registry:** Backed by MongoDB Atlas (`SpamNumber` model). When any user flags a telemarketer or phishing number, all community users are protected.
- **Pre-Call Threat Warning:** Incoming call alert displays: `🚨 Likely Spam (X Community Reports)`.
- **Dedicated UI Filter Tabs:** Filter your call log with one tap:
  - `📞 All`
  - `🛡️ Screened`
  - `🚨 Spam`

### 5. 🔒 Strict On-Device Privacy Architecture
- **Zero Chat Transcripts on Cloud:** Verbatim conversations and summaries are stored **exclusively on-device** in local encrypted Room SQLite (`callmate_ai.db`).
- **MongoDB Atlas Cloud:** Stores only authentication identity, custom screening instructions, saved courier addresses, and community spam numbers.

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          CALLMATE AI ARCHITECTURE                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  📱 Android Native App (Jetpack Compose / Kotlin)                       │
│     ├── UI Layer: Compose Screens & Navigation Dock                     │
│     ├── Core Layer: ContactsManager, LocationService, Audio (STT/TTS)   │
│     └── Local DB: Room SQLite (Verbatim Transcripts & Summaries)        │
│                                                                         │
│  🌐 Web Experience & Simulator (Port 8000)                              │
│     └── Interactive Mobile Shell, Web Speech Mic, Live Simulation       │
│                                                                         │
│  ⚡ FastAPI AI Core (Port 8000 - Python)                                 │
│     ├── /api/v1/ai/chat        -> Gemini Live Screening Engine          │
│     ├── /api/v1/ai/classify    -> Multi-category Intent Classifier      │
│     └── /api/v1/ai/summarize   -> Structured Executive Summaries        │
│                                                                         │
│  ☁️ Node.js Cloud Backend (Port 5000 - Express & Mongoose)              │
│     ├── /api/auth/*            -> JWT Authentication                    │
│     ├── /api/users/*           -> Profile, Addresses, GPS & Rules       │
│     └── /api/spam/*            -> Crowdsourced Spam Registry            │
│                                                                         │
│  🍃 MongoDB Atlas Cloud Cluster                                         │
│     ├── users                  -> User profiles, addresses, rules       │
│     └── spamnumbers            -> Global community reported numbers     │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 📋 Android Device Permissions

| Permission | Real-Time Functionality |
| :--- | :--- |
| **`android.permission.READ_CONTACTS`** | Reads device address book to let saved contacts ring through directly. |
| **`android.permission.ACCESS_FINE_LOCATION`** | Captures GPS coordinates for delivery driver address guidance. |
| **`android.permission.RECORD_AUDIO`** | Real-time speech recognition for live caller-assistant conversations. |
| **`android.permission.READ_PHONE_STATE`** | Detects incoming call ring state and phone numbers. |
| **`android.permission.POST_NOTIFICATIONS`** | Sends instant executive alerts upon screening completion. |

---

## ⚙️ Quickstart & Setup Guide

### 1. Prerequisites
- **Python 3.10+**
- **Node.js v18+**
- **Android Studio** (Optional, for compiling from source)
- **Google Gemini API Key**

---

### 2. Backend Installation

#### Step A: Python AI Server (FastAPI)
```bash
cd backend
python -m venv venv
# On Windows:
venv\Scripts\activate
# On Linux/macOS:
source venv/bin/activate

pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

#### Step B: Node.js Cloud Server (Express + MongoDB Atlas)
```bash
cd backend
npm install
node src/server.js
```

---

### 3. Environment Variables (`backend/.env`)

```env
PORT=5000
MONGODB_URI=mongodb+srv://<username>:<password>@cluster0.kukhjf4.mongodb.net/callmate_ai
JWT_SECRET=callmate_super_secret_jwt_key_2026

AI_PROVIDER=gemini
GEMINI_API_KEY=your_gemini_api_key_here
GEMINI_MODEL=gemini-3.6-flash
```

---

### 4. Running the Android App from Source

```bash
cd android
# Build debug APK
./gradlew assembleDebug

# Install on connected device or emulator
./gradlew installDebug
```

---

## 📱 How to Use on Your Phone

1. **Install APK:** Download and install [`CallMate_AI_v1.0.0_debug.apk`](https://github.com/sanjana71006/CallAgent).
2. **Grant Permissions:** Allow Contacts, Location, and Microphone when prompted.
3. **Connect to Backend:** Ensure your phone is connected to the same Wi-Fi network as your PC (`http://<YOUR_PC_IP>:8000`).
4. **Experience Live AI:** Tap **"Talk to your assistant"** to test live two-way AI call screening!

---

## 🤝 Community & Contributions

- **Repository:** [https://github.com/sanjana71006/CallAgent](https://github.com/sanjana71006/CallAgent)
- **Issues & Pull Requests:** Contributions, bug reports, and feature suggestions are welcome!

---

## 📄 License

Distributed under the **MIT License**. See `LICENSE` for more information.
