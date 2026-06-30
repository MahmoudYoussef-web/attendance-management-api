# Attendance Management API

RESTful API for employee attendance tracking with QR code check-in, leave management, and reporting.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Runtime | Java 21, Spring Boot 4.0 |
| Database | MySQL 8.0 + Flyway migrations |
| Cache | Redis (QR sessions, token blacklist) |
| Auth | JWT (jjwt), Spring Security 6 |
| API Docs | Swagger UI (springdoc-openapi) |

## Quick Start

```bash
# 1. Start MySQL + Redis + Backend
docker compose up -d

# 2. Open Swagger
open http://localhost:8080/swagger-ui/index.html
```

Or run backend manually:

```bash
cd backend
./mvnw spring-boot:run
```

## Bootstrap Admin

| Field | Value |
|-------|-------|
| Email | `admin@example.com` |
| Password | `Admin@123` |

## Roles

| Role | Access |
|------|--------|
| `SUPER_ADMIN` | Full access |
| `HR_MANAGER` | Create/read employees, departments, schedules; approve leaves |
| `EMPLOYEE` | Submit leaves, check-in via QR, view notifications |

## API Endpoints

### Auth
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/login` | Login → access + refresh tokens |
| POST | `/api/auth/refresh` | Refresh access token |
| POST | `/api/auth/logout` | Blacklist current token |

### Departments
| Method | Path | Roles |
|--------|------|-------|
| GET/POST | `/api/departments` | SUPER_ADMIN, HR_MANAGER |
| GET/PUT/DELETE | `/api/departments/{id}` | SUPER_ADMIN |

### Work Schedules
| Method | Path | Roles |
|--------|------|-------|
| GET/POST | `/api/schedules` | SUPER_ADMIN, HR_MANAGER |
| GET/PUT/DELETE | `/api/schedules/{id}` | SUPER_ADMIN, HR_MANAGER |

### Employees
| Method | Path | Roles |
|--------|------|-------|
| POST | `/api/employees` | SUPER_ADMIN, HR_MANAGER |
| GET | `/api/employees` | SUPER_ADMIN, HR_MANAGER |
| GET | `/api/employees/{id}` | SUPER_ADMIN, HR_MANAGER |
| PUT | `/api/employees/{id}` | SUPER_ADMIN, HR_MANAGER |
| PUT | `/api/employees/{id}/transfer` | SUPER_ADMIN, HR_MANAGER |
| PUT | `/api/employees/{id}/status` | SUPER_ADMIN, HR_MANAGER |

### QR Attendance
| Method | Path | Roles |
|--------|------|-------|
| POST | `/api/attendance/qr/sessions` | SUPER_ADMIN, HR_MANAGER |
| DELETE | `/api/attendance/qr/sessions/{id}` | SUPER_ADMIN, HR_MANAGER |
| POST | `/api/attendance/check-in` | All |
| GET | `/api/attendance` | SUPER_ADMIN, HR_MANAGER |
| GET | `/api/attendance/today` | SUPER_ADMIN, HR_MANAGER |

### Leaves
| Method | Path | Roles |
|--------|------|-------|
| POST | `/api/leaves` | All |
| GET | `/api/leaves/my` | EMPLOYEE |
| GET | `/api/leaves` | SUPER_ADMIN, HR_MANAGER |
| PUT | `/api/leaves/{id}/review` | SUPER_ADMIN, HR_MANAGER |

### Notifications
| Method | Path | Roles |
|--------|------|-------|
| GET | `/api/notifications` | All |
| GET | `/api/notifications/unread` | All |
| GET | `/api/notifications/unread-count` | All |
| PUT | `/api/notifications/{id}/read` | All |
| PUT | `/api/notifications/read-all` | All |

### Reports
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/reports/daily` | All employees + status for a date |
| GET | `/api/reports/monthly` | Per-employee status counts (range) |
| GET | `/api/reports/late-arrivals` | LATE records in range |
| GET | `/api/reports/absences` | Employees with no record (range) |
| GET | `/api/reports/departments` | Department-wise stats (range) |

All report endpoints support pagination: `?page=0&size=10&sort=employeeId,asc`

## Data Flow

```
HR creates QR session → qrPayload + qrSignature (HMAC-SHA256)
       ↓
Employee scans QR → POST /check-in → AttendanceRecord
       ↓
Scheduler (23:59 daily) → auto-marks ABSENT
```

## Project Structure

```
attendance-api/
├── backend/               # Spring Boot API
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── mvnw
├── frontend/              # React/Next.js frontend (coming soon)
│   └── .gitkeep
├── docker-compose.yml     # MySQL + Redis + API containers
├── .env                   # Environment variables
└── README.md
```
