from fastapi import APIRouter
from app.services.ai_service import ai_service
from app.core.config import settings

router = APIRouter()

@router.get("/health", summary="Health check and status")
async def health():
    ai_health = await ai_service.check_health()
    return {
        "status": "healthy",
        "app_name": settings.PROJECT_NAME,
        "version": settings.VERSION,
        "ai": ai_health
    }
