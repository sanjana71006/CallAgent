from fastapi import APIRouter, HTTPException
from app.models.summary import SummarizeRequest, SummarizeResponse
from app.services.ai_service import ai_service
from app.core.logging import logger

router = APIRouter()

@router.post("/summarize", response_model=SummarizeResponse, summary="Generate structured post-call executive summary")
async def summarize(request: SummarizeRequest):
    try:
        return await ai_service.summarize(request)
    except Exception as e:
        logger.error(f"Error processing call summary: {e}")
        raise HTTPException(status_code=500, detail=f"AI summary error: {str(e)}")
