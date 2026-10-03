# Test Plan — Court Hearing Reminder Portal

## Overview
This document describes the testing strategy across all levels.

---

## Testing Levels

### 1. Unit Tests (JUnit 5 + Mockito)
**Location:** `backend/src/test/java/com/court/hearing/`  
**Run:** Inside Docker during build / `mvn test` in `maven:3.9-eclipse-temurin-17` container  
**Database:** H2 in-memory (MODE=MySQL)  

| Test Class | Tests | What's Tested |
|---|---|---|
| `HearingServiceTest` | 10 | CRUD, status transitions, role restrictions |
| `ReminderServiceTest` | 3 | Reminder creation, dedup, status |
| `DashboardServiceTest` | 1 | Summary aggregation |
| `AuthControllerTest` | 3 | Login success, failure, JWT validation |
**Total:** 17 unit tests

### 2. Integration Tests via Docker Compose
**Approach:** Start full stack with `docker compose up --build`, then:
- Verify login API: `curl -X POST http://localhost:8080/api/auth/login`
- Verify hearings: `curl http://localhost:8080/api/hearings`
- Verify dashboard: `curl http://localhost:8080/api/dashboard/summary`

### 3. Selenium E2E Tests
**Location:** `tests/selenium/`  
**Framework:** Selenium 4.x + JUnit 5 + WebDriverManager  
**Run:** `mvn verify -Pselenium -Dapp.base.url=http://localhost`  

| Test Class | Tests | Scenario |
|---|---|---|
| `LoginTest` | 3 | Login success, failure, auth redirect |
| `CreateHearingTest` | 1 | Add hearing form submission |
| `SearchHearingTest` | 2 | Search with results, search empty |
| `UpdateStatusTest` | 1 | Status update workflow |
| `DashboardTest` | 2 | Stat cards non-zero, charts rendered |
**Total:** 9 Selenium tests

---

## Test Coverage Requirements
- Unit tests: Service layer fully mocked, repositories mocked
- Selenium: All major user journeys covered
- All tests must pass before merge to `main`

---

## Test Data
Seeds applied at startup via `DataSeeder.java`:
- 3 users: admin, lawyer, clerk
- 15 hearings with varied statuses, priorities, and relative dates (past, today, near future, far future)

---

## Controlled Defect (Demo)
- **Branch:** `demo/broken-build`  
- **Defect:** LoginTest expects wrong URL `/WRONG_URL_THAT_NEVER_EXISTS`  
- **Jenkins result:** Stage "Selenium Tests" fails → pipeline RED  
- **Fix:** Revert to correct URL → pipeline GREEN  
