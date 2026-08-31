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

class OllamaProvider(AiProvider):
    """
    Ollama AI provider communicating with local Ollama daemon (REST API /api/chat or /api/generate).
    Includes JSON schema enforcement, sanitization, and regex fallback extraction.
    """
    
    def __init__(self, base_url: Optional[str] = None, model: Optional[str] = None):
        self.base_url = (base_url or settings.OLLAMA_BASE_URL).rstrip("/")
        self.model = model or settings.AI_MODEL
        self.timeout = httpx.Timeout(30.0, connect=5.0)

    async def _generate(self, prompt: str, system: Optional[str] = None) -> str:
        url = f"{self.base_url}/api/generate"
        payload = {
            "model": self.model,
            "prompt": prompt,
            "stream": False,
            "options": {
                "temperature": 0.3,
                "top_p": 0.9,
            }
        }
        if system:
            payload["system"] = system
            
        async with httpx.AsyncClient(timeout=self.timeout) as client:
            response = await client.post(url, json=payload)
            response.raise_for_status()
            data = response.json()
            return data.get("response", "").strip()

    def _extract_json(self, raw_text: str) -> Dict[str, Any]:
        """Extract and parse JSON safely from model output."""
        try:
            return json.loads(raw_text)
        except json.JSONDecodeError:
            pass
            
        match = re.search(r'\{.*\}', raw_text, re.DOTALL)
        if match:
            try:
                return json.loads(match.group(0))
            except json.JSONDecodeError:
                pass
                
        raise ValueError(f"Could not parse valid JSON from output: {raw_text}")

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
            # Remove any unwanted speaker prefix if model echoes it
            cleaned = re.sub(r'^(AI ASSISTANT|AI|CallMate):\s*', '', raw_response, flags=re.IGNORECASE).strip()
            
            # Determine if call concluded
            is_done = any(w in cleaned.lower() for w in ["goodbye", "have a great day", "alerting the user immediately", "bye"])
            return ChatResponse(
                response=cleaned,
                suggested_action="Listening",
                is_call_complete=is_done
            )
        except Exception as e:
            logger.error(f"Ollama chat generation failed: {e}")
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
            
            # Validate against Enum
            valid_category = CallCategory[category_str] if category_str in CallCategory.__members__ else CallCategory.UNKNOWN
            valid_importance = ImportanceLevel[importance_str] if importance_str in ImportanceLevel.__members__ else ImportanceLevel.MEDIUM
            
            return ClassifyResponse(
                category=valid_category,
                importance=valid_importance,
                confidence=float(data.get("confidence", 0.9)),
                reason=str(data.get("reason", "Call evaluated based on conversation context.")),
                is_spam=bool(data.get("is_spam", False))
            )
        except Exception as e:
            logger.error(f"Ollama classification failed: {e}")
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
                executive_summary=str(data.get("executive_summary", f"Call with {caller_name_or_phone} screened by CallMate AI."))
            )
        except Exception as e:
            logger.error(f"Ollama summary generation failed: {e}")
            raise

    async def check_health(self) -> Dict[str, Any]:
        try:
            async with httpx.AsyncClient(timeout=httpx.Timeout(3.0)) as client:
                resp = await client.get(f"{self.base_url}/api/tags")
                if resp.status_code == 200:
                    models_data = resp.json().get("models", [])
                    model_names = [m.get("name") for m in models_data]
                    return {
                        "status": "healthy",
                        "provider": "ollama",
                        "base_url": self.base_url,
                        "selected_model": self.model,
                        "available_models": model_names,
                        "ollama_available": True
                    }
        except Exception as e:
            logger.warning(f"Ollama health check unreachable: {e}")
            
        return {
            "status": "degraded",
            "provider": "ollama (unreachable - fallback to mock)",
            "base_url": self.base_url,
            "selected_model": self.model,
            "available_models": [],
            "ollama_available": False
        }
