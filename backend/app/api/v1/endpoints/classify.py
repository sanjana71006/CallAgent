from fastapi import APIRouter, HTTPException
from app.models.classification import ClassifyRequest, ClassifyResponse
from app.services.ai_service import ai_service
from app.core.logging import logger

router = APIRouter()

@router.post("/classify", response_model=ClassifyResponse, summary="Classify call category, importance, and spam status")
async def classify(request: ClassifyRequest):
    try:
        return await ai_service.classify(request)
    except Exception as e:
        logger.error(f"Error processing classification: {e}")
        raise HTTPException(status_code=500, detail=f"AI classification error: {str(e)}")
