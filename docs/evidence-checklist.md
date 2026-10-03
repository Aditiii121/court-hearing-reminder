# Evidence Checklist — Court Hearing Reminder Portal

Use this checklist when submitting / demonstrating the project.

## M1 — Repository & Planning

- [x] Git repository initialized with `.gitignore` (no secrets, no node_modules, no target/)
- [x] `.env.example` committed (passwords masked)
- [x] Feature branches created: `feature/authentication`, `feature/hearing-crud`, `feature/dashboard`, `feature/reminders`
- [x] `development` branch as integration branch
- [x] `main` branch as production branch
- [x] Tags: `v1.0.0`, `v1.0.1`
- [x] Conventional commit messages (feat:, fix:, test:, ci:, docker:, docs:)
- [x] `docs/agile-plan.md` — user stories, sprints, acceptance criteria
- [x] `docs/architecture.md` — Mermaid component + deployment diagrams
- [x] `docs/build-plan.md` — milestones

## M2 — Backend

- [x] Spring Boot 3.2.x application starts
- [x] MySQL connection via env vars (DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD)
- [x] JWT authentication working (login → token → protected routes)
- [x] Hearing CRUD: create, read, update, delete all functional
- [x] Status transition workflow with role restrictions
- [x] Role-based access: ADMIN, LAWYER, CLERK
- [x] Dashboard summary API returns real aggregated counts
- [x] Reminders API returns real DB records
- [x] `@Scheduled` hourly job creates/updates reminder records
- [x] `DataSeeder`: 3 users + 15 hearings seeded on startup
- [x] `GET /api/health` returns `{"status":"UP"}`

## M3 — Frontend

- [x] React/Vite SPA builds without errors
- [x] Login page with data-testid attributes
- [x] Dashboard with Recharts (pie + bar) showing real data
- [x] Hearing list with pagination and search
- [x] Add Hearing form with all fields
- [x] Edit Hearing form pre-populated
- [x] Hearing Details with status update dropdown
- [x] Reminders page with red (24h) and amber (7-day) highlighting
- [x] Role-based UI (CLERK cannot see Add Hearing)
- [x] JWT stored in localStorage, sent in all requests
- [x] 401 response → auto-logout and redirect to login

## M4 — Docker

- [x] `backend/Dockerfile` — multi-stage: maven builder → JRE-alpine runtime
- [x] `frontend/Dockerfile` — multi-stage: node builder → nginx-alpine runtime
- [x] Non-root user in backend (`courtapp`)
- [x] Healthchecks in both Dockerfiles
- [x] `docker/docker-compose.yml` — all 3 services with depends_on + healthchecks
- [x] Named volume for MySQL data persistence
- [x] `docker compose up --build` starts full stack from root `.env`
- [x] Frontend nginx proxies `/api/` to backend service

## M5 — Testing

- [x] 17 JUnit5 unit tests (service + controller layer)
- [x] H2 in-memory DB for tests (no MySQL needed)
- [x] `tests/selenium/pom.xml` with failsafe plugin
- [x] 5 Selenium test classes, 9 total tests
- [x] Selenium runs with `mvn verify -Pselenium`
- [x] Screenshots saved to `target/screenshots/` on failure

## M6 — Jenkins CI/CD

- [x] `Jenkinsfile` — declarative pipeline, 9 stages
- [x] JUnit test results published in Jenkins
- [x] Selenium screenshots archived as artifacts
- [x] `PUSH_IMAGE` parameter gates Docker Hub push
- [x] `DEPLOY` parameter gates deployment
- [x] `demo/broken-build` branch with deliberate test failure
- [x] Pipeline failure clearly shown in Jenkins UI

## M7 — Ansible

- [x] `ansible/inventory` with example host configuration
- [x] `ansible/group_vars/all.yml` — all variables defined
- [x] `ansible/templates/env.j2` — sensitive config templated
- [x] `ansible/templates/docker-compose.yml.j2` — compose file templated
- [x] `ansible/playbook.yml` — idempotent (second run: changed=0)
- [x] Installs Docker if not present
- [x] Pulls images and starts compose
- [x] Waits for backend healthcheck

## M8 — Scripts

- [x] `scripts/healthcheck.sh` — retry loop, configurable URL/retries
- [x] `scripts/healthcheck.ps1` — PowerShell equivalent
- [x] `scripts/deploy.sh` — saves previous tag, deploys new, healthchecks
- [x] `scripts/deploy.ps1` — PowerShell equivalent
- [x] `scripts/rollback.sh` — stops current, restores previous tag
- [x] `scripts/rollback.ps1` — PowerShell equivalent

## M9 — Documentation

- [x] `docs/api.md` — all endpoints documented
- [x] `docs/test-plan.md` — all test levels documented
- [x] `docs/deployment.md` — step-by-step deployment guide
- [x] `docs/devops-workflow.md` — CI/CD, git workflow, rollback demo
- [x] `docs/task-mapping.md` — 15 tasks mapped to files
- [x] `docs/demo-script.md` — 20-minute demo walkthrough
- [x] `README.md` — complete project overview

## Security Checklist

- [x] `.env` in `.gitignore`
- [x] Passwords never logged
- [x] JWT secret from env var
- [x] BCrypt password hashing
- [x] Non-root Docker user (backend)
- [x] Nginx security headers (X-Frame-Options, X-Content-Type-Options)
- [x] CORS configured to restrict origins
