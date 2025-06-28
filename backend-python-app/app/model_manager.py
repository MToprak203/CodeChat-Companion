from __future__ import annotations

import os
import threading
from typing import Tuple
import logging

import torch
from transformers import AutoModelForCausalLM, AutoTokenizer


class ModelManager:
    """Singleton to lazily load and cache the language model."""

    _instance: ModelManager | None = None
    _model: AutoModelForCausalLM | None = None
    _tokenizer: AutoTokenizer | None = None
    _model_name: str | None = None
    _provider: str | None = None
    _is_initialized = False
    _is_initializing = False
    _init_lock = threading.Lock()

    logger = logging.getLogger(__name__)

    def __new__(cls, *args, **kwargs) -> "ModelManager":
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance

    def initialize(self, model_name: str, provider: str = "huggingface") -> None:
        """Load the model and tokenizer once in a thread safe way."""
        with self._init_lock:
            if self._is_initialized:
                self.logger.debug("Model already initialized, skipping")
                return
            if self._is_initializing:
                self.logger.debug("Model initialization in progress, skipping duplicate")
                return
            self.logger.info("Starting model initialization: %s", model_name)
            self._is_initializing = True
            self._model_name = model_name
            self._provider = provider

        local_only = provider == "local"
        token = os.getenv("HF_TOKEN") if not local_only else None

        try:
            if not torch.cuda.is_available() or torch.cuda.device_count() == 0:
                raise RuntimeError("A CUDA capable GPU is required to load the model")

            model = AutoModelForCausalLM.from_pretrained(
                model_name,
                torch_dtype=torch.bfloat16,
                trust_remote_code=True,
                device_map={"": 0},
                local_files_only=local_only,
                token=token,
            )
            tokenizer = AutoTokenizer.from_pretrained(
                model_name,
                padding_side="left",
                local_files_only=local_only,
                token=token,
            )
            if tokenizer.pad_token is None:
                tokenizer.pad_token = tokenizer.eos_token

            with self._init_lock:
                self._model = model
                self._tokenizer = tokenizer
                self._is_initialized = True
                self._is_initializing = False

            device = next(model.parameters()).device
            self.logger.info("Model %s loaded successfully on device: %s", model_name, device)
        except Exception as exc:
            with self._init_lock:
                self._is_initializing = False
            self.logger.error("Error loading model: %s", exc)
            if isinstance(exc, RuntimeError) and "CUDA out of memory" in str(exc):
                raise RuntimeError("Model does not fit on the GPU") from exc
            raise

    def get_model_and_tokenizer(self) -> Tuple[AutoModelForCausalLM, AutoTokenizer]:
        if not self._is_initialized:
            raise RuntimeError("Model Manager not initialized")
        return self._model, self._tokenizer  # type: ignore[return-value]

    def is_initialized(self) -> bool:
        return self._is_initialized

    def is_initializing(self) -> bool:
        return self._is_initializing

