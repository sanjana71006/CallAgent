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
                response=f"Hello, this is {request.assistant_name}, screening this call on behalf of the user. How may I help you today?",
                suggested_action="Listen to caller"
            )
        
        last_turn = turns[-1]
        text_lower = last_turn.text.lower().strip()
        caller_turns_count = len([t for t in turns if t.speaker == "caller"])
        
        # 1. Couriers & Deliveries
        if any(w in text_lower for w in ["otp", "pin", "verification code"]):
            return ChatResponse(
                response="For security, OTPs cannot be provided over a phone call. Please leave the package at the doorstep or with building security.",
                suggested_action="OTP security guardrail activated"
            )
        elif any(w in text_lower for w in ["delivery", "package", "parcel", "courier", "amazon", "swiggy", "zomato", "blinkit", "gate", "door"]):
            if any(w in text_lower for w in ["outside", "arrived", "here", "reach", "flat", "door"]):
                return ChatResponse(
                    response="Thank you for the delivery! Please leave the package right at the doorstep or security desk. I have alerted the user.",
                    suggested_action="Drop-off instruction delivered",
                    is_call_complete=True
                )
            return ChatResponse(
                response="Thank you for delivering! Please leave the package at the doorstep or with security. Do you need any specific directions?",
                suggested_action="Awaiting delivery confirmation"
            )

        # 2. Interviews & Recruitment
        elif any(w in text_lower for w in ["interview", "resume", "hr", "hiring", "job", "recruiter", "position", "candidate"]):
            if caller_turns_count <= 1:
                return ChatResponse(
                    response="Thank you for reaching out regarding this opportunity. Which company and role is this for, and what is the preferred callback time?",
                    suggested_action="Awaiting interview details"
                )
            else:
                return ChatResponse(
                    response="Got it! I have recorded your company notes and interview callback schedule for the candidate. Have a wonderful day!",
                    suggested_action="Interview details logged",
                    is_call_complete=True
                )

        # 3. Urgency & Emergencies
        elif any(w in text_lower for w in ["urgent", "emergency", "hospital", "doctor", "accident", "police", "asap"]):
            return ChatResponse(
                response="Understood, I am marking this call as high priority and alerting the user immediately.",
                suggested_action="Escalate to user immediately",
                is_call_complete=True
            )

        # 4. Telemarketing, Sales, & Scams
        elif any(w in text_lower for w in ["loan", "credit card", "investment", "insurance", "crypto", "free gift", "winner", "cash prize"]):
            return ChatResponse(
                response="Thank you for calling, but the user is not interested in marketing or commercial offers. Please remove this number from your list. Goodbye.",
                suggested_action="Spam/Sales screening",
                is_call_complete=True
            )

        # 5. Work & Projects
        elif any(w in text_lower for w in ["meeting", "project", "deadline", "client", "office", "report"]):
            return ChatResponse(
                response="Thank you for the update. Could you please leave a brief message regarding the agenda so I can pass it along promptly?",
                suggested_action="Work inquiry logged"
            )

        # 6. Identity & AI Assistant queries
        elif any(w in text_lower for w in ["who are you", "is this an ai", "are you a bot", "robot", "human"]):
            return ChatResponse(
                response=f"I am {request.assistant_name}, an AI assistant screening this call on behalf of the user. How may I assist you?",
                suggested_action="AI disclosure"
            )

        # 7. Greetings
        elif any(w in text_lower for w in ["hello", "hi", "hey", "can i speak", "is this"]):
            return ChatResponse(
                response="Hello! The user is currently unavailable. May I ask what this call is regarding so I can relay your message?",
                suggested_action="Identify caller purpose"
            )

        # 8. Farewells
        elif any(w in text_lower for w in ["bye", "thank you", "goodbye", "that's all", "talk later", "nothing else"]):
            return ChatResponse(
                response="Thank you for calling. I have saved our conversation and notified the user. Have a great day!",
                suggested_action="End call",
                is_call_complete=True
            )

        # 9. Conversational Flow
        else:
            if caller_turns_count == 1:
                return ChatResponse(
                    response="Thank you for providing that. May I take down your name and callback number?",
                    suggested_action="Awaiting contact info"
                )
            elif caller_turns_count == 2:
                return ChatResponse(
                    response="Understood, I have recorded your message. Is there any specific time you would prefer a callback?",
                    suggested_action="Awaiting callback time"
                )
            else:
                return ChatResponse(
                    response="Thank you, I have logged all the details from this call and will update the user right away. Goodbye!",
                    suggested_action="End call",
                    is_call_complete=True
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
