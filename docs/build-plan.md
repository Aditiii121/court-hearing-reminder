# Build Plan — Automated Court Hearing Reminder Portal

## Milestones

| # | Milestone | Status |
|---|-----------|--------|
| M1 | Repo scaffold, git init, .gitignore, .env.example, agile docs, architecture docs | ✅ |
| M2 | Backend: entities, security/JWT, seed, CRUD, search, dashboard, reminders, unit tests | 🔄 |
| M3 | Frontend: all pages wired to real API | 🔄 |
| M4 | Dockerfiles + Compose; `docker compose up --build` works end-to-end | 🔄 |
| M5 | Selenium tests (5 journeys) passing; Surefire/HTML reports | 🔄 |
| M6 | Jenkinsfile, scripts, versioned images 1.0.0 + 1.0.1, controlled-defect demo | 🔄 |
| M7 | Ansible playbook; idempotency demonstrated | 🔄 |
| M8 | Rollback + health-check scripts; 1.0.1 fail → rollback to 1.0.0 | 🔄 |
| M9 | Finalize README, all docs, git merges + tags, final report | 🔄 |

## Key Decisions
- Java/Maven compilation inside Docker multi-stage build (no local JDK required)
- H2 profile used ONLY for unit tests; MySQL used for all other profiles
- All secrets via env vars; .env never committed
- Jenkins runs in Docker with Docker socket access for Windows compatibility
- Ansible requires Linux/WSL control node (documented clearly)
