import pytest
from app.services.ai_service import ai_service
from app.services.mock_provider import MockAiProvider
from app.models.chat import ChatRequest, ConversationTurn, Speaker
from app.models.classification import ClassifyRequest, CallCategory, ImportanceLevel
from app.models.summary import SummarizeRequest

@pytest.mark.asyncio
async def test_mock_chat_flow():
    provider = MockAiProvider()
    req = ChatRequest(
        conversation=[
            ConversationTurn(speaker=Speaker.CALLER, text="Who is speaking? Is this an AI?")
        ]
    )
    res = await provider.chat(req)
    assert "AI assistant" in res.response

@pytest.mark.asyncio
async def test_mock_chat_emergency():
    provider = MockAiProvider()
    req = ChatRequest(
        conversation=[
            ConversationTurn(speaker=Speaker.CALLER, text="This is urgent emergency hospital calling!")
        ]
    )
    res = await provider.chat(req)
    assert res.is_call_complete is True
    assert "urgent" in res.response.lower()

@pytest.mark.asyncio
async def test_mock_classify_banking():
    provider = MockAiProvider()
    req = ClassifyRequest(
        conversation=[
            ConversationTurn(speaker=Speaker.CALLER, text="Hello, this is your bank branch regarding account statement verification.")
        ]
    )
    res = await provider.classify(req)
    assert res.category == CallCategory.BANKING
    assert res.importance in [ImportanceLevel.HIGH, ImportanceLevel.MEDIUM]

@pytest.mark.asyncio
async def test_ai_service_fallback():
    # Verify ai_service handles requests safely without raising unhandled errors
    req = ChatRequest(
        conversation=[
            ConversationTurn(speaker=Speaker.CALLER, text="Hello, I have an inquiry about your project.")
        ]
    )
    res = await ai_service.chat(req)
    assert isinstance(res.response, str)
    assert len(res.response) > 0
