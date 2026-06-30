# Attendance Management API — Project Overview

A production-ready attendance management system built with Spring Boot 4.0, featuring QR code check-in, leave management, role-based access control, and comprehensive reporting.

---

## 1. Tech Stack

| Layer | Technology |
|-------|-----------|
| Runtime | Java 21, Spring Boot 4.0.7 |
| Database | MySQL 8.0 + Flyway migrations |
| Cache | Redis 7 (QR sessions + token blacklist) |
| Auth | JWT (jjwt 0.11.5, HMAC-SHA256) |
| ORM | Hibernate 7 / Spring Data JPA |
| Validation | Jakarta Bean Validation |
| API Docs | Swagger UI (springdoc-openapi 2.8.9) |
| Build | Maven + Spring Boot Maven Plugin |
| Deployment | Docker Compose (MySQL + Redis + App) |

---

## 2. Project Structure

```
attendance-api/
├── backend/                    # Spring Boot application
│   ├── src/main/java/com/attendance/
│   │   ├── config/             # OpenAPI, QR config, Flyway, Security
│   │   ├── controller/         # 9 REST controllers
│   │   ├── dto/                # 33 request/response records
│   │   ├── entity/             # 10 JPA entities + 5 enums
│   │   ├── exception/          # Custom exceptions + GlobalExceptionHandler
│   │   ├── mapper/             # Entity → DTO mappers
│   │   ├── repository/         # 9 Spring Data JPA repositories
│   │   └── service/            # 11 service classes + scheduler
│   ├── src/main/resources/
│   │   ├── db/migration/       # 8 Flyway migrations (V1–V8)
│   │   └── application.yaml    # Env-based config
│   ├── pom.xml
│   ├── Dockerfile
│   └── mvnw
├── frontend/                   # Frontend (placeholder)
├── docker-compose.yml          # MySQL + Redis + App
├── .env / .env.example
├── .gitignore
├── README.md
└── PROJECT.md                  # This file
```

---

## 3. Database Schema (10 tables, 8 Flyway migrations)

### V1 — Core Auth

**`users`** — Authentication accounts
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | Auto-increment |
| email | VARCHAR(100) | UNIQUE, NOT NULL |
| password_hash | VARCHAR(255) | NOT NULL |
| full_name | VARCHAR(100) | NOT NULL |
| role | VARCHAR(20) | ENUM: SUPER_ADMIN, HR_MANAGER, EMPLOYEE |
| is_enabled | BOOLEAN | Default TRUE |
| is_locked | BOOLEAN | Default FALSE |
| failed_attempts | INT | Default 0, lock after 5 |
| created_at | TIMESTAMP | |

**`refresh_tokens`** — JWT refresh token rotation
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| user_id | BIGINT FK → users(id) | ON DELETE CASCADE |
| token_hash | VARCHAR(64) | SHA-256 of token |
| expires_at | TIMESTAMP | |
| is_revoked | BOOLEAN | Default FALSE |
| created_at | TIMESTAMP | |

**`audit_logs`** — Audit trail
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| actor_id | BIGINT FK → users(id) | |
| action | VARCHAR(50) | |
| entity_type | VARCHAR(50) | |
| entity_id | BIGINT | |
| details | JSON | |
| ip_address | VARCHAR(45) | |
| created_at | TIMESTAMP | |

### V3 — Departments

**`departments`**
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| name | VARCHAR(100) | UNIQUE, NOT NULL |
| description | VARCHAR(255) | |
| created_at | TIMESTAMP | |

### V4 — Work Schedules

**`work_schedules`**
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| name | VARCHAR(100) | NOT NULL |
| start_time | TIME | NOT NULL |
| end_time | TIME | NOT NULL |
| late_after_minutes | INT | Default 15 |
| half_day_after_minutes | INT | Default 180 |
| created_at | TIMESTAMP | |

### V5 — Employees

**`employees`**
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| user_id | BIGINT FK → users(id) | UNIQUE, ON DELETE CASCADE |
| employee_code | VARCHAR(50) | UNIQUE, NOT NULL |
| first_name | VARCHAR(100) | NOT NULL |
| last_name | VARCHAR(100) | NOT NULL |
| phone | VARCHAR(20) | |
| department_id | BIGINT FK → departments(id) | ON DELETE SET NULL |
| position | VARCHAR(100) | |
| hire_date | DATE | NOT NULL |
| status | VARCHAR(20) | ENUM: ACTIVE, INACTIVE, TERMINATED |
| schedule_id | BIGINT FK → work_schedules(id) | ON DELETE SET NULL |
| created_at | TIMESTAMP | |

Indexes: `idx_employees_department_id`, `idx_employees_schedule_id`, `idx_employees_status`

### V6 — QR + Attendance

