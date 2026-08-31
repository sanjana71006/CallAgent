# CallMate AI Architecture & Design Specification

## 1. Executive Summary

**CallMate AI** ("Let AI answer. You decide.") is an intelligent Android AI phone assistant engineered with Clean Architecture, MVVM presentation patterns, Room local persistence, Jetpack Compose Material 3 UI, on-device Speech-to-Text/Text-to-Speech orchestration, and a local Python FastAPI backend interfacing with Ollama.

The application operates without requiring paid commercial cloud APIs during development or execution.

---

## 2. High-Level System Architecture

```
+-----------------------------------------------------------------------------------+
|                              CallMate AI Android App                              |
|                                                                                   |
|  +-----------------------------------------------------------------------------+  |
|  | Presentation Layer (Jetpack Compose + Material 3 + ViewModels)              |  |
|  | - Splash: Custom CallMate AI branding and animated entrance                 |  |
|  | - Onboarding: 4-step introduction (Assistant, Transcripts, Control, Privacy)|  |
|  | - Home Dashboard: Activity stats, Assistant toggle, Call Simulator action  |  |
|  | - Live Call Screen: Live transcript stream, Audio wave, Takeover trigger   |  |
|  | - Call History: Category filters (Spam, Work, etc.), Search, Call cards     |  |
|  | - Call Details: Full transcript, Structured summary, Actionable next steps  |  |
|  | - Settings: Assistant persona, Ollama host, Voice pitch/rate, Data clear    |  |
|  +-----------------------------------------------------------------------------+  |
|                                         |                                         |
|  +-----------------------------------------------------------------------------+  |
|  | Domain Layer (Clean Architecture)                                           |  |
|  | - Entities: Call, TranscriptMessage, CallCategory, ImportanceLevel           |  |
|  | - UseCases: ProcessCallConversationUseCase, ClassifyCallUseCase,             |  |
|  |             SummarizeCallUseCase, ManageSettingsUseCase                       |  |
|  | - Repositories: CallRepository, SettingsRepository, AiAssistantService        |  |
|  +-----------------------------------------------------------------------------+  |
|                                         |                                         |
|  +-----------------------------------------------------------------------------+  |
|  | Data Layer & Local Storage                                                  |  |
|  | - Room SQLite DB: CallEntity, TranscriptEntity, AssistantSettingsEntity     |  |
|  | - DataStore: User preferences & configuration serializer                     |  |
|  | - Network Client: Retrofit / OkHttp communicating with local FastAPI server |  |
|  +-----------------------------------------------------------------------------+  |
|                                         |                                         |
|  +-----------------------------------------------------------------------------+  |
|  | Core & Hardware Abstractions                                                |  |
|  | - Telephony: CallProvider (SimulatorCallProvider <-> FutureTelephonyProvider|  |
|  | - Speech-to-Text: SpeechToTextManager (Android SpeechRecognizer / Whisper)  |  |
|  | - Text-to-Speech: TextToSpeechManager (Android TTS Engine)                  |  |
|  +-----------------------------------------------------------------------------+  |
+------------------------------------------^----------------------------------------+
                                           | HTTP / REST (Localhost / 10.0.2.2)
+------------------------------------------v----------------------------------------+
|                              CallMate AI Local Backend                             |
|                                (FastAPI + Uvicorn)                                |
|                                                                                   |
|  - Endpoints:                                                                     |
|    - GET  /api/v1/health       -> System status, AI engine availability           |
|    - POST /api/v1/ai/chat      -> Multi-turn phone screening dialogue generation  |
|    - POST /api/v1/ai/classify  -> 10-category classification & spam detection     |
|    - POST /api/v1/ai/summarize -> Structured executive recap & action suggestions |
|    - GET  /api/v1/config       -> Dynamic models and prompt configuration         |
|                                                                                   |
|  - AI Providers:                                                                  |
|    - OllamaProvider (qwen2.5:0.5b / llama3.2 via HTTP)                            |
|    - MockAiProvider (High-precision heuristic offline & testing fallback)         |
+-----------------------------------------------------------------------------------+
```

---

## 3. Conversation & Screening Flow

1. **Call Initiation**: User or Simulator launches incoming call -> UI presents Caller ID, Phone Number, and Actions (`[Listen Live]`, `[Take Call]`, `[Decline]`).
2. **AI Screening**: Assistant greets caller politely and states identity as an AI assistant.
3. **Voice Input (STT)**: Caller speech captured via `SpeechRecognizer` and streamed into the live transcript.
4. **AI Reasoning**: Transcript forwarded to `/api/v1/ai/chat`. Ollama generates concise (1-3 sentences) response avoiding commitments or private info leakage.
5. **Speech Synthesis (TTS)**: Android `TextToSpeech` articulates AI response to caller.
6. **Live Monitoring**: User views transcript in real-time and can tap `[Take Call]` at any point to transition to direct conversation.
7. **Call Termination**: User or AI ends call -> Backend `/api/v1/ai/classify` and `/api/v1/ai/summarize` generate category, importance score, and executive summary notes.
8. **Persistence**: Call record and messages persisted in Room SQLite DB. Home dashboard activity statistics refresh reactively.
