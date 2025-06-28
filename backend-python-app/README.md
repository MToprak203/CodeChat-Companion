# Python AI Backend

A lightweight FastAPI service to stream responses from a language model.
Models can come from Hugging Face or a local directory.
The API exposes a single `/generate` endpoint used by the Java backend.
Requests can optionally include prior chat messages and project files so the
model has context when answering coding questions.

## Setup

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python setup_env.py  # install torch and transformers for CPU or GPU
```

## Usage

Set the model configuration using environment variables. They can also be placed
in a `.env` file inside `backend-python-app` which is loaded automatically at
startup.

- `MODEL_NAME_OR_PATH` points to the model name or local path
- `MODEL_PROVIDER` can be `huggingface` or `local` (defaults to `huggingface`)
- `HF_TOKEN` is an optional Hugging Face access token used when downloading
  models from the hub.
- `JWT_SECRET` is used to verify JWT tokens sent from the Java backend.
- The Java backend includes the current user's token in the `Authorization`
  header when calling the `/generate` endpoint.
- `ENABLE_NGROK` set to `true` will expose the service using ngrok for
  development. Requires `pyngrok` which is included in `requirements.txt`.
- `NGROK_AUTHTOKEN` (optional) can be provided to use your ngrok account.


These variables can be placed in a `.env` file inside the
`backend-python-app` directory. The service and Docker build both load this
file automatically so you don't need to export the variables manually.

Then run:

```bash
python main.py
```

When `ENABLE_NGROK` is `true`, the server starts an ngrok tunnel on startup
and prints the public URL so you can share it temporarily.

Send a POST request to `/generate` with JSON like:

```json
{
  "conversationId": 1,
  "chatHistory": [
    {"role": "user", "content": "previous message"},
    {"role": "assistant", "content": "previous answer"}
  ],
  "projectFiles": ["file content"],
  "correlationId": "abc-123"
}
```

When `prompt` is not provided, the service uses the last entry from
`chatHistory` as the new user request and treats the remaining messages as
context. `chatHistory` should be a list of objects containing a `role`
(`user` or `assistant`) and the message `content`. `projectFiles` remains a list
of strings. If either field is omitted or an empty list is supplied, the related
section is skipped when building the prompt. The
service labels any present sections and includes a short introduction so the AI
knows how to use them.

Example of the resulting prompt structure:

```
System: You are a helpful AI coding assistant. Use the chat history to maintain
conversation context and the project files to answer questions about the codebase.
User: <first message>
Assistant: <answer>
User: <next question>
...
System: Project files:
<contents>
User: <prompt>
Assistant:
```

The service loads the model before starting Uvicorn, so the first startup may
take a while. Once running, the `/generate` endpoint is immediately available.

To cancel a running generation for a conversation send:

```bash
POST /stop/<conversationId>
```

## Docker

Build and run the service in a container. The image automatically detects
CUDA support and installs the matching PyTorch variant. During the image
build the default model is downloaded so that it is immediately available
when a container starts.

Use the ``MODEL_NAME_OR_PATH`` and ``MODEL_PROVIDER`` build arguments to
override the model or source, and ``HF_TOKEN`` to provide a Hugging Face
access token if required.

Mount a volume to `/cache` so models and pip downloads persist between
container runs. Any `.env` file present in the project directory will be
copied during the build so the model can be downloaded using your
environment-specific settings.

```bash
docker build -t backend-python-app .
docker run --gpus all \
  --env-file .env \
  -v $(pwd)/hf_cache:/cache/huggingface \
  -v $(pwd)/pip_cache:/cache/pip \
  -p 8000:8000 backend-python-app

# Example of building with a custom model
# docker build --build-arg MODEL_NAME_OR_PATH=my-org/my-model \
#              --build-arg HF_TOKEN=xxx -t backend-python-app \
#              .
```

### Using Docker Compose for Hot Reloading

To iterate on the code without rebuilding the image, run the service via
`docker compose`. The compose file mounts the project directory and starts
Uvicorn with the `--reload` flag so changes are picked up automatically.

```bash
cd backend-python-app
docker compose up
```
