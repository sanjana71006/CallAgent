from abc import ABC, abstractmethod
from typing import List, Dict, Any, Optional
from app.models.chat import ChatRequest, ChatResponse
from app.models.classification import ClassifyRequest, ClassifyResponse
from app.models.summary import SummarizeRequest, SummarizeResponse

class AiProvider(ABC):
    """Abstract base class for all AI providers."""
    
    @abstractmethod
    async def chat(self, request: ChatRequest) -> ChatResponse:
        """Generate conversational screening response for the ongoing call."""
        pass
        
    @abstractmethod
    async def classify(self, request: ClassifyRequest) -> ClassifyResponse:
        """Classify the call category, importance, and spam status."""
        pass
        
    @abstractmethod
    async def summarize(self, request: SummarizeRequest) -> SummarizeResponse:
        """Generate structured post-call executive summary."""
        pass
        
    @abstractmethod
    async def check_health(self) -> Dict[str, Any]:
        """Check provider connectivity and status."""
        pass
