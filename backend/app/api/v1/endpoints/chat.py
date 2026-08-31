from fastapi import APIRouter, HTTPException
from app.models.chat import ChatRequest, ChatResponse
from app.services.ai_service import ai_service
from app.core.logging import logger

router = APIRouter()

@router.post("/chat", response_model=ChatResponse, summary="Generate conversational AI phone screening response")
async def chat(request: ChatRequest):
    try:
        return await ai_service.chat(request)
    except Exception as e:
        logger.error(f"Error processing chat request: {e}")
        raise HTTPException(status_code=500, detail=f"AI chat generation error: {str(e)}")
