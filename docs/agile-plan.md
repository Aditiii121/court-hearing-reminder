# Agile Plan — Automated Court Hearing Reminder Portal

## Problem Statement
Court hearing management in many jurisdictions relies on paper-based or fragmented digital systems, leading to missed hearings, scheduling conflicts, and communication failures between lawyers, clerks, and court administrators. This project delivers a centralised, role-aware web portal that manages court hearing records, sends automated in-app reminders, and exposes a DevOps-grade CI/CD pipeline for continuous delivery.

## Stakeholders
| Stakeholder | Role | Interest |
|-------------|------|----------|
| Court Administrator | ADMIN | Full system control, user management |
| Lawyer | LAWYER | View/manage assigned cases, receive reminders |
| Court Clerk | CLERK | Create & search hearings, update limited fields |
| Professor / Evaluator | Observer | Assess DevOps implementation quality |

## Objectives
1. Centralise court hearing records with full CRUD operations
2. Automate 24-hour and 7-day reminders via scheduled jobs
3. Enforce role-based access control (ADMIN, LAWYER, CLERK)
4. Demonstrate a production-grade DevOps pipeline: Git → Jenkins → Docker → Deploy → Ansible → Health Check → Rollback
5. Achieve 80%+ unit test coverage on core service logic

## Constraints
- No paid external services (no Twilio, SendGrid, etc.)
- Simple monolith — no microservices or Kubernetes
- Must run on Windows developer machine via Docker Compose
- Ansible requires Linux/WSL control node

## MVP Definition
A working monolith with:
- JWT-secured REST API with role-based endpoints
- MySQL-backed hearing CRUD + search with pagination
- Scheduled reminder classification job
- React frontend wired to real API
- Docker Compose one-command startup
- CI pipeline in Jenkins
- Passing unit + Selenium tests

## Product Backlog

| ID | Story | Priority | Points |
|----|-------|----------|--------|
| US-01 | As a clerk, I want to log in with secure credentials so my role is enforced | High | 3 |
| US-02 | As a clerk, I want to create a hearing record with all required fields | High | 5 |
| US-03 | As a lawyer, I want to view all hearings assigned to me | High | 3 |
| US-04 | As an admin, I want to view all hearings across all lawyers | High | 3 |
| US-05 | As a user, I want to search hearings by case number, court, judge, status | High | 5 |
| US-06 | As a lawyer, I want to update a hearing's status following the workflow | High | 5 |
| US-07 | As a user, I want to see a dashboard summary with counts and upcoming hearings | Medium | 5 |
| US-08 | As a user, I want to receive in-app reminders for hearings within 24 h / 7 days | Medium | 5 |
| US-09 | As an admin, I want audit fields (createdBy, updatedBy) on every record | Medium | 3 |
| US-10 | As a DevOps engineer, I want a Jenkins pipeline that stops on test failure | High | 8 |
| US-11 | As a DevOps engineer, I want versioned Docker images pushed to Docker Hub | High | 5 |
| US-12 | As a DevOps engineer, I want Ansible to provision and deploy idempotently | High | 8 |
| US-13 | As a DevOps engineer, I want automated rollback when health check fails | High | 5 |
| US-14 | As a QA engineer, I want Selenium tests covering 5 key user journeys | High | 8 |
| US-15 | As an admin, I want the API to return proper 400/401/403/404/500 JSON errors | Medium | 3 |

## Definition of Done
- Code reviewed (self-review in solo project), merged via feature branch
- Unit tests written and green
- Docker build successful
- API endpoint tested manually or via Selenium
- Documentation updated
- Conventional commit tag applied

## Sprint Plan (2-week sprints)

### Sprint 1 (Week 1–2): Foundation
- US-01 Authentication + JWT
- US-02 Hearing CRUD
- US-09 Audit fields
- US-15 Error handling
- M1: Repo scaffold + docs

### Sprint 2 (Week 3–4): Features
- US-03, US-04 View hearings (role-filtered)
- US-05 Search + pagination
- US-06 Status workflow
- US-07 Dashboard
- US-08 Reminders

### Sprint 3 (Week 5–6): Frontend
- All React pages (Login, Dashboard, Hearings, Reminders)
- Role-aware UI
- data-testid attributes

### Sprint 4 (Week 7–8): DevOps
- US-10 Jenkins pipeline
- US-11 Docker + versioning
- US-12 Ansible
- US-13 Rollback
- US-14 Selenium tests

## User Stories (Detailed)

### US-02 Create Hearing
**As a** court clerk, **I want** to create a new hearing record with case number, title, parties, court, date/time, judge, and lawyer assignment, **so that** the record is persisted and visible to all authorised users.

**Acceptance Criteria:**
- Given I am logged in as CLERK or higher
- When I submit a valid hearing form
- Then a new hearing appears in the list with status SCHEDULED
- And an audit record shows my username as createdBy
- And duplicate caseNumber is rejected with HTTP 409

### US-06 Status Workflow
**As a** lawyer, **I want** to transition a hearing status following the defined workflow, **so that** statuses are always valid and auditable.

**Acceptance Criteria:**
- SCHEDULED → PENDING, IN_PROGRESS, ADJOURNED, CANCELLED (allowed)
- COMPLETED → any transition (blocked, 403)
- CANCELLED → any transition (blocked, 403)
- Status change returns 200 with updated hearing

### US-13 Rollback
**As a** DevOps engineer, **I want** the deployment script to automatically roll back to the previous version when the health check fails, **so that** production is never left broken.

**Acceptance Criteria:**
- Deploy 1.0.1 with bad config → health check fails → rollback runs → 1.0.0 is healthy
- Evidence: terminal output showing rollback triggered
