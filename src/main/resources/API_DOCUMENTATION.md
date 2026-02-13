# Magister CRM - Backend API Documentation

## Overview

Magister is an educational CRM system built with Spring Boot 3.2.0 and PostgreSQL. It manages teachers, students, groups, payments, attendance, and a coin reward system.

**Base URL**: `https://magister-production-a4a6.up.railway.app` (production) or `http://localhost:8080` (local)

**Swagger UI**: `{base-url}/swagger-ui.html`

---

## Authentication

All endpoints (except `/api/auth/**`) require JWT authentication.

### How it works

1. Call `POST /api/auth/login` with email and password
2. Receive a JWT token in the response
3. Include the token in all subsequent requests:
   - **Authorization header**: `Bearer <token>`
   - **X-User-Id header**: Your user ID (returned from login)

### JWT Token Details

| Property | Value |
|----------|-------|
| Algorithm | HS256 |
| Expiration | 24 hours |
| Claims | `userId` (Long), `role` (String), `sub` (email) |

---

## Roles & Permissions

| Path | ADMIN | TEACHER | STUDENT |
|------|-------|---------|---------|
| `/api/auth/**` | Yes | Yes | Yes |
| `/api/admin/**` | Yes | No | No |
| `/api/teacher/**` | Yes | Yes | No |
| `/api/student/**` | Yes | No | Yes |
| `/api/groups/**` | Yes | Yes | No |
| `/api/users/**` | Yes | Yes | No |

---

## Enums

| Enum | Values |
|------|--------|
| **UserRole** | `ADMIN`, `TEACHER`, `STUDENT` |
| **GroupStatus** | `ACTIVE`, `INACTIVE`, `COMPLETED` |
| **EnrollmentStatus** | `ACTIVE`, `COMPLETED`, `DROPPED` |
| **AttendanceStatus** | `PRESENT`, `ABSENT`, `LATE` |
| **PaymentMethod** | `CASH`, `CARD`, `TRANSFER` |

---

## DTOs Reference

### LoginRequest
```json
{
  "email": "string",
  "password": "string"
}
```

### LoginResponse
```json
{
  "token": "string",
  "userId": 1,
  "email": "string",
  "fullName": "string",
  "role": "ADMIN | TEACHER | STUDENT"
}
```

### UserDTO
```json
{
  "id": 1,
  "email": "string",
  "fullName": "string",
  "phone": "string",
  "role": "ADMIN | TEACHER | STUDENT"
}
```

### CreateUserRequest
```json
{
  "email": "string",
  "password": "string",
  "fullName": "string",
  "phone": "string",
  "role": "ADMIN | TEACHER | STUDENT"
}
```

### UpdateUserRequest
```json
{
  "fullName": "string",
  "phone": "string",
  "email": "string",
  "password": "string",
  "role": "ADMIN | TEACHER | STUDENT"
}
```

### GroupDTO
```json
{
  "id": 1,
  "name": "string",
  "description": "string",
  "teacherId": 1,
  "teacherName": "string",
  "schedule": "string",
  "status": "ACTIVE | INACTIVE | COMPLETED",
  "studentCount": 10,
  "createdAt": "2026-01-01T10:00:00"
}
```

### CreateGroupRequest
```json
{
  "name": "string",
  "description": "string",
  "teacherId": 1,
  "schedule": "string"
}
```

### UpdateGroupRequest
```json
{
  "name": "string",
  "description": "string",
  "schedule": "string",
  "status": "ACTIVE | INACTIVE | COMPLETED"
}
```

### PaymentDTO
```json
{
  "id": 1,
  "studentId": 1,
  "studentName": "string",
  "teacherId": 1,
  "teacherName": "string",
  "groupId": 1,
  "groupName": "string",
  "amount": 50000.00,
  "paymentDate": "2026-01-15T10:00:00",
  "method": "CASH | CARD | TRANSFER",
  "notes": "string",
  "createdAt": "2026-01-15T10:00:00"
}
```

### CreatePaymentRequest
```json
{
  "studentId": 1,
  "groupId": 1,
  "amount": 50000.00,
  "paymentDate": "2026-01-15T10:00:00",
  "method": "CASH | CARD | TRANSFER",
  "notes": "string"
}
```

### PaymentStatsDTO
```json
{
  "totalPayments": 25,
  "totalAmount": 1250000.00
}
```

