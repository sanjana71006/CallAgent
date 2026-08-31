from pydantic import BaseModel, Field
from typing import List, Optional
from enum import Enum

class Speaker(str, Enum):
    CALLER = "caller"
    AI = "ai"
    USER = "user"

class ConversationTurn(BaseModel):
    speaker: Speaker
    text: str

class ChatRequest(BaseModel):
    call_id: Optional[str] = "sim-001"
    conversation: List[ConversationTurn]
    caller_phone: Optional[str] = None
    caller_name: Optional[str] = None
    assistant_name: Optional[str] = "CallMate AI"
    personality: Optional[str] = "polite and professional"

class ChatResponse(BaseModel):
    response: str
    suggested_action: Optional[str] = None
    is_call_complete: bool = False
