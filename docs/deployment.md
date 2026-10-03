# Deployment Guide — Court Hearing Reminder Portal

## Prerequisites
- Docker Desktop (Windows) — v24+ with Compose v2
- PowerShell 5.1+ (Windows)
- Git

## Quick Start (Windows)

```powershell
# 1. Clone the repository
git clone <repo-url>
cd "court-hearing-reminder"

# 2. Create .env from template
Copy-Item .env.example .env
# Edit .env with strong passwords (see below)
notepad .env

# 3. Build and start everything
cd docker
docker compose up --build

# 4. Access the application
Start-Process "http://localhost"
```

## .env Configuration
```env
DB_ROOT_PASSWORD=SomeStrongRoot@123
DB_NAME=courtdb
DB_USERNAME=courtuser
DB_PASSWORD=SomeStrongPass@123
JWT_SECRET=aVeryLongAndRandomJWTSecretAtLeast256BitsLong!!ChangeMeNow
DOCKER_HUB_USERNAME=yourdockerhubusername
IMAGE_TAG=1.0.0
```

> ⚠️ **Never commit `.env` to git.** It is in `.gitignore`.

## Service URLs After Startup

| Service | URL |
|---|---|
| Frontend | http://localhost |
| Backend API | http://localhost:8080/api |
| Health Check | http://localhost:8080/api/health |

## Startup Sequence
Services start in dependency order with healthchecks:
1. **MySQL** starts → healthcheck (mysqladmin ping) passes
2. **Backend** starts → waits for MySQL → healthcheck (/api/health) passes
3. **Frontend** starts → waits for Backend

Typical startup time: 2–4 minutes on first run (image build + DB init).

## Verify Deployment

```powershell
# Check all containers are running
docker ps

# Check backend health
Invoke-RestMethod http://localhost:8080/api/health

# Check logs
docker logs court-backend --tail 50
docker logs court-mysql --tail 20
```

## Useful Commands

```powershell
# Stop all services
cd docker
docker compose down

# Stop and remove volumes (wipes DB)
docker compose down -v

# View logs
docker compose logs -f backend

# Rebuild a single service
docker compose up --build backend
```

## Deploy New Version

```powershell
# Build and tag new version
$env:IMAGE_TAG = "1.0.1"
cd docker
docker compose up --build -d

# Or use the deploy script
cd ..
.\scripts\deploy.ps1 -ImageTag 1.0.1
```

## Rollback

```powershell
.\scripts\rollback.ps1
# OR to a specific tag:
.\scripts\rollback.ps1 -RollbackTag 1.0.0
```

## Ansible Deployment (Linux/WSL Required)

> Ansible cannot run natively on Windows. Use WSL2 or a Linux server.

```bash
# From WSL terminal
cd /mnt/c/Users/aditi/Downloads/devops\ proj/court-hearing-reminder

# Install Ansible (if not installed)
pip3 install ansible

# Edit ansible/inventory with your server IP
nano ansible/inventory

# Run playbook
ansible-playbook -i ansible/inventory ansible/playbook.yml \
  -e "docker_hub_username=yourusername" \
  -e "image_tag=1.0.0" \
  -K
```

## Jenkins Setup

1. Start Jenkins: `docker run -p 8090:8080 -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts`
2. Install plugins: Docker Pipeline, Git, JUnit, Pipeline
3. Add Docker Hub credentials with ID `dockerhub-credentials`
4. Create new Pipeline job → Git SCM → point to this repo
5. Build with parameters (IMAGE_TAG, PUSH_IMAGE, DEPLOY)