### AttendanceDTO
```json
{
  "id": 1,
  "studentId": 1,
  "studentName": "string",
  "groupId": 1,
  "groupName": "string",
  "lessonDate": "2026-01-15T10:00:00",
  "status": "PRESENT | ABSENT | LATE",
  "notes": "string",
  "markedBy": "string",
  "createdAt": "2026-01-15T10:00:00"
}
```

### MarkAttendanceRequest
```json
{
  "studentId": 1,
  "groupId": 1,
  "lessonDate": "2026-01-15T10:00:00",
  "status": "PRESENT | ABSENT | LATE",
  "notes": "string"
}
```

### UpdateAttendanceRequest
```json
{
  "status": "PRESENT | ABSENT | LATE",
  "notes": "string"
}
```

### AttendanceSummary
```json
{
  "totalPresent": 20,
  "totalAbsent": 3,
  "totalLate": 2,
  "totalLessons": 25,
  "attendanceRate": 80.0,
  "recentAttendance": []
}
```

### CoinDTO
```json
{
  "id": 1,
  "studentId": 1,
  "studentName": "string",
  "groupId": 1,
  "groupName": "string",
  "teacherName": "string",
  "amount": 10,
  "reason": "string",
  "awardedDate": "2026-01-15T10:00:00"
}
```

### AwardCoinsRequest
```json
{
  "studentId": 1,
  "groupId": 1,
  "amount": 10,
  "reason": "string"
}
```

### CoinSummary
```json
{
  "totalCoins": 150,
  "recentCoins": []
}
```

### LeaderboardEntryDTO
```json
{
  "studentId": 1,
  "studentName": "string",
  "totalCoins": 150,
  "rank": 1,
  "groupName": "string"
}
```

### AdminDashboardDTO
```json
{
  "totalUsers": 50,
  "totalGroups": 10,
  "activeGroups": 8,
  "totalStudents": 40,
  "totalTeachers": 5,
  "paymentStats": {},
  "recentUsers": []
}
```

### TeacherDashboardDTO
```json
{
  "groups": [],
  "paymentStats": {},
  "recentActivity": [],
  "totalStudents": 20,
  "totalGroups": 3
}
```

### StudentDashboardDTO
```json
{
  "groups": [],
  "attendanceSummary": {},
  "recentPayments": [],
  "coinSummary": {},
  "totalPayments": 5
}
```

---

## API Endpoints

### Auth (`/api/auth`) - Public

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login and get JWT token |
| POST | `/api/auth/register` | Register a new user |
| GET | `/api/auth/profile` | Get current user profile |
| PUT | `/api/auth/profile` | Update current user profile |

**Headers for profile endpoints**: `X-User-Id: {userId}`

---

### Admin (`/api/admin`) - ADMIN only

#### User Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/users` | Get all users |
| GET | `/api/admin/users/{id}` | Get user by ID |
| GET | `/api/admin/users/role/{role}` | Get users by role |
| GET | `/api/admin/students/orphaned` | Get students not in any active group |
| POST | `/api/admin/users` | Create new user (any role) |
| PUT | `/api/admin/users/{id}` | Update any user |
| DELETE | `/api/admin/users/{id}` | Delete user (cascades all related data) |

#### Groups, Payments, Attendance, Coins (Read-only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/groups` | Get all groups |
| GET | `/api/admin/groups/{id}` | Get group by ID |
| GET | `/api/admin/groups/{id}/students` | Get students in group |
| GET | `/api/admin/payments/teacher/{teacherId}` | Get payments by teacher |
| GET | `/api/admin/payments/student/{studentId}` | Get payments by student |
| GET | `/api/admin/payments/group/{groupId}` | Get payments by group |
| GET | `/api/admin/attendance/group/{groupId}` | Get attendance by group |
| GET | `/api/admin/attendance/student/{studentId}` | Get attendance by student |
| GET | `/api/admin/coins/student/{studentId}` | Get coins by student |
| GET | `/api/admin/coins/group/{groupId}` | Get coins by group |
| GET | `/api/admin/coins/leaderboard/{groupId}` | Get group coin leaderboard |
| GET | `/api/admin/dashboard` | Get admin dashboard |

---

### Teacher (`/api/teacher`) - TEACHER, ADMIN

**All endpoints require header**: `X-User-Id: {teacherId}`

#### Student Management (Full CRUD)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/teacher/students` | Create a new student (role forced to STUDENT) |
| GET | `/api/teacher/students` | Get all my students across all groups |
| GET | `/api/teacher/students/{id}` | Get student details |
| PUT | `/api/teacher/students/{id}` | Update student (own students only) |
| DELETE | `/api/teacher/students/{id}` | Delete student (own students only, cascades all data) |

