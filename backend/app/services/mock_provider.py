import re
from typing import Dict, Any
from app.services.ai_provider import AiProvider
from app.models.chat import ChatRequest, ChatResponse
from app.models.classification import ClassifyRequest, ClassifyResponse, CallCategory, ImportanceLevel
from app.models.summary import SummarizeRequest, SummarizeResponse

class MockAiProvider(AiProvider):
    """
    Intelligent heuristic mock AI provider for offline use, tests, and demo mode.
    Accurately classifies real-world caller patterns and generates polite phone screening responses.
    """
    
    async def chat(self, request: ChatRequest) -> ChatResponse:
        turns = request.conversation
        if not turns:
            return ChatResponse(
                response=f"Hello, this is {request.assistant_name}. How may I help you today?",
                suggested_action="Listen to caller"
            )
        
        last_turn = turns[-1]
        text_lower = last_turn.text.lower()
        
        # Heuristics based on typical screening dialogues
        if any(w in text_lower for w in ["interview", "resume", "hr", "hiring", "job", "recruiter", "position"]):
            return ChatResponse(
                response="Thank you for calling. Could you please specify the role, company name, and the best callback time?",
                suggested_action="Awaiting interview details"
            )
        elif any(w in text_lower for w in ["delivery", "package", "parcel", "courier", "door", "address", "otp"]):
            if "otp" in text_lower:
                return ChatResponse(
                    response="For security, I cannot share OTPs over the phone. Please leave the package at the doorstep or with security.",
                    suggested_action="OTP security guardrail activated"
                )
            return ChatResponse(
                response="Thank you. Please leave the package at the doorstep or security gate. I will notify the owner.",
                suggested_action="Package drop-off acknowledged"
            )
        elif any(w in text_lower for w in ["urgent", "emergency", "hospital", "doctor", "accident", "asap"]):
            return ChatResponse(
                response="Understood. I am flagging this call as urgent and alerting the user immediately.",
                suggested_action="Escalate to user immediately",
                is_call_complete=True
            )
        elif any(w in text_lower for w in ["loan", "credit card", "investment", "insurance", "crypto", "free gift", "winner"]):
            return ChatResponse(
                response="Thank you, but the user is not interested in marketing or unsolicited promotional offers. Have a good day.",
                suggested_action="Spam/Sales screening",
                is_call_complete=True
            )
        elif any(w in text_lower for w in ["who are you", "is this an ai", "are you a bot", "robot"]):
            return ChatResponse(
                response=f"Yes, I am {request.assistant_name}, an AI assistant screening this call. How can I assist you?",
                suggested_action="AI disclosure"
            )
        elif any(w in text_lower for w in ["bye", "thank you", "goodbye", "that's all", "talk later"]):
            return ChatResponse(
                response="Thank you for calling. I will pass your message along. Have a great day!",
                suggested_action="End call",
                is_call_complete=True
            )
        else:
            return ChatResponse(
                response="Thank you for providing that. May I take a brief message or have your contact details for a callback?",
                suggested_action="Take message"
            )
            
    async def classify(self, request: ClassifyRequest) -> ClassifyResponse:
        full_text = " ".join([turn.text.lower() for turn in request.conversation])
        
        if any(w in full_text for w in ["interview", "hr", "hiring", "job", "recruiter", "position", "candidate"]):
            return ClassifyResponse(
                category=CallCategory.RECRUITMENT,
                importance=ImportanceLevel.HIGH,
                confidence=0.96,
                reason="Caller is discussing a job opportunity or scheduling an interview.",
                is_spam=False
            )
        elif any(w in full_text for w in ["delivery", "package", "parcel", "courier", "amazon", "fedex", "shipment"]):
            return ClassifyResponse(
                category=CallCategory.DELIVERY,
                importance=ImportanceLevel.MEDIUM,
                confidence=0.92,
                reason="Courier or delivery driver communicating package arrival.",
                is_spam=False
            )
        elif any(w in full_text for w in ["urgent", "emergency", "hospital", "doctor", "accident"]):
            return ClassifyResponse(
                category=CallCategory.PERSONAL,
                importance=ImportanceLevel.URGENT,
                confidence=0.98,
                reason="Caller stated an urgent or emergency situation.",
                is_spam=False
            )
        elif any(w in full_text for w in ["loan", "credit card", "insurance", "crypto", "promotion", "special offer", "winner"]):
            return ClassifyResponse(
                category=CallCategory.SALES,
                importance=ImportanceLevel.LOW,
                confidence=0.94,
                reason="Unsolicited commercial telemarketing or sales pitch.",
                is_spam=True
            )
        elif any(w in full_text for w in ["project", "meeting", "deadline", "client", "office", "report", "standup"]):
            return ClassifyResponse(
                category=CallCategory.WORK,
                importance=ImportanceLevel.HIGH,
                confidence=0.90,
                reason="Colleague or business associate discussing work matters.",
                is_spam=False
            )
        elif any(w in full_text for w in ["account", "bank", "transaction", "card", "branch", "statement"]):
            return ClassifyResponse(
                category=CallCategory.BANKING,
                importance=ImportanceLevel.HIGH,
                confidence=0.88,
                reason="Banking or financial service inquiry.",
                is_spam=False
            )
        else:
            return ClassifyResponse(
                category=CallCategory.UNKNOWN,
                importance=ImportanceLevel.MEDIUM,
                confidence=0.75,
                reason="General inquiry or brief interaction.",
                is_spam=False
            )
            
    async def summarize(self, request: SummarizeRequest) -> SummarizeResponse:
        full_text = " ".join([f"{turn.speaker}: {turn.text}" for turn in request.conversation])
        full_text_lower = full_text.lower()
        caller_id = request.caller_name or request.caller_phone or "Unknown Caller"
        
        if "interview" in full_text_lower or "recruiter" in full_text_lower:
            return SummarizeResponse(
                caller=caller_id,
                purpose="Job interview coordination",
                important_details="Caller reached out regarding hiring process and next interview rounds.",
                recommended_action="Review interview schedule and call back recruiter",
                category=CallCategory.RECRUITMENT,
                importance=ImportanceLevel.HIGH,
                is_spam=False,
                executive_summary=f"{caller_id} called regarding an interview opportunity. Requested candidate confirmation and discussion on availability."
            )
        elif "delivery" in full_text_lower or "package" in full_text_lower:
            return SummarizeResponse(
                caller=caller_id,
                purpose="Package delivery notification",
                important_details="Driver confirmed package arrival at building entrance / security gate.",
                recommended_action="Collect package from security or doorstep",
                category=CallCategory.DELIVERY,
                importance=ImportanceLevel.MEDIUM,
                is_spam=False,
                executive_summary=f"{caller_id} delivered a package. Driver was instructed to leave the parcel in the designated secure drop-off location."
            )
        elif "loan" in full_text_lower or "credit card" in full_text_lower or "insurance" in full_text_lower:
            return SummarizeResponse(
                caller=caller_id,
                purpose="Unsolicited telemarketing",
                important_details="Promotional sales call offering financial services.",
                recommended_action="No action needed (Block if repeated)",
                category=CallCategory.SALES,
                importance=ImportanceLevel.LOW,
                is_spam=True,
                executive_summary=f"Automated screening intercepted an unsolicited sales pitch from {caller_id}. AI assistant declined on user's behalf."
            )
        else:
            return SummarizeResponse(
                caller=caller_id,
                purpose="General phone inquiry",
                important_details=f"Conversation turns: {len(request.conversation)} messages exchanged.",
                recommended_action="Review transcript if required",
                category=CallCategory.UNKNOWN,
                importance=ImportanceLevel.MEDIUM,
                is_spam=False,
                executive_summary=f"{caller_id} reached out through CallMate AI. Brief dialogue completed and stored in local history."
            )
            
    async def check_health(self) -> Dict[str, Any]:
        return {
            "status": "healthy",
            "provider": "mock",
            "model": "built-in heuristic engine",
            "ollama_available": False
        }
