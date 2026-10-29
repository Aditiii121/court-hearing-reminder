# ⚖️ Automated Court Hearing Reminder Portal

> **Academic DevOps Project** — A fully working court hearing management system built with a complete DevOps pipeline.

[![Build](https://img.shields.io/badge/build-passing-brightgreen)]()
[![CI](https://github.com/Aditiii121/court-hearing-reminder/actions/workflows/ci.yml/badge.svg)](https://github.com/Aditiii121/court-hearing-reminder/actions/workflows/ci.yml)
[![Docker](https://img.shields.io/badge/docker-multi--stage-blue)]()
[![Java](https://img.shields.io/badge/java-17-orange)]()
[![React](https://img.shields.io/badge/react-19-61dafb)]()

---

## 📋 Overview

The **Automated Court Hearing Reminder Portal** is a monolithic full-stack web application that allows court staff to manage hearing schedules and automatically generates reminders for upcoming hearings.

### Key Features
- **JWT Authentication** — role-based access (Admin, Lawyer, Clerk)
- **JWT Authentication** — role-based access (Admin, Lawyer, Clerk, **Client**)
- **Client Portal** — self-service dashboard for clients to view their own hearings and reminders
- **Hearing CRUD** — create, view, edit, delete with status workflow
- **Status Transitions** — SCHEDULED → PENDING → IN_PROGRESS → COMPLETED/ADJOURNED/CANCELLED
- **Automated Reminders** — scheduled job (hourly) flags hearings within 24h and 7 days
- **Real-time Dashboard** — charts showing hearings by status and priority
- **Search & Pagination** — filter hearings by case number, judge, parties

---

## 🏗️ Architecture

```
Browser (React SPA)
       ↓  HTTP
 Nginx :80  →  /api/*  →  Spring Boot :8080  →  MySQL :3306
```

**Tech Stack:**
| Layer | Technology |
|---|---|
| Frontend | React 19 + Vite + Recharts |
| Backend | Java 17 + Spring Boot 3.2 + Spring Security |
| Database | MySQL 8.0 |
| Auth | JWT (JJWT 0.12.5) + BCrypt |
| Containerisation | Docker + Docker Compose |
| CI/CD | Jenkins (declarative pipeline) |
| Config Management | Ansible |
| Testing | JUnit5 + Mockito + Selenium 4 |

---

## 🚀 Quick Start

### Prerequisites
- Docker Desktop (Windows, with Compose v2)
- Git
- PowerShell 5.1+

### Start the Application

```powershell
# Clone
git clone <repo-url>
cd "court-hearing-reminder"

# Configure environment
Copy-Item .env.example .env
# Edit .env with your passwords (see .env.example for required vars)

# Build and start everything
cd docker
docker compose up --build
```

Wait ~3 minutes for all services to start. Then:

- 🌐 **Frontend:** http://localhost
- 🔌 **API:** http://localhost:8080/api
- ❤️ **Health:** http://localhost:8080/api/health

### Demo Credentials
| Role | Email | Password |
|---|---|---|
| Admin | admin@example.com | Admin@123 |
| Lawyer | lawyer@example.com | Lawyer@123 |
| Clerk | clerk@example.com | Clerk@123 |

---

## 📁 Project Structure

```
court-hearing-reminder/
├── backend/                 # Spring Boot API
│   ├── src/main/java/       # Application code
│   ├── src/test/java/       # Unit tests (JUnit5 + Mockito)
│   └── Dockerfile           # Multi-stage: maven → JRE-alpine
├── frontend/                # React SPA
│   ├── src/                 # Components, pages, services
│   ├── nginx.conf           # SPA + API proxy
│   └── Dockerfile           # Multi-stage: node → nginx-alpine
├── docker/
│   └── docker-compose.yml   # 3 services with healthchecks
├── tests/selenium/          # Selenium E2E tests (JUnit5)
├── scripts/                 # Deploy + rollback + healthcheck
│   ├── deploy.sh / .ps1
│   ├── rollback.sh / .ps1
│   └── healthcheck.sh / .ps1
├── ansible/                 # Configuration management
│   ├── playbook.yml         # Idempotent deployment playbook
│   ├── inventory            # Target hosts
│   ├── group_vars/all.yml   # Shared variables
│   └── templates/           # env.j2, docker-compose.yml.j2
├── docs/                    # Full documentation
│   ├── architecture.md
│   ├── api.md
│   ├── deployment.md
│   ├── devops-workflow.md
│   ├── test-plan.md
│   ├── task-mapping.md
│   ├── demo-script.md
│   └── evidence-checklist.md
├── Jenkinsfile              # 9-stage declarative pipeline
├── .env.example             # Environment template (safe to commit)
└── .gitignore               # .env, node_modules, target/, etc.
```

---

## 🧪 Testing

### Unit Tests (run inside Docker — no local Java needed)
```powershell
docker run --rm -v "${PWD}/backend:/app" -w /app maven:3.9.6-eclipse-temurin-17 mvn test
```
17 tests: HearingService (10), ReminderService (3), DashboardService (1), AuthController (3)
32 tests: ClientController (15), HearingService (10), AuthController (3), ReminderService (3), DashboardService (1)

**Or via GitHub Actions** (no local Java needed) — see the Actions tab.

### Selenium E2E Tests
> Requires: running app + Chrome browser + Maven installed
```bash
cd tests/selenium
mvn verify -Pselenium -Dapp.base.url=http://localhost
```
9 tests: Login (3), CreateHearing (1), Search (2), UpdateStatus (1), Dashboard (2)

---

## 🔄 CI/CD Pipeline (Jenkins)

**9 Stages:**
1. Checkout
2. Backend Build (Docker multi-stage)
3. Unit Tests
4. Frontend Build (Docker multi-stage)
5. Start Integration Environment
6. Selenium Tests
7. Docker Push (conditional)
8. Deploy (conditional)
9. Health Check

**Controlled Defect Demo:**  
Branch `demo/broken-build` contains a deliberate `LoginTest` failure to demonstrate pipeline failure detection.

---

## 📦 Docker Images

```
<dockerhub-username>/court-backend:1.0.0
<dockerhub-username>/court-frontend:1.0.0
```

Versioned releases: `v1.0.0`, `v1.0.1`

---

## 🔒 Security

- Passwords: BCrypt hashed, never logged
- JWT: HS256, configurable expiry, sent as Bearer token
- Secrets: `.env` in `.gitignore`, Ansible vault for production
- Containers: Non-root user (`courtapp`) in backend
- Headers: `X-Frame-Options`, `X-Content-Type-Options` via Nginx

---

## 📖 Documentation

| Doc | Description |
|---|---|
| [Architecture](docs/architecture.md) | System design + Mermaid diagrams |
| [API](docs/api.md) | All REST endpoints |
| [Deployment](docs/deployment.md) | Step-by-step setup guide |
| [DevOps Workflow](docs/devops-workflow.md) | Git strategy, Jenkins, rollback, Ansible |
| [Test Plan](docs/test-plan.md) | Unit + Selenium testing strategy |
| [Task Mapping](docs/task-mapping.md) | 15 tasks → implementation |
| [Demo Script](docs/demo-script.md) | 20-minute demo walkthrough |
| [Evidence Checklist](docs/evidence-checklist.md) | Submission checklist |

---

## ⚙️ Scripts Reference

```powershell
# Deploy new version
.\scripts\deploy.ps1 -ImageTag 1.0.1

# Rollback to previous
.\scripts\rollback.ps1

# Manual healthcheck
.\scripts\healthcheck.ps1 -Url http://localhost:8080/api/health
```

---

## 🗒️ Notes for Evaluators

- **Windows host:** Java/Maven are NOT installed locally. All Java compilation happens inside Docker multi-stage builds.
- **Ansible:** Requires Linux/WSL2 — cannot run natively on Windows. Full playbook is implemented; see `docs/devops-workflow.md`.
- **Jenkins:** Must be running separately (Docker or VM). `Jenkinsfile` is fully implemented for demonstration.
- **Demo user data:** 15 hearings seeded automatically on startup via `DataSeeder.java`.
