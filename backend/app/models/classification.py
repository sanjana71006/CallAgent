from pydantic import BaseModel, Field
from typing import List, Optional
from enum import Enum
from app.models.chat import ConversationTurn

class CallCategory(str, Enum):
    PERSONAL = "PERSONAL"
    WORK = "WORK"
    RECRUITMENT = "RECRUITMENT"
    DELIVERY = "DELIVERY"
    BANKING = "BANKING"
    SERVICE = "SERVICE"
    SALES = "SALES"
    TELEMARKETING = "TELEMARKETING"
    SPAM = "SPAM"
    UNKNOWN = "UNKNOWN"

class ImportanceLevel(str, Enum):
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"
    URGENT = "URGENT"

class ClassifyRequest(BaseModel):
    call_id: Optional[str] = "sim-001"
    conversation: List[ConversationTurn]
    caller_phone: Optional[str] = None
    caller_name: Optional[str] = None

class ClassifyResponse(BaseModel):
    category: CallCategory = CallCategory.UNKNOWN
    importance: ImportanceLevel = ImportanceLevel.MEDIUM
    confidence: float = Field(default=0.9, ge=0.0, le=1.0)
    reason: str
    is_spam: bool = False
