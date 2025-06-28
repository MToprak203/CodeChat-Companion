from __future__ import annotations

import os
from typing import List, Optional

from fastapi import APIRouter, HTTPException, Depends
from fastapi.responses import StreamingResponse
import logging
from pydantic import BaseModel, Field

from .schemas import ChatMessage

from .service.ai_service import AIService
from .auth import get_current_user


logger = logging.getLogger(__name__)
router = APIRouter()

MODEL_NAME_OR_PATH = os.getenv("MODEL_NAME_OR_PATH", "deepseek-ai/deepseek-coder-6.7b-instruct")
MODEL_PROVIDER = os.getenv("MODEL_PROVIDER", "huggingface")
service = AIService(MODEL_NAME_OR_PATH, MODEL_PROVIDER)


class GenerateRequest(BaseModel):
    conversation_id: Optional[int] = Field(None, alias="conversationId")
    chat_history: Optional[List[ChatMessage]] = Field(None, alias="chatHistory")
    project_files: Optional[List[str]] = Field(None, alias="projectFiles")
    correlation_id: Optional[str] = Field(None, alias="correlationId")
    prompt: Optional[str] = None

    class Config:
        validate_by_name = True


@router.post("/generate")
async def generate(request: GenerateRequest, user: dict = Depends(get_current_user)):
    prompt = request.prompt
    chat_history = request.chat_history or []
    files = request.project_files

    logger.debug(
        "[python:generate] conversationId=%s prompt_len=%s history=%d files=%d",
        request.conversation_id,
        len(prompt) if prompt else 0,
        len(chat_history),
        len(files) if files else 0,
    )

    if not prompt and chat_history:
        prompt = chat_history[-1].content
        chat_history = chat_history[:-1]

    if not prompt:
        raise HTTPException(status_code=400, detail="Prompt missing")

    if request.conversation_id is None:
        raise HTTPException(status_code=400, detail="conversationId required")

    logger.debug("[python:generate] starting stream")
    return StreamingResponse(
        service.stream_response(request.conversation_id, prompt, chat_history, files),
        media_type="text/plain; charset=utf-8",
    )


@router.post("/stop/{conversation_id}")
async def stop_generation(conversation_id: int, user: dict = Depends(get_current_user)):
    logger.debug("[python:stop] stop requested by %s for %s", user.get("sub"), conversation_id)
    service.stop_generation(conversation_id)
    return {"status": "stopped"}
