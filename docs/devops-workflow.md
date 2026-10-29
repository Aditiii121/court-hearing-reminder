
# DevOps Workflow — Court Hearing Reminder Portal

## Overview

This document describes the complete CI/CD and DevOps workflow implemented for the project.

---

## Source Control Workflow (Git)

```
main  ◄──────── protected, tags v1.0.x
  └── development  ◄─── feature branches merged here
        ├── feature/authentication
        ├── feature/hearing-crud
        ├── feature/dashboard
        ├── feature/reminders
        └── demo/broken-build   ◄─── controlled defect branch
```

### Branch Strategy
| Branch | Purpose |
|---|---|
| `main` | Production-ready code only |
| `development` | Integration branch |
| `feature/*` | Individual features |
| `demo/broken-build` | Intentional failure for demo |

### Commit Convention (Conventional Commits)
- `feat:` — new feature
- `fix:` — bug fix
- `test:` — adding tests
- `ci:` — CI/CD changes
- `docker:` — Docker/containerization changes
- `docs:` — documentation

### Tags
- `v1.0.0` — stable release
- `v1.0.1` — updated release (for rollback demo)

---

## CI/CD Pipeline (Jenkins)

### Pipeline Stages

```
Checkout → Backend Build → Unit Tests → Frontend Build
    → Start Integration Env → Selenium Tests
    → Docker Push (if PUSH_IMAGE=true)
    → Deploy (if DEPLOY=true) → Health Check
```

### Pipeline Parameters
| Parameter | Default | Description |
|---|---|---|
| `IMAGE_TAG` | `1.0.0` | Tag for built images |
| `PUSH_IMAGE` | `false` | Push to Docker Hub |
| `DEPLOY` | `false` | Deploy after build |
| `APP_URL` | `http://localhost` | URL for Selenium tests |

### Jenkins Setup
1. Install Jenkins (Docker recommended): `docker run -p 8090:8080 jenkins/jenkins:lts`
2. Install plugins: Docker Pipeline, Git, JUnit, Pipeline
3. Add credential `dockerhub-credentials` (Username/Password)
4. Create Pipeline job pointing to this repo's `Jenkinsfile`

---

## Controlled Defect Demo

### Branch: `demo/broken-build`

The branch `demo/broken-build` contains a deliberate Selenium test failure to demonstrate Jenkins pipeline failure detection.

**How the defect was introduced:**
```java
// In LoginTest.java — line changed to expect wrong URL
wait.until(ExpectedConditions.urlContains("/WRONG_URL_THAT_NEVER_EXISTS"));
```

**Demo Steps:**
1. Switch Jenkins to build `demo/broken-build` branch
2. Observe Stage 6 (Selenium Tests) turning RED
3. Pipeline reports failure with screenshot archived
4. Fix: merge `main` back / revert the change
5. Rebuild → GREEN

---

## Docker Hub Images

| Image | Tags |
|---|---|
| `<username>/court-backend` | `1.0.0`, `1.0.1`, `latest` |
| `<username>/court-frontend` | `1.0.0`, `1.0.1`, `latest` |

---

## Rollback Demo

### Scenario: Deploy v1.0.1 with bad config → fail → rollback to v1.0.0

```powershell
# 1. Deploy v1.0.0 (working)
.\scripts\deploy.ps1 -ImageTag 1.0.0

# 2. Deploy v1.0.1 (simulated bad JWT_SECRET — set DB_PASSWORD=WRONG in .env)
#    Healthcheck fails → pipeline aborts
.\scripts\deploy.ps1 -ImageTag 1.0.1

# 3. Rollback to v1.0.0
.\scripts\rollback.ps1
# OR explicitly:
.\scripts\rollback.ps1 -RollbackTag 1.0.0
```

---

## Ansible Deployment

> **NOTE:** Ansible requires Linux. On Windows, use WSL2 or a Linux VM.

```bash
# From WSL or Linux:
cd /mnt/c/Users/aditi/Downloads/devops\ proj/court-hearing-reminder

# First run
ansible-playbook -i ansible/inventory ansible/playbook.yml \
  -e "docker_hub_username=yourusername" \
  -e "image_tag=1.0.0" \
  -K   # ask sudo password

# Second run (idempotency check) — should show changed=0 on stable system
ansible-playbook -i ansible/inventory ansible/playbook.yml \
  -e "docker_hub_username=yourusername" \
  -e "image_tag=1.0.0" \
  -K
```

Expected second-run output:
```
PLAY RECAP
court-server : ok=12   changed=0   unreachable=0   failed=0
```
