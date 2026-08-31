from fastapi import APIRouter
from app.core.config import settings

router = APIRouter()

@router.get("/config", summary="Retrieve active backend configuration and prompt options")
async def get_config():
    return {
        "app_name": settings.PROJECT_NAME,
        "version": settings.VERSION,
        "ai_provider": settings.AI_PROVIDER,
        "ai_model": settings.AI_MODEL,
        "ollama_base_url": settings.OLLAMA_BASE_URL,
        "supported_categories": [
            "PERSONAL", "WORK", "RECRUITMENT", "DELIVERY", "BANKING",
            "SERVICE", "SALES", "TELEMARKETING", "SPAM", "UNKNOWN"
        ],
        "supported_importance_levels": ["LOW", "MEDIUM", "HIGH", "URGENT"]
    }
