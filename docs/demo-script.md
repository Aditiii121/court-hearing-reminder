# Demo Script — Court Hearing Reminder Portal

## Pre-Demo Checklist
- [ ] Docker Desktop running
- [ ] `.env` file created from `.env.example`
- [ ] Run `docker compose up --build` in `docker/` — wait for all 3 containers healthy
- [ ] Browser open at http://localhost
- [ ] Jenkins running (optional — for pipeline demo)

---

## Demo Flow (20 minutes)

### 1. Architecture Overview (2 min)
- Show `docs/architecture.md` — explain 3-tier: React SPA → Spring Boot → MySQL
- Show `docker/docker-compose.yml` — explain the 3 services and healthcheck chain
- Show `backend/Dockerfile` and `frontend/Dockerfile` — multi-stage builds

### 2. Login & Authentication (2 min)
1. Open http://localhost → redirected to `/login`
2. Enter wrong credentials → **show error message** (JWT rejection)
3. Enter `admin@example.com` / `Admin@123` → login succeeds → **Dashboard**
4. Show JWT in browser DevTools → Application → LocalStorage → `token`

### 3. Dashboard (2 min)
- Point to **stat cards** — 15 total hearings from seeded data
- Point to **Pie chart** — hearings by status (Recharts, real data)
- Point to **Bar chart** — hearings by priority
- If any hearing within 24h → show red alert banner

### 4. Hearing List & Search (2 min)
1. Click **Hearings** in sidebar → table with 15 rows
2. Search for "CIV" → filtered results
3. Click "Clear" → all results back
4. Show pagination if > 10 results

### 5. Create Hearing (2 min)
1. Click **Add Hearing** → form page
2. Fill in: Case Number, Title, Court, Judge, Date (future), Priority=URGENT
3. Submit → toast "Hearing created" → redirected to list
4. **New row appears** in table — verify real DB persistence

### 6. Hearing Details & Status Update (2 min)
1. Click **View** on a SCHEDULED hearing
2. Explain all fields displayed
3. Select **IN_PROGRESS** from status dropdown → Update Status
4. Toast "Status updated" → page refreshes with new badge
5. Explain role restrictions: CLERK can only set SCHEDULED/PENDING

### 7. Reminders Page (1 min)
1. Click **Reminders** in sidebar
2. Show hearings highlighted in **red** (within 24h) and **amber** (within 7 days)
3. Explain `@Scheduled` hourly job in `ReminderScheduler.java`
4. Click "Trigger Scheduler" (admin only) → show toast

### 8. Unit Tests (2 min)
- Show `HearingServiceTest.java` — mock repository, verify service logic
- Show `ReminderServiceTest.java` — verify dedup logic
- Explain: "17 tests run inside Docker multi-stage build — no Java needed on host"

### 9. CI/CD Pipeline — Jenkins (3 min)
1. Open Jenkins → show the pipeline view with 9 stages
2. Show a successful GREEN build
3. Show JUnit test results published
4. Show archived Selenium screenshots

### 10. Controlled Defect (2 min)
1. Switch Jenkins to build `demo/broken-build` branch
2. Show Selenium stage turning RED
3. Expand the failure → LoginTest expected `/WRONG_URL_THAT_NEVER_EXISTS`
4. Show screenshot in archived artifacts
5. "Fix" by switching back to `main` → rebuild GREEN

### 11. Rollback Demo (1 min)
```powershell
# Deploy broken v1.0.1
.\scripts\deploy.ps1 -ImageTag 1.0.1
# (healthcheck would fail with bad config — show the output)

# Rollback
.\scripts\rollback.ps1
# Service restored to v1.0.0 → healthcheck passes
```

### 12. Ansible (1 min)
- Show `ansible/playbook.yml` — idempotent tasks
- Show `ansible/templates/env.j2` — sensitive values templated
- Explain: "First run: changed=12, Second run: changed=0"
- Note: Requires Linux/WSL — cannot run on Windows host directly

---

## Q&A Prep

**Q: Why not microservices?**  
A: Academic scope — monolith demonstrates all DevOps practices without Kubernetes complexity. Primary focus is the DevOps pipeline, not service architecture.

**Q: How are passwords protected?**  
A: `.env` file in `.gitignore` — never committed. Ansible uses Jinja2 templates with vault-encrypted variables in production.

**Q: How do reminders work without email?**  
A: `ReminderScheduler` runs every hour, creates `Reminder` records in DB for hearings within 24h/7d. The `/reminders/upcoming` API serves these to the UI. Email gateway can be added by implementing `JavaMailSender` in `ReminderService.sendNotification()`.
