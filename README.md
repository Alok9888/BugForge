# BugForge

BugForge is a LeetCode-style practice app for building React components. It includes a seeded challenge catalog, a React/Vite workspace, an isolated live preview, and server-controlled browser evaluation.

See [the current system design](docs/system-design.md) for the architecture, request flows, data model, API endpoints, and deployment status.

## Requirements

- Node.js 20 or newer and npm for the frontend (the evaluator image includes its own Node.js runtime)
- Docker Desktop with Compose (for PostgreSQL, the backend API, and the isolated evaluator)
- Java 21 or newer only if running backend tests directly on the host

## Run locally

1. Start PostgreSQL, the API, and the isolated evaluator from the project root:

   ```powershell
   Copy-Item .env.example .env
   # Edit .env and set DATABASE_PASSWORD to a private, long random value.
   docker compose up -d --build postgres evaluator backend
   ```

   PostgreSQL is published only on `127.0.0.1:5433` for local inspection. The API is published on `127.0.0.1:8080`. The evaluator has no host port; the API calls it over a private Docker network. It runs in a restricted container with memory/CPU/PID limits, no added Linux capabilities, and a local authentication token.

2. Install and start the frontend in a second terminal:

   ```powershell
   cd frontend
   npm install
   npm run dev
   ```

   Open `http://localhost:5173`. Vite proxies `/api` calls to the local backend. To use a different API host, set `VITE_API_PROXY` in `frontend/.env`.

The local evaluator token must match in the API and evaluator environments. For local development both default to `local-dev-only-change-me`. Set the same value in the project root `.env` if you change it. Do not use the development token in a deployed environment.

Running the evaluator directly on the host instead of through Compose requires Node.js 22.18 or newer.

## Render deployment preview

The root `render.yaml` defines a Render Blueprint for the frontend, private API, private evaluator, and managed PostgreSQL database. To create it, push this project to a Git repository you control, choose **New → Blueprint** in Render, connect that repository, and review the services and costs before applying the Blueprint. Render generates the evaluator token and passes it to the API and evaluator; do not add the local `.env` file to Git.

The frontend uses Render's free web-service plan and can sleep after inactivity. The API, evaluator, and database use paid plans; the Blueprint's current smallest configuration is approximately US$38/month before taxes or usage changes. Review Render's current pricing before deployment.

This setup is for a controlled preview. The app accepts arbitrary submitted JavaScript, and the evaluator is not yet isolated in a separate per-submission sandbox. Keep access restricted and do not invite public submissions until that isolation and abuse controls have been added. A private evaluator service and shared token protect the service boundary, but do not make execution of untrusted code production-safe.

## Tests and production frontend build

```powershell
cd backend
.\mvnw.cmd test
```

```powershell
cd frontend
npm run build
```

The frontend `postinstall` script copies the preview runtime libraries into an ignored `public/runtime` directory. Run `npm install` after cloning before building.

## How evaluation works

- The browser preview compiles JSX with Babel and renders it in a sandboxed iframe. Its Content Security Policy blocks network connections and privileged parent-window access.
- On Submit, the frontend sends only the challenge slug and source code to Spring Boot. It does not send pass/fail claims.
- Spring loads the challenge's private test definitions and forwards the source and tests to the separate evaluator service using the shared token.
- The evaluator compiles JSX with Babel, then runs the component and its interaction/assertion cases in Playwright Chromium inside a sandboxed iframe. It blocks browser network requests and enforces request limits and per-step timeouts.
- Spring validates the runner response, stores the submission and progress, and returns the individual test results. Only a fully passing run marks the challenge complete.

The evaluator is a dedicated service rather than a JavaScript engine embedded in Spring. Its Docker container provides useful isolation for this local MVP, but arbitrary code execution remains a high risk area. Before public deployment, run evaluation in a stronger per-job sandbox with an independently maintained browser image, strict egress policy, request authentication, and monitoring.

## API

- `GET /api/problems` — paginated challenge metadata (the original `/api/challenges` alias remains available).
- `GET /api/problems/{slug}` — challenge statement and starter code; private test definitions are kept server-side.
- `POST /api/submissions` — JSON body: `{ "challengeSlug": "counter", "code": "export default function App() { return <h1>Hello</h1>; }" }`.
- `GET /api/progress` and `GET /api/progress/{slug}` — persisted progress for the browser's generated `X-User-Id`.

Draft code and completion indicators are stored separately in browser local storage. There is no login or account system in this MVP.
