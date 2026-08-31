from pydantic import BaseModel, Field
from typing import List, Optional
from app.models.chat import ConversationTurn
from app.models.classification import CallCategory, ImportanceLevel

class SummarizeRequest(BaseModel):
    call_id: Optional[str] = "sim-001"
    conversation: List[ConversationTurn]
    caller_phone: Optional[str] = None
    caller_name: Optional[str] = None

class SummarizeResponse(BaseModel):
    caller: str = "Unknown Caller"
    purpose: str
    important_details: str
    recommended_action: str
    category: CallCategory = CallCategory.UNKNOWN
    importance: ImportanceLevel = ImportanceLevel.MEDIUM
    is_spam: bool = False
    executive_summary: str
