from functools import lru_cache
from typing import Tuple
import os
import logging

import torch
from transformers import AutoModelForCausalLM, AutoTokenizer


logger = logging.getLogger(__name__)

def load_model(model_name_or_path: str, provider: str = "huggingface") -> Tuple[object, object]:
    """Load a model/tokenizer pair from Hugging Face or a local directory.

    The ``provider`` parameter controls where models are fetched from. When set
    to ``"local"`` the ``local_files_only`` flag is enabled so the tokenizer and
    model are loaded exclusively from disk without contacting the Hugging Face
    Hub.
    """

    local_only = provider == "local"
    hf_token = os.getenv("HF_TOKEN") if not local_only else None
    if not torch.cuda.is_available() or torch.cuda.device_count() == 0:
        raise RuntimeError("A CUDA capable GPU is required to load the model")

    tokenizer = AutoTokenizer.from_pretrained(
        model_name_or_path,
        local_files_only=local_only,
        token=hf_token,
    )
    try:
        model = AutoModelForCausalLM.from_pretrained(
            model_name_or_path,
            local_files_only=local_only,
            token=hf_token,
            device_map={"": 0},
        )
    except RuntimeError as exc:
        if "CUDA out of memory" in str(exc):
            raise RuntimeError("Model does not fit on the GPU") from exc
        raise

    device = next(model.parameters()).device
    logger.info("Loading model on %s", device)

    return model, tokenizer


@lru_cache()
def get_model(model_name_or_path: str, provider: str = "huggingface") -> Tuple[object, object]:
    """Return a cached model/tokenizer pair for the given provider."""
    return load_model(model_name_or_path, provider)