#### Group Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/teacher/groups` | Get my groups |
| GET | `/api/teacher/groups/{id}` | Get group details |
| GET | `/api/teacher/groups/{id}/students` | Get students in my group |

#### Payments (No admin confirmation needed)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/teacher/payments` | Record a payment |
| GET | `/api/teacher/payments` | Get my recorded payments |
| GET | `/api/teacher/payments/student/{studentId}` | Get payments for a student |
| GET | `/api/teacher/payments/group/{groupId}` | Get payments for a group |
| GET | `/api/teacher/payments/stats` | Get payment statistics |

#### Attendance

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/teacher/attendance` | Mark attendance |
| PUT | `/api/teacher/attendance/{id}` | Update attendance record |
| GET | `/api/teacher/attendance/group/{groupId}` | Get group attendance |
| GET | `/api/teacher/attendance/student/{studentId}` | Get student attendance |

#### Coins

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/teacher/coins` | Award coins to student |
| GET | `/api/teacher/coins/student/{studentId}` | Get student coins |
| GET | `/api/teacher/coins/group/{groupId}` | Get group coins |
| GET | `/api/teacher/coins/leaderboard/{groupId}` | Get group leaderboard |

#### Dashboard

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/teacher/dashboard` | Get teacher dashboard |

---

### Student (`/api/student`) - STUDENT, ADMIN

**All endpoints require header**: `X-User-Id: {studentId}`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/student/dashboard` | Get student dashboard |
| GET | `/api/student/groups` | Get my enrolled groups |
| GET | `/api/student/attendance` | Get my attendance history |
| GET | `/api/student/attendance/summary` | Get my attendance summary |
| GET | `/api/student/payments` | Get my payment history |
| GET | `/api/student/coins` | Get my coins |
| GET | `/api/student/coins/summary` | Get my coin summary |
| GET | `/api/student/coins/total` | Get my total coins |

---

### Groups (`/api/groups`) - TEACHER, ADMIN

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/groups` | Create a new group |
| PUT | `/api/groups/{id}` | Update group |
| POST | `/api/groups/{groupId}/enroll/{studentId}` | Enroll student in group |
| DELETE | `/api/groups/{groupId}/students/{studentId}` | Remove student from group |
| GET | `/api/groups` | Get all groups |
| GET | `/api/groups/{id}` | Get group by ID |
| GET | `/api/groups/{id}/students` | Get group students |

---

### Users (`/api/users`) - TEACHER, ADMIN

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users/students` | Create a new student |
| GET | `/api/users` | Get all users |
| GET | `/api/users/{id}` | Get user by ID |
| PUT | `/api/users/{id}` | Update user |
| GET | `/api/users/students` | Get all students |
| GET | `/api/users/teachers` | Get all teachers |

---

## Entity Relationships

```
User (TEACHER) ──1:N──> Group
User (STUDENT) ──1:N──> GroupStudent ──N:1──> Group
User (STUDENT) ──1:N──> Payment ──N:1──> Group
User (TEACHER) ──1:N──> Payment
User (STUDENT) ──1:N──> Attendance ──N:1──> Group
User (TEACHER) ──1:N──> Attendance (as markedBy)
User (STUDENT) ──1:N──> Coin ──N:1──> Group
User (TEACHER) ──1:N──> Coin
```

## Cascade Deletion

When a user is deleted, all related records are automatically cleaned up:

**For any user (student)**:
- All payments where they are the student
- All attendance records
- All coin records
- All group enrollments

**For teachers (additionally)**:
- All payments they recorded
- All attendance they marked
- All coins they awarded
- All groups they own (and those groups' enrollments)

---

## Error Responses

All errors follow this format:

```json
{
  "status": 404,
  "message": "User not found with id: 1",
  "timestamp": "2026-01-15T10:00:00"
}
```

| Status | Exception | Description |
|--------|-----------|-------------|
| 400 | BusinessException | Business rule violation |
| 401 | UnauthorizedException | Permission denied |
| 404 | ResourceNotFoundException | Entity not found |
| 500 | Exception | Unexpected server error |

---

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `PGHOST` | localhost | PostgreSQL host |
| `PGPORT` | 5432 | PostgreSQL port |
| `PGDATABASE` | railway | Database name |
| `PGUSER` | postgres | Database user |
| `PGPASSWORD` | 1111 | Database password |
| `JWT_SECRET` | (built-in) | JWT signing secret (min 256 bits) |
| `PORT` | 8080 | Server port |
| `APP_BASE_URL` | (railway URL) | Application base URL |
