from typing import Dict, Any, Optional
from app.core.config import settings
from app.core.logging import logger
from app.services.ai_provider import AiProvider
from app.services.ollama_provider import OllamaProvider
from app.services.mock_provider import MockAiProvider
from app.services.gemini_provider import GeminiProvider
from app.models.chat import ChatRequest, ChatResponse
from app.models.classification import ClassifyRequest, ClassifyResponse
from app.models.summary import SummarizeRequest, SummarizeResponse

class AiService:
    """
    Main orchestration service for AI interactions in CallMate AI.
    Handles dynamic provider selection (Google Gemini, Ollama, or Mock) and graceful fallback.
    """
    
    def __init__(self):
        self.mock_provider = MockAiProvider()
        self.ollama_provider = OllamaProvider()
        self.gemini_provider = GeminiProvider()
        
    @property
    def current_provider(self) -> AiProvider:
        provider = settings.AI_PROVIDER.lower()
        if provider == "gemini":
            return self.gemini_provider
        elif provider == "mock":
            return self.mock_provider
        return self.ollama_provider

    async def chat(self, request: ChatRequest) -> ChatResponse:
        try:
            return await self.current_provider.chat(request)
        except Exception as e:
            if settings.FALLBACK_TO_MOCK_ON_ERROR:
                logger.warning(f"Primary AI provider failed ({e}). Gracefully falling back to Mock provider.")
                return await self.mock_provider.chat(request)
            raise

    async def classify(self, request: ClassifyRequest) -> ClassifyResponse:
        try:
            return await self.current_provider.classify(request)
        except Exception as e:
            if settings.FALLBACK_TO_MOCK_ON_ERROR:
                logger.warning(f"Primary AI provider failed ({e}). Gracefully falling back to Mock provider.")
                return await self.mock_provider.classify(request)
            raise

    async def summarize(self, request: SummarizeRequest) -> SummarizeResponse:
        try:
            return await self.current_provider.summarize(request)
        except Exception as e:
            if settings.FALLBACK_TO_MOCK_ON_ERROR:
                logger.warning(f"Primary AI provider failed ({e}). Gracefully falling back to Mock provider.")
                return await self.mock_provider.summarize(request)
            raise

    async def check_health(self) -> Dict[str, Any]:
        return await self.current_provider.check_health()

ai_service = AiService()