**`qr_sessions`**
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| session_id | VARCHAR(36) | UUID, UNIQUE |
| created_by | BIGINT FK → users(id) | ON DELETE CASCADE |
| expires_at | TIMESTAMP | NOT NULL |
| is_active | BOOLEAN | Default TRUE |
| created_at | TIMESTAMP | |

**`attendance_records`**
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| employee_id | BIGINT FK → employees(id) | ON DELETE CASCADE |
| session_id | VARCHAR(36) | NOT NULL |
| attendance_date | DATE | NOT NULL |
| check_in_time | TIMESTAMP | NOT NULL |
| status | VARCHAR(20) | ENUM: PRESENT, LATE, HALF_DAY, ABSENT, ON_LEAVE |
| notes | VARCHAR(255) | |
| created_at | TIMESTAMP | |

Constraints:
- `uq_employee_session`: UNIQUE(employee_id, session_id)
- `uq_employee_date`: UNIQUE(employee_id, attendance_date)

### V8 — Leave + Notifications

**`leave_requests`**
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| employee_id | BIGINT FK → employees(id) | ON DELETE CASCADE |
| leave_type | VARCHAR(20) | ENUM: ANNUAL, SICK, EMERGENCY, UNPAID |
| start_date | DATE | NOT NULL |
| end_date | DATE | NOT NULL |
| reason | VARCHAR(500) | |
| status | VARCHAR(20) | ENUM: PENDING, APPROVED, REJECTED |
| rejection_reason | VARCHAR(500) | |
| approved_by | BIGINT FK → users(id) | ON DELETE SET NULL |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

**`notifications`**
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| employee_id | BIGINT FK → employees(id) | ON DELETE CASCADE |
| message | TEXT | NOT NULL |
| is_read | BOOLEAN | Default FALSE |
| created_at | TIMESTAMP | |

---

## 4. Entities & Enums

### JPA Entities (10)

| Entity | Table | Key Relationships |
|--------|-------|-------------------|
| `User` | users | 1:N → RefreshToken |
| `RefreshToken` | refresh_tokens | N:1 → User |
| `Department` | departments | 1:N → Employee |
| `WorkSchedule` | work_schedules | 1:N → Employee |
| `Employee` | employees | 1:1 → User, N:1 → Department, N:1 → WorkSchedule |
| `QrSession` | qr_sessions | N:1 → User (creator) |
| `AttendanceRecord` | attendance_records | N:1 → Employee |
| `LeaveRequest` | leave_requests | N:1 → Employee, N:1 → User (approver) |
| `Notification` | notifications | N:1 → Employee |

### Enums (5)

| Enum | Values |
|------|--------|
| `Role` | SUPER_ADMIN, HR_MANAGER, EMPLOYEE |
| `EmploymentStatus` | ACTIVE, INACTIVE, TERMINATED |
| `AttendanceStatus` | PRESENT, LATE, HALF_DAY, ABSENT, ON_LEAVE |
| `LeaveStatus` | PENDING, APPROVED, REJECTED |
| `LeaveType` | ANNUAL, SICK, EMERGENCY, UNPAID |

---

## 5. API Reference (34 endpoints)

