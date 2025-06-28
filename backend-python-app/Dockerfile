FROM python:3.10-slim

RUN apt-get update && apt-get install -y --no-install-recommends \
        git \
    && rm -rf /var/lib/apt/lists/*

# cache locations for Hugging Face models and pip packages
ENV HF_HOME=/cache/huggingface \
    PIP_CACHE_DIR=/cache/pip

# ensure cache directories exist
RUN mkdir -p "$HF_HOME" "$PIP_CACHE_DIR"

# allow users to persist the above caches using a Docker volume
VOLUME ["/cache"]

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY setup_env.py /tmp/setup_env.py
RUN python /tmp/setup_env.py && rm /tmp/setup_env.py

COPY . .

CMD ["python", "main.py"]
