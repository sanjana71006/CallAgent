# CallMate AI - Testing Guide

## 1. Automated Testing Strategy

CallMate AI features a multi-tiered automated test suite verifying AI prompt logic, REST APIs, heuristic engines, database DAOs, repositories, viewmodels, and Compose UI flows.

---

## 2. Running Backend Tests

Navigate to `backend/` and run `pytest`:
```bash
cd backend
python -m pytest tests/ -v
```

### Verified Test Cases:
- `test_root_endpoint`: Verifies service status and API docs link.
- `test_health_endpoint`: Verifies health monitor and AI provider status.
- `test_config_endpoint`: Validates supported categories and importance levels.
- `test_chat_endpoint_screening`: Tests multi-turn AI screening conversation.
- `test_classify_recruitment_call`: Verifies category detection and importance scoring.
- `test_classify_spam_call`: Verifies unsolicited sales detection and spam flagging.
- `test_summarize_delivery_call`: Validates structured JSON recap and action extraction.
- `test_mock_chat_flow`: Validates heuristic phone response generation.
- `test_mock_chat_emergency`: Verifies emergency escalation and instant call completion.
- `test_mock_classify_banking`: Tests financial inquiry categorization.
- `test_ai_service_fallback`: Tests zero-crash failover when Ollama is offline.

---

## 3. Running Android Unit Tests

Run Android unit tests using the Gradle wrapper:
```bash
cd android
./gradlew test
```

## 4. Running Android UI Tests

Run Android connected instrumentation tests:
```bash
cd android
./gradlew connectedAndroidTest
```
