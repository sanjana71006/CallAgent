import json
import re
import httpx
from typing import Dict, Any, Optional
from app.core.config import settings
from app.core.logging import logger
from app.services.ai_provider import AiProvider
from app.models.chat import ChatRequest, ChatResponse
from app.models.classification import ClassifyRequest, ClassifyResponse, CallCategory, ImportanceLevel
from app.models.summary import SummarizeRequest, SummarizeResponse
from app.prompts.system_prompts import CALLMATE_SYSTEM_PROMPT, CLASSIFICATION_PROMPT, SUMMARY_PROMPT

class GeminiProvider(AiProvider):
    """
    Google Gemini AI Provider communicating with the Gemini REST API.
    Supports chat screening, call classification, and structured summarization.
    """
    
    def __init__(self, api_key: Optional[str] = None, model: Optional[str] = None):
        self.api_key = api_key or settings.GEMINI_API_KEY
        self.model = model or settings.GEMINI_MODEL
        self.timeout = httpx.Timeout(6.0, connect=2.5)

    async def _generate(self, prompt: str, system: Optional[str] = None) -> str:
        candidate_models = [self.model, "gemini-flash-lite-latest", "gemini-flash-latest"]
        # Deduplicate while preserving order
        unique_models = []
        for m in candidate_models:
            if m and m not in unique_models:
                unique_models.append(m)
        
        last_error = None
        for m in unique_models:
            url = f"https://generativelanguage.googleapis.com/v1beta/models/{m}:generateContent?key={self.api_key}"
            
            payload: Dict[str, Any] = {
                "contents": [
                    {
                        "parts": [{"text": prompt}]
                    }
                ],
                "generationConfig": {
                    "temperature": 0.4,
                    "topP": 0.9,
                    "maxOutputTokens": 1024
                }
            }
            
            if system:
                payload["system_instruction"] = {
                    "parts": [{"text": system}]
                }
                
            try:
                async with httpx.AsyncClient(timeout=self.timeout) as client:
                    response = await client.post(url, json=payload)
                    if response.status_code == 200:
                        data = response.json()
                        candidates = data.get("candidates", [])
                        if candidates:
                            parts = candidates[0].get("content", {}).get("parts", [])
                            if parts:
                                return parts[0].get("text", "").strip()
                    else:
                        last_error = f"HTTP {response.status_code}: {response.text[:120]}"
            except Exception as e:
                last_error = str(e)
                continue
                
        raise ValueError(f"Gemini API generation failed across models: {last_error}")

    def _extract_json(self, raw_text: str) -> Dict[str, Any]:
        """Extract and parse JSON safely from model output, handling markdown code blocks."""
        clean_text = re.sub(r'^```(?:json)?\s*', '', raw_text.strip(), flags=re.IGNORECASE)
        clean_text = re.sub(r'\s*```$', '', clean_text.strip())
        
        try:
            return json.loads(clean_text)
        except json.JSONDecodeError:
            pass
            
        match = re.search(r'\{.*\}', clean_text, re.DOTALL)
        if match:
            try:
                return json.loads(match.group(0))
            except json.JSONDecodeError:
                pass
                
        raise ValueError(f"Could not parse valid JSON from Gemini output: {raw_text}")

    async def chat(self, request: ChatRequest) -> ChatResponse:
        system = CALLMATE_SYSTEM_PROMPT.format(
            assistant_name=request.assistant_name or "CallMate AI",
            personality=request.personality or "polite and professional",
            caller_phone=request.caller_phone or "Unknown",
            caller_name=request.caller_name or "Unknown"
        )
        
        # Build conversational transcript
        dialogue = []
        for turn in request.conversation:
            speaker_label = "CALLER" if turn.speaker == "caller" else "AI ASSISTANT"
            dialogue.append(f"{speaker_label}: {turn.text}")
            
        prompt = (
            "Current conversation history:\n" + 
            ("\n".join(dialogue) if dialogue else "Caller just connected.") +
            "\n\nGenerate the next AI ASSISTANT phone response (1-3 sentences maximum):"
        )
        
        try:
            raw_response = await self._generate(prompt=prompt, system=system)
            cleaned = re.sub(r'^(AI ASSISTANT|AI|CallMate):\s*', '', raw_response, flags=re.IGNORECASE).strip()
            
            is_done = any(w in cleaned.lower() for w in ["goodbye", "have a great day", "alerting the user immediately", "bye", "take care"])
            return ChatResponse(
                response=cleaned,
                suggested_action="Listening",
                is_call_complete=is_done
            )
        except Exception as e:
            logger.error(f"Gemini chat generation failed: {e}")
            raise

    async def classify(self, request: ClassifyRequest) -> ClassifyResponse:
        dialogue = [f"{turn.speaker}: {turn.text}" for turn in request.conversation]
        transcript_str = "\n".join(dialogue)
        prompt = CLASSIFICATION_PROMPT.format(transcript=transcript_str)
        
        try:
            raw_json_str = await self._generate(prompt=prompt)
            data = self._extract_json(raw_json_str)
            
            category_str = str(data.get("category", "UNKNOWN")).upper()
            importance_str = str(data.get("importance", "MEDIUM")).upper()
            
            valid_category = CallCategory[category_str] if category_str in CallCategory.__members__ else CallCategory.UNKNOWN
            valid_importance = ImportanceLevel[importance_str] if importance_str in ImportanceLevel.__members__ else ImportanceLevel.MEDIUM
            
            return ClassifyResponse(
                category=valid_category,
                importance=valid_importance,
                confidence=float(data.get("confidence", 0.95)),
                reason=str(data.get("reason", "Call evaluated by Google Gemini based on conversation context.")),
                is_spam=bool(data.get("is_spam", False))
            )
        except Exception as e:
            logger.error(f"Gemini classification failed: {e}")
            raise

    async def summarize(self, request: SummarizeRequest) -> SummarizeResponse:
        dialogue = [f"{turn.speaker}: {turn.text}" for turn in request.conversation]
        transcript_str = "\n".join(dialogue)
        caller_name_or_phone = request.caller_name or request.caller_phone or "Unknown Caller"
        
        prompt = SUMMARY_PROMPT.format(
            transcript=transcript_str,
            caller_name_or_phone=caller_name_or_phone
        )
        
        try:
            raw_json_str = await self._generate(prompt=prompt)
            data = self._extract_json(raw_json_str)
            
            category_str = str(data.get("category", "UNKNOWN")).upper()
            importance_str = str(data.get("importance", "MEDIUM")).upper()
            
            valid_category = CallCategory[category_str] if category_str in CallCategory.__members__ else CallCategory.UNKNOWN
            valid_importance = ImportanceLevel[importance_str] if importance_str in ImportanceLevel.__members__ else ImportanceLevel.MEDIUM
            
            return SummarizeResponse(
                caller=str(data.get("caller", caller_name_or_phone)),
                purpose=str(data.get("purpose", "General inquiry")),
                important_details=str(data.get("important_details", "No key details recorded")),
                recommended_action=str(data.get("recommended_action", "Review call transcript")),
                category=valid_category,
                importance=valid_importance,
                is_spam=bool(data.get("is_spam", False)),
                executive_summary=str(data.get("executive_summary", f"Call with {caller_name_or_phone} screened by CallMate AI (Gemini Engine)."))
            )
        except Exception as e:
            logger.error(f"Gemini summary generation failed: {e}")
            raise

    async def check_health(self) -> Dict[str, Any]:
        try:
            url = f"https://generativelanguage.googleapis.com/v1beta/models/{self.model}?key={self.api_key}"
            async with httpx.AsyncClient(timeout=httpx.Timeout(4.0)) as client:
                resp = await client.get(url)
                if resp.status_code == 200:
                    return {
                        "status": "healthy",
                        "provider": "gemini",
                        "model": self.model,
                        "gemini_available": True
                    }
                else:
                    return {
                        "status": "degraded",
                        "provider": "gemini",
                        "error": resp.text[:150],
                        "gemini_available": False
                    }
        except Exception as e:
            logger.warning(f"Gemini health check error: {e}")
            return {
                "status": "degraded",
                "provider": "gemini (unreachable - fallback to mock)",
                "error": str(e),
                "gemini_available": False
            }
