# AI Chat Application

This repository contains a simple chat application with a React front-end and a Spring WebFlux backend.

## Prerequisites
- Node.js 20+
- Java 21+
- Docker (for database and messaging services)

## Running the Backend

1. Start the supporting services (PostgreSQL, Redis, Kafka, and Minio):
   ```bash
   cd backend-java-app
   docker-compose up -d
   ```
2. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

The backend will listen on `http://localhost:8080`.

The included `docker-compose.yml` file also starts a Minio instance at
`http://localhost:9000` (console at `http://localhost:9001`) with default
credentials `minioadmin:minioadmin`.

## Running the Python AI Service

Use the provided compose file to start the FastAPI service in hot reload mode.
Changes to the `backend-python-app` directory are reflected immediately.

```bash
cd backend-python-app
docker compose up
```

## Running the Frontend

1. Install dependencies:
   ```bash
   cd frontend-app
   npm install
   ```
2. Start the dev server:
   ```bash
   npm run dev
   ```

The app expects the backend API base URL to be set via `VITE_API_BASE_URL` in `.env` (defaults to `http://localhost:8080/api/v1`).

### Project Upload Filtering

When uploading a project only source code and a handful of configuration files
are accepted. Common build directories (like `node_modules` or `target`) are
skipped automatically and files without a recognised extension are ignored. This
prevents dependencies or build artefacts from being sent to the backend. Files
such as `pom.xml`, `package.json` and other standard config files are preserved.
The same rules are enforced client and server side.

### Selecting Project Files

When chatting about a project you can choose which files the AI should load.
On the project chat page, use the checkboxes next to each file to select the
subset to send. The selected list is forwarded to the backend whenever a
conversation is started or a message is sent. If any files are selected, the
AI service will read **only** those files; otherwise it falls back to loading
the entire project. The currently selected files are displayed to everyone in
the conversation so participants know which parts of the project are being
shared. Use the **Clear Selection** button to quickly deselect all files.
The selection for each project is stored temporarily in Redis so all
participants see the same list in real time.

AI responses are only triggered after a user sends a message. Creating a new
conversation no longer causes an automatic reply from the assistant.

## Environment Variables

- `VITE_API_BASE_URL` – Base URL for API requests from the front-end.
- `JWT_SECRET` – Secret used by the backend to sign JWT tokens. The Python AI
  service uses the same value to verify incoming requests.
- `MINIO_ENDPOINT` – Minio endpoint URL used by the backend.
- `MINIO_ACCESS_KEY` – Access key for Minio.
- `MINIO_SECRET_KEY` – Secret key for Minio.
- `MINIO_BUCKET` – Bucket name where project files are stored.
- `APP_AI_BASE_URL` – Base URL of the external AI service.
- `KAFKA_BOOTSTRAP_SERVERS` – Kafka bootstrap servers used by the backend.
- WebSocket endpoints are exposed under `/ws/{userId}/conversations/{conversationId}`.
  - `/messages` – broadcast new persisted messages
  - `/tokens` – stream AI token responses in real time
- `/notify` – per-user conversation notifications
- `/ws/{userId}/notifications` – system-wide notifications
- `/ws/{userId}/projects/{projectId}/selected-files` – project file selection updates

