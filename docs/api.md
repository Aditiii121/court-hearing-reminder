# API Documentation — Court Hearing Reminder Portal

## Base URL
- Local dev: `http://localhost:8080/api`
- Docker Compose: `http://localhost:80/api` (proxied via Nginx)

## Authentication
All endpoints except `/auth/login` require `Authorization: Bearer <JWT>` header.

---

## Auth

### POST /api/auth/login
```json
Request:  { "email": "admin@example.com", "password": "Admin@123" }
Response: {
  "token": "eyJ...",
  "userId": 1,
  "email": "admin@example.com",
  "fullName": "Admin User",
  "role": "ADMIN"
}
```

---

## Health

### GET /api/health
```json
Response: { "status": "UP", "timestamp": "2024-01-01T10:00:00" }
```

---

## Hearings

### GET /api/hearings?page=0&size=10
Returns paginated list of hearings.

### GET /api/hearings/{id}
Returns single hearing by ID.

### POST /api/hearings
**Roles:** ADMIN, LAWYER
```json
{
  "caseNumber": "CIV-2024-0001",
  "title": "Smith vs Jones",
  "courtName": "High Court of Delhi",
  "courtRoom": "Room 3A",
  "judgeName": "Hon. Justice Kumar",
  "petitionerName": "John Smith",
  "respondentName": "Mary Jones",
  "lawyerName": "Adv. Sharma",
  "hearingDate": "2024-12-01T10:00:00",
  "hearingType": "REGULAR",
  "status": "SCHEDULED",
  "priority": "HIGH",
  "description": "Property dispute",
  "notes": "Documents to be presented"
}
```

### PUT /api/hearings/{id}
**Roles:** ADMIN, LAWYER — updates all fields.

### DELETE /api/hearings/{id}
**Roles:** ADMIN only.

### PATCH /api/hearings/{id}/status
**Roles:** ADMIN, LAWYER, CLERK (with restrictions)
```json
Request:  { "status": "IN_PROGRESS" }
Response: { ...updated hearing... }
```

**Status Transition Rules:**
| From | To (allowed) |
|---|---|
| SCHEDULED | PENDING, IN_PROGRESS, CANCELLED |
| PENDING | SCHEDULED, IN_PROGRESS, ADJOURNED, CANCELLED |
| IN_PROGRESS | ADJOURNED, COMPLETED, CANCELLED |
| ADJOURNED | SCHEDULED, PENDING, CANCELLED |
| COMPLETED | *(terminal)* |
| CANCELLED | *(terminal)* |

CLERK: can only use SCHEDULED and PENDING as target status.

### GET /api/hearings/search?q=keyword&page=0&size=10
Searches case number, title, judge name, petitioner, respondent.

---

## Dashboard

### GET /api/dashboard/summary
```json
{
  "totalHearings": 15,
  "todayHearings": 2,
  "within24Hours": 3,
  "within7Days": 5,
  "completedHearings": 4,
  "cancelledHearings": 1,
  "byStatus": {
    "SCHEDULED": 5, "PENDING": 3, "IN_PROGRESS": 2,
    "ADJOURNED": 1, "COMPLETED": 4, "CANCELLED": 1
  },
  "byPriority": { "LOW": 2, "MEDIUM": 6, "HIGH": 5, "URGENT": 2 }
}
```

---

## Reminders

### GET /api/reminders/upcoming?type=WITHIN_24_HOURS
Returns reminders filtered by type (optional). Types: `WITHIN_24_HOURS`, `WITHIN_7_DAYS`.

### POST /api/reminders/trigger
**Roles:** ADMIN — manually triggers the reminder scheduler.

---

## HTTP Status Codes
| Code | Meaning |
|---|---|
| 200 | OK |
| 201 | Created |
| 400 | Bad Request / Validation error |
| 401 | Unauthorized (missing/invalid JWT) |
| 403 | Forbidden (insufficient role) |
| 404 | Not Found |
| 409 | Conflict (duplicate case number) |
| 422 | Unprocessable (invalid status transition) |
| 500 | Internal Server Error |
