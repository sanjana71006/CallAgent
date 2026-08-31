# CallMate AI - Setup & Installation Guide

This guide walks you through setting up CallMate AI Android App, Local FastAPI Backend, and Local Ollama AI Engine.

---

## Prerequisites

1. **Operating System**: Windows / macOS / Linux
2. **Java Development Kit (JDK)**: JDK 17, 21, or 22
3. **Android SDK / Android Studio**: Android SDK Platform 34 or 35/36
4. **Python**: Python 3.10+ (Python 3.11 recommended)
5. **Ollama (Optional for local LLM inference, Mock engine available out of the box)**:
   - Download Ollama from https://ollama.com
   - Pull model: `ollama pull qwen2.5:0.5b` or `ollama pull llama3.2`

---

## 1. Backend Setup

1. Open a terminal in the `backend/` directory:
   ```bash
   cd backend
   ```
2. Install Python dependencies:
   ```bash
   pip install -r requirements.txt
   ```
3. Start the FastAPI server:
   ```bash
   uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
   ```
4. Verify backend health in your browser:
   ```
   http://localhost:8000/api/v1/health
   ```
   Interactive Swagger docs are available at `http://localhost:8000/docs`.

---

## 2. Ollama Configuration

1. Launch Ollama in the background:
   ```bash
   ollama serve
   ```
2. Pull the default fast screening model:
   ```bash
   ollama pull qwen2.5:0.5b
   ```
3. If Ollama is not installed or running, CallMate AI will automatically and gracefully fall back to the built-in intelligent heuristic screening engine without crashing or disrupting calls.

---

## 3. Android App Setup

1. Open the project in **Android Studio** or navigate to `android/` directory.
2. If building from the terminal:
   ```bash
   cd android
   ./gradlew assembleDebug
   ```
3. Run on an Android Emulator or connected physical device:
   ```bash
   ./gradlew installDebug
   ```

### Emulator Networking Note:
When running on the standard Android Emulator, the Android OS connects to the host machine backend via `http://10.0.2.2:8000`. This is pre-configured in CallMate AI's default network settings and can be modified at any time in **Settings -> AI Provider -> Backend URL**.
