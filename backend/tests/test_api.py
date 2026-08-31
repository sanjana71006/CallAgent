import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_root_endpoint():
    response = client.get("/")
    assert response.status_code == 200
    assert "CallMate AI" in response.text

def test_health_endpoint():
    response = client.get("/api/v1/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "healthy"
    assert "ai" in data

def test_config_endpoint():
    response = client.get("/api/v1/config")
    assert response.status_code == 200
    data = response.json()
    assert "PERSONAL" in data["supported_categories"]
    assert "URGENT" in data["supported_importance_levels"]

def test_chat_endpoint_screening():
    payload = {
        "call_id": "test-call-1",
        "assistant_name": "CallMate AI",
        "personality": "polite",
        "conversation": [
            {
                "speaker": "caller",
                "text": "Hello, I am calling to schedule a technical interview for the senior engineer role."
            }
        ]
    }
    response = client.post("/api/v1/ai/chat", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "response" in data
    assert len(data["response"]) > 0

def test_classify_recruitment_call():
    payload = {
        "call_id": "test-call-2",
        "conversation": [
            {
                "speaker": "caller",
                "text": "Hi, I am reaching out from Google HR regarding your job application."
            },
            {
                "speaker": "ai",
                "text": "Thank you for calling. Could you specify the position and callback time?"
            }
        ]
    }
    response = client.post("/api/v1/ai/classify", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["category"] == "RECRUITMENT"
    assert data["importance"] in ["HIGH", "URGENT", "MEDIUM"]
    assert data["is_spam"] is False

def test_classify_spam_call():
    payload = {
        "call_id": "test-call-3",
        "conversation": [
            {
                "speaker": "caller",
                "text": "Congratulations! You have won a pre-approved zero interest personal loan with cash prize!"
            }
        ]
    }
    response = client.post("/api/v1/ai/classify", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["category"] == "SALES"
    assert data["is_spam"] is True

def test_summarize_delivery_call():
    payload = {
        "call_id": "test-call-4",
        "caller_name": "Amazon Delivery",
        "conversation": [
            {
                "speaker": "caller",
                "text": "Hello, I am at the gate with your Amazon parcel package."
            },
            {
                "speaker": "ai",
                "text": "Please leave the package with security at the gate."
            }
        ]
    }
    response = client.post("/api/v1/ai/summarize", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["category"] == "DELIVERY"
    assert "package" in data["purpose"].lower() or "delivery" in data["purpose"].lower()
    assert len(data["recommended_action"]) > 0
