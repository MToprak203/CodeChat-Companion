from __future__ import annotations

from pathlib import Path
import logging
import os
from contextlib import asynccontextmanager
from app.api import router
from dotenv import load_dotenv
from fastapi import FastAPI

# Load variables from a local .env file when present
load_dotenv(Path(__file__).resolve().parent / ".env")

log_level = os.getenv("LOG_LEVEL", "DEBUG").upper()
logging.basicConfig(level=getattr(logging, log_level, logging.DEBUG))
logger = logging.getLogger(__name__)

USE_NGROK = os.getenv("ENABLE_NGROK", "").lower() in {"1", "true", "yes"}


def _start_ngrok() -> None:
    if not USE_NGROK:
        return
    try:
        from pyngrok import ngrok
    except Exception as exc:  # pragma: no cover - optional dependency
        logger.error("pyngrok not installed: %s", exc)
        return
    port = int(os.getenv("PORT", "8000"))
    token = os.getenv("NGROK_AUTHTOKEN")
    if token:
        ngrok.set_auth_token(token)
    url = ngrok.connect(port, bind_tls=True).public_url
    logger.info("ngrok tunnel active at %s -> http://localhost:%s", url, port)


@asynccontextmanager
async def lifespan(app: FastAPI):
    _start_ngrok()
    yield


app = FastAPI(title="AI Backend", lifespan=lifespan)
app.include_router(router)

if __name__ == "__main__":
    import uvicorn

    logger.debug("Starting AI Backend on 0.0.0.0:8000")
    uvicorn.run(app, host="0.0.0.0", port=8000)