### Auth — `/api/auth`
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/login` | Public | Login → access + refresh tokens |
| POST | `/api/auth/refresh` | Public | Rotate refresh token |
| POST | `/api/auth/logout` | Bearer | Blacklist JWT |
| PUT | `/api/auth/change-password` | Bearer | Change own password |

### Users — `/api/users`
| Method | Path | Role | Description |
|--------|------|------|-------------|
| POST | `/api/users` | SUPER_ADMIN | Create user |
| GET | `/api/users` | SUPER_ADMIN | List users |
| GET | `/api/users/{id}` | SUPER_ADMIN | Get user |
| PUT | `/api/users/{id}` | SUPER_ADMIN | Update user |
| DELETE | `/api/users/{id}` | SUPER_ADMIN | Delete user |

### Departments — `/api/departments`
| Method | Path | Role | Description |
|--------|------|------|-------------|
| POST | `` | SUPER_ADMIN | Create |
| GET | `` | SUPER_ADMIN, HR_MANAGER | List |
| GET | `/{id}` | SUPER_ADMIN, HR_MANAGER | Get |
| PUT | `/{id}` | SUPER_ADMIN | Update |
| DELETE | `/{id}` | SUPER_ADMIN | Delete (blocked if employees assigned) |

### Work Schedules — `/api/schedules`
| Method | Path | Role | Description |
|--------|------|------|-------------|
| POST | `` | SUPER_ADMIN, HR_MANAGER | Create |
| GET | `` | SUPER_ADMIN, HR_MANAGER | List |
| GET | `/{id}` | SUPER_ADMIN, HR_MANAGER | Get |
| PUT | `/{id}` | SUPER_ADMIN, HR_MANAGER | Update |
| DELETE | `/{id}` | SUPER_ADMIN | Delete |

### Employees — `/api/employees`
| Method | Path | Role | Description |
|--------|------|------|-------------|
| POST | `` | SUPER_ADMIN, HR_MANAGER | Create |
| GET | `` | SUPER_ADMIN, HR_MANAGER | List |
| GET | `/{id}` | SUPER_ADMIN, HR_MANAGER | Get |
| PUT | `/{id}` | SUPER_ADMIN, HR_MANAGER | Update |
| PUT | `/{id}/transfer` | SUPER_ADMIN, HR_MANAGER | Transfer department |
| PUT | `/{id}/status` | SUPER_ADMIN, HR_MANAGER | Change status |

### QR Attendance — `/api/attendance`
| Method | Path | Role | Description |
|--------|------|------|-------------|
| POST | `/qr/sessions` | SUPER_ADMIN, HR_MANAGER | Create QR session |
| DELETE | `/qr/sessions/{sessionId}` | SUPER_ADMIN, HR_MANAGER | Deactivate |
| POST | `/check-in` | All | Employee check-in (QR payload + signature) |
| GET | `` | SUPER_ADMIN, HR_MANAGER | History |
| GET | `/today` | SUPER_ADMIN, HR_MANAGER | Today's records |

### Leaves — `/api/leaves`
| Method | Path | Role | Description |
|--------|------|------|-------------|
| POST | `` | All | Submit leave |
| GET | `/my` | EMPLOYEE | My leaves |
| GET | `` | SUPER_ADMIN, HR_MANAGER | All leaves |
| PUT | `/{id}/review` | SUPER_ADMIN, HR_MANAGER | Approve/reject |

### Notifications — `/api/notifications`
| Method | Path | Role | Description |
|--------|------|------|-------------|
| GET | `` | All | My notifications |
| GET | `/unread` | All | Unread only |
| GET | `/unread-count` | All | Count |
| PUT | `/{id}/read` | All | Mark read |
| PUT | `/read-all` | All | Mark all read |

### Reports — `/api/reports` (all require SUPER_ADMIN or HR_MANAGER)
| Method | Path | Description | Paginated |
|--------|------|-------------|-----------|
| GET | `/daily?date=` | All employees + status | Yes |
| GET | `/monthly?employeeId=&start=&end=` | Per-employee status counts | Yes |
| GET | `/late-arrivals?start=&end=` | Late arrivals with minutes-late | Yes |
| GET | `/absences?start=&end=` | Employees with no record | Yes |
| GET | `/departments?start=&end=` | Department-wise stats | Yes |

---

## 6. Security

### Authentication
- **JWT** (jjwt, HMAC-SHA256)
- Access token: 15 min, Refresh token: 7 days
- JWT claims: `sub` (email), `role`, `jti` (UUID for blacklisting)
- Refresh tokens are rotated (old token revoked, new one issued)

### Authorization
- **RBAC** with 3 roles: `SUPER_ADMIN` > `HR_MANAGER` > `EMPLOYEE`
- Method-level security via `@PreAuthorize`
- Public endpoints only: `/api/auth/login`, `/api/auth/refresh`, Swagger docs

### Protection
- **Account lockout**: 5 failed attempts → `is_locked = true`
- **BCrypt** password encoding
- **JWT blacklist**: In-memory store of revoked `jti`s
- **Stateless**: `SessionCreationPolicy.STATELESS`, CSRF disabled
- **Global exception handler**: `@RestControllerAdvice` with structured `ErrorResponse`

---

## 7. Phase-by-Phase Build Order

| Phase | Migrations | What Was Built |
|-------|------------|----------------|
| **1** | V1 | Auth: users, refresh_tokens, audit_logs; JWT security; login/refresh/logout |
| **2** | V3 | Departments CRUD |
| **3** | V4 | WorkSchedules CRUD |
| **4** | V5 | Employees CRUD (FKs to user, department, schedule) + EmploymentStatus enum |
| **5** | V6 + V7 | QR attendance: qr_sessions + attendance_records; HMAC-SHA256 QR signing; Redis session cache; check-in with status calculation (PRESENT/LATE/HALF_DAY) based on WorkSchedule thresholds; ON_LEAVE status |
| **6** | -- | Reporting service: 5 native SQL reports with pagination (daily, monthly, late arrivals, absences via recursive CTE, department stats) |
| **7** | -- | Absent scheduler: `@Scheduled(cron = "0 59 23 * * ?")` — auto-marks ABSENT at 23:59 |
| **8** | -- | Global exception handler (`@RestControllerAdvice`) + Swagger security scheme |
| **9** | V8 | Leave management + notifications: leave requests with approve/reject workflow; auto-creates ON_LEAVE attendance records on approval; notification CRUD |
| **10** | -- | Docker Compose (MySQL + Redis + App), env-based config, project restructure (backend/ + frontend/) |

---

## 8. Deployment

### Docker Compose

```bash
# Start everything
docker compose up -d

