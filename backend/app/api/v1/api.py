from fastapi import APIRouter
from app.api.v1.endpoints import health, chat, classify, summarize, config

api_router = APIRouter()

api_router.include_router(health.router, tags=["Health"])
api_router.include_router(chat.router, prefix="/ai", tags=["AI Conversation"])
api_router.include_router(classify.router, prefix="/ai", tags=["Call Classification"])
api_router.include_router(summarize.router, prefix="/ai", tags=["Call Summary"])
api_router.include_router(config.router, tags=["Configuration"])
