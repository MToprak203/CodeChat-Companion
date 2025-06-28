from __future__ import annotations

from typing import Iterable, List, Optional, Dict
from threading import Thread, Event, Lock
import logging

from transformers import TextIteratorStreamer
from transformers.generation.stopping_criteria import StoppingCriteria, StoppingCriteriaList

from ..model_manager import ModelManager
from ..schemas import ChatMessage

logger = logging.getLogger(__name__)

END_TOKEN = "[DONE]"


class _StopSignalCriteria(StoppingCriteria):
    """Transformer stopping criteria that stops when an event is set."""

    def __init__(self, event: Event) -> None:
        self.event = event

    def __call__(self, input_ids, scores, **kwargs) -> bool:  # type: ignore[override]
        return self.event.is_set()


class AIService:
    """Provide streaming text generation using a shared language model."""

    def __init__(self, model_name_or_path: str, provider: str = "huggingface") -> None:
        self.model_name_or_path = model_name_or_path
        self.provider = provider
        self.manager = ModelManager()
        self._threads: Dict[int, Thread] = {}
        self._streamers: Dict[int, TextIteratorStreamer] = {}
        self._events: Dict[int, Event] = {}
        self._lock = Lock()
        # load the model synchronously so the server only starts once ready
        logger.debug("[python:service:init] model=%s provider=%s", model_name_or_path, provider)
        self.manager.initialize(model_name_or_path, provider)

    def build_prompt(
        self,
        prompt: str,
        chat_history: Optional[List[ChatMessage]] = None,
        files: Optional[List[str]] = None,
        tokenizer=None,
    ) -> str:
        """Combine the request, chat history and files into a single prompt."""

        intro = (
            "You are a helpful AI coding assistant. Use the chat history to "
            "maintain context and the project files to answer questions about "
            "the codebase."
        )

        logger.debug(
            "[python:service:build_prompt] prompt_len=%s history=%d files=%d",
            len(prompt),
            len(chat_history) if chat_history else 0,
            len(files) if files else 0,
        )

        messages: List[dict] = [
            {"role": "system", "content": intro}
        ]

        if chat_history:
            messages.extend(
                {"role": msg.role, "content": msg.content} for msg in chat_history
            )

        if files:
            file_content = "\n".join(files)
            messages.append({"role": "system", "content": f"Project files:\n{file_content}"})

        messages.append({"role": "user", "content": prompt})

        if tokenizer is not None and getattr(tokenizer, "chat_template", None):
            return tokenizer.apply_chat_template(
                messages, tokenize=False, add_generation_prompt=True
            )

        prompt_text = ""
        for msg in messages:
            prompt_text += f"{msg['role'].capitalize()}: {msg['content']}\n"
        return prompt_text + "Assistant: "

    def stop_generation(self, conversation_id: int) -> None:
        """Signal the generation thread for the given conversation to stop."""
        with self._lock:
            event = self._events.pop(conversation_id, None)
            streamer = self._streamers.pop(conversation_id, None)
            thread = self._threads.pop(conversation_id, None)
            if event:
                event.set()
            if streamer is not None:
                try:
                    streamer.end()
                except Exception as exc:  # pragma: no cover - best effort
                    logger.warning("[python:service:stop] failed to end stream: %s", exc)
        if thread and thread.is_alive():
            thread.join(timeout=0.1)

    def stream_response(
        self,
        conversation_id: int,
        prompt: str,
        chat_history: Optional[List[ChatMessage]] = None,
        files: Optional[List[str]] = None,
    ) -> Iterable[str]:
        logger.debug("[python:service:stream] start generation for %s", conversation_id)
        model, tokenizer = self.manager.get_model_and_tokenizer()
        full_prompt = self.build_prompt(prompt, chat_history, files, tokenizer)
        streamer = TextIteratorStreamer(
            tokenizer, skip_prompt=True, skip_special_tokens=True
        )
        inputs = tokenizer(
            full_prompt,
            return_tensors="pt",
            padding=True,
            truncation=True,
            return_attention_mask=True,
        ).to(model.device)

        pad_token = tokenizer.pad_token_id or tokenizer.eos_token_id
        max_length = getattr(model.config, "max_position_embeddings", None)
        if not max_length:
            max_length = getattr(tokenizer, "model_max_length", 2048)
        remaining = max_length - inputs.input_ids.shape[1]
        if remaining < 1:
            remaining = 1

        stop_event = Event()
        stop_criteria = StoppingCriteriaList([_StopSignalCriteria(stop_event)])

        thread = Thread(
            target=model.generate,
            kwargs={
                "input_ids": inputs.input_ids,
                "attention_mask": inputs.attention_mask,
                "max_new_tokens": remaining,
                "do_sample": True,
                "pad_token_id": pad_token,
                "streamer": streamer,
                "stopping_criteria": stop_criteria,
            },
            daemon=True,
        )

        with self._lock:
            # Replace any existing generation for this conversation
            self._events[conversation_id] = stop_event
            self._streamers[conversation_id] = streamer
            self._threads[conversation_id] = thread

        thread.start()
        logger.debug("[python:service:stream] generation thread started")
        try:
            for token in streamer:
                yield token
            logger.debug("[python:service:stream] complete")
            yield END_TOKEN
        finally:
            self.stop_generation(conversation_id)