# Open Swagger
open http://localhost:8080/swagger-ui/index.html
```

Containers: `mysql:8.0`, `redis:7-alpine`, `app` (built from `backend/Dockerfile`)

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | localhost | MySQL host |
| `DB_PORT` | 3306 | MySQL port |
| `DB_NAME` | attendance_db | Database name |
| `DB_USER` | attendance_user | Database user |
| `DB_PASS` | Attendance@2026# | Database password |
| `DB_ROOT_PASS` | Root@2026# | MySQL root password |
| `REDIS_HOST` | localhost | Redis host |
| `REDIS_PORT` | 6379 | Redis port |
| `JWT_SECRET` | (256-bit key) | JWT signing secret |
| `QR_HMAC_SECRET` | (32+ chars) | QR HMAC signing secret |

### Bootstrap Admin

| Field | Value |
|-------|-------|
| Email | `admin@example.com` |
| Password | `Admin@123` |
| Role | `SUPER_ADMIN` |

Created automatically on first startup (when `users` table is empty).

---

## 9. Key Business Logic

### QR Check-in Flow
1. HR creates QR session → receives `qrPayload` + `qrSignature`
2. Frontend encodes these into a QR code
3. Employee scans QR → sends payload + signature to `POST /check-in`
4. Backend validates HMAC signature → checks Redis for session validity + dedup key
5. Calculates status based on employee's WorkSchedule:
   - Check-in ≤ `startTime + lateAfterMinutes` → **PRESENT**
   - Check-in ≤ `startTime + halfDayAfterMinutes` → **LATE**
   - Check-in > `halfDayAfterMinutes` → **HALF_DAY**
6. On approval: per-day loop → updates existing records to ON_LEAVE or creates new ones

### Leave Approval
1. Employee submits leave → status: `PENDING`
2. HR/Admin reviews → `APPROVE` or `REJECT`
3. On approve: for each day in [start_date, end_date]:
   - If attendance record exists → update status to `ON_LEAVE`
   - If not → create new record with `ON_LEAVE`
4. On approve/reject: create notification for the employee

### Absent Scheduling
- Runs daily at 23:59
- For every `ACTIVE` employee with a WorkSchedule:
  - If no attendance record exists for today → insert record with status `ABSENT`

---

## 10. Services Summary

| Service | File | Key Responsibility |
|---------|------|-------------------|
| `AuthService` | service/AuthService.java | Login, refresh token rotation, logout, password change |
| `UserService` | service/UserService.java | User CRUD |
| `DepartmentService` | service/DepartmentService.java | Department CRUD + delete guard |
| `WorkScheduleService` | service/WorkScheduleService.java | Schedule CRUD |
| `EmployeeService` | service/EmployeeService.java | Employee CRUD + transfer + status change |
| `QrService` | service/QrService.java | QR session creation, HMAC signing, Redis storage |
| `AttendanceService` | service/AttendanceService.java | Check-in, history, status calculation |
| `LeaveService` | service/LeaveService.java | Leave submit, review, attendance record backfill |
| `NotificationService` | service/NotificationService.java | Notification CRUD + unread tracking |
| `ReportingService` | service/ReportingService.java | 5 native SQL reports with pagination |
| `AbsentScheduler` | service/AbsentScheduler.java | Scheduled 23:59 auto-ABSENT marking |

---

## 11. DTO Count: 33 records

| Group | Count | DTOs |
|-------|-------|------|
| Auth | 5 | AuthLoginRequest, AuthLoginResponse, AuthRefreshRequest, AuthRefreshResponse, ChangePasswordRequest |
| User | 3 | UserCreateRequest, UserUpdateRequest, UserDto |
| Department | 3 | CreateDepartmentRequest, UpdateDepartmentRequest, DepartmentDto |
| WorkSchedule | 3 | CreateWorkScheduleRequest, UpdateWorkScheduleRequest, WorkScheduleDto |
| Employee | 5 | CreateEmployeeRequest, UpdateEmployeeRequest, EmployeeTransferRequest, EmployeeStatusRequest, EmployeeDto |
| Attendance | 4 | CheckInRequest, QrSessionResponse, AttendanceDto, AttendanceMapper |
| Leave | 3 | CreateLeaveRequest, ReviewLeaveRequest, LeaveRequestDto |
| Notification | 1 | NotificationDto |
| Reports | 5 | DailyAttendanceDto, MonthlySummaryDto, LateArrivalDto, AbsenceDto, DepartmentStatsDto |
| Error | 1 | ErrorResponse |

---

*Generated: June 2026 — 34 API endpoints, 10 database tables, 11 services, 9 controllers, 33 DTOs.*
