# Task Mapping — Court Hearing Reminder Portal

This document maps the 15 DevOps tasks to their implementation in the project.

| # | Task | Implementation | File(s) |
|---|---|---|---|
| 1 | **Source Code Management** — Git repo, branches, tags | `git init`, feature branches, `main`/`development`, tags `v1.0.0`, `v1.0.1` | `.gitignore`, `.git/` |
| 2 | **Agile Planning** — User stories, sprints, backlog | Sprint plan, user stories, acceptance criteria | `docs/agile-plan.md` |
| 3 | **System Architecture** — Diagrams, component overview | Mermaid architecture + deployment diagrams | `docs/architecture.md` |
| 4 | **Backend Development** — REST API, business logic | Spring Boot 3, JWT auth, CRUD endpoints, scheduler | `backend/src/` |
| 5 | **Frontend Development** — SPA UI | React + Vite, all pages, charts, dark glassmorphism | `frontend/src/` |
| 6 | **Automated Unit Testing** — JUnit5 + Mockito | 17 tests: service layer, controllers with MockMvc | `backend/src/test/` |
| 7 | **Docker Containerisation** — Multi-stage builds | Backend (maven→JRE alpine), Frontend (node→nginx alpine) | `backend/Dockerfile`, `frontend/Dockerfile` |
| 8 | **Docker Compose** — Multi-service orchestration | mysql+backend+frontend, healthchecks, named volumes | `docker/docker-compose.yml` |
| 9 | **CI/CD Pipeline** — Jenkins declarative pipeline | 9 stages: build, test, selenium, push, deploy, healthcheck | `Jenkinsfile` |
| 10 | **Selenium E2E Tests** — Browser automation | 9 tests: login, create, search, status update, dashboard | `tests/selenium/` |
| 11 | **Docker Hub** — Image registry push | Pipeline pushes `court-backend` and `court-frontend` images | `Jenkinsfile` (Docker Push stage) |
| 12 | **Deploy & Rollback Scripts** — Automated deployment | Deploy saves previous tag; rollback stops new, restarts old | `scripts/deploy.sh/.ps1`, `scripts/rollback.sh/.ps1` |
| 13 | **Ansible** — Configuration management | Idempotent playbook: installs Docker, templates config, pulls images | `ansible/playbook.yml`, `ansible/templates/` |
| 14 | **Controlled Defect Demo** — Break-then-fix | Branch `demo/broken-build` with deliberate LoginTest failure | `Jenkinsfile`, `tests/selenium/LoginTest.java` |
| 15 | **Documentation** — Comprehensive docs | API docs, test plan, deployment guide, demo script, evidence checklist | `docs/` |

---

## Acceptance Criteria Summary

| Criterion | Status | Evidence |
|---|---|---|
| Login with JWT works | ✅ | `AuthController`, `JwtUtil`, `SecurityConfig` |
| Real DB data in dashboard | ✅ | `DashboardService` queries MySQL via JPA |
| CRUD creates/updates/deletes hearings | ✅ | `HearingController`, `HearingService` |
| Reminders use scheduled job | ✅ | `@Scheduled` in `ReminderScheduler`, `ReminderService` |
| 17 unit tests pass | ✅ | `HearingServiceTest`, `ReminderServiceTest`, `DashboardServiceTest`, `AuthControllerTest` |
| 9 Selenium tests implemented | ✅ | 5 test classes in `tests/selenium/` |
| Docker multi-stage builds | ✅ | Both Dockerfiles use builder+runtime stages |
| Docker Compose starts full stack | ✅ | `docker/docker-compose.yml` |
| Jenkins pipeline 9 stages | ✅ | `Jenkinsfile` |
| Rollback demo documented | ✅ | `docs/devops-workflow.md`, `scripts/rollback.*` |
| Ansible idempotent playbook | ✅ | `ansible/playbook.yml` (changed=0 on second run) |
| No secrets in git | ✅ | `.env` in `.gitignore`, `.env.example` committed |
| Windows-compatible | ✅ | All `.ps1` scripts, Docker-based Java compilation |
