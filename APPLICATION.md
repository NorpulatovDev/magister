# Magister - Education CRM System

## Overview

Magister is a Spring Boot-based Education CRM (Customer Relationship Management) system designed for managing educational groups, students, teachers, attendance, payments, and a gamification coin system.

## Technology Stack

| Technology       | Version | Purpose                    |
|------------------|---------|----------------------------|
| Spring Boot      | 3.2.0   | Application framework      |
| Java             | 17      | Programming language        |
| PostgreSQL       | -       | Database                   |
| Spring Security  | -       | Authentication & authorization |
| JWT (JJWT)       | 0.11.5  | Token-based authentication |
| Spring Data JPA  | -       | ORM / Data access          |
| Lombok           | -       | Boilerplate reduction      |
| springdoc-openapi| 2.3.0   | Swagger / API documentation|
| Maven            | -       | Build tool                 |

## Architecture

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Controller  │────▶│   Service    │────▶│  Repository  │────▶│  PostgreSQL  │
│   (REST API) │     │  (Business)  │     │   (JPA)      │     │  (Database)  │
└──────────────┘     └──────────────┘     └──────────────┘     └──────────────┘
       │
       │  JWT Filter
       ▼
┌──────────────┐
│   Security   │
│  (JWT Auth)  │
└──────────────┘
```

## User Roles

The system has three roles with different permission levels:

| Role      | Description                                      |
|-----------|--------------------------------------------------|
| **ADMIN** | Full system access. Can manage all users, groups, payments, attendance, and coins. |
| **TEACHER** | Can manage own groups, enroll students, mark attendance, record payments, and award coins. |
| **STUDENT** | Read-only access to own data: groups, attendance, payments, and coins. Can update own profile. |

## Entities

### User
Core entity representing all system users (admins, teachers, students).
- Fields: `id`, `email` (unique), `password` (bcrypt-hashed), `fullName`, `phone`, `role`, `createdAt`

### Group
Represents an educational group/class led by a teacher.
- Fields: `id`, `name`, `description`, `teacher` (FK), `schedule`, `status` (ACTIVE/INACTIVE/COMPLETED), `createdAt`

### GroupStudent (Join Table)
Enrollment relationship between students and groups.
- Fields: `id`, `group` (FK), `student` (FK), `enrolledAt`, `completedAt`, `status` (ACTIVE/COMPLETED/DROPPED)

### Attendance
Tracks student attendance per lesson.
- Fields: `id`, `student` (FK), `group` (FK), `markedBy` (FK), `lessonDate`, `status` (PRESENT/ABSENT/LATE), `notes`, `createdAt`

### Payment
Records payments made by students.
- Fields: `id`, `student` (FK), `teacher` (FK), `group` (FK), `amount`, `paymentDate`, `method` (CASH/CARD/TRANSFER), `notes`, `createdAt`

### Coin
Gamification token awarded by teachers to students for achievements.
- Fields: `id`, `student` (FK), `teacher` (FK), `group` (FK), `amount`, `reason`, `awardedDate`

## Authentication Flow

1. **Register**: `POST /api/auth/register` - Create a new account with email, password, fullName, and role
2. **Login**: `POST /api/auth/login` - Authenticate with email/password, receive a JWT token
3. **Use Token**: Include `Authorization: Bearer <token>` header in all subsequent requests
4. **Token Contents**: JWT carries `userId`, `email`, and `role` claims; expires in 24 hours

## API Endpoints

### Auth (`/api/auth`) - Public

| Method | Endpoint     | Description              |
|--------|-------------|--------------------------|
| POST   | `/login`    | Login, returns JWT token |
| POST   | `/register` | Register new user        |
| GET    | `/profile`  | Get current user profile |
| PUT    | `/profile`  | Update own profile       |

### Student (`/api/student`) - STUDENT & ADMIN

| Method | Endpoint              | Description                          |
|--------|-----------------------|--------------------------------------|
| GET    | `/dashboard`          | Dashboard with groups, attendance, payments, coins |
| GET    | `/groups`             | List enrolled groups                 |
| GET    | `/attendance`         | View own attendance records          |
| GET    | `/attendance/summary` | Attendance stats (present/absent/late counts + rate) |
| GET    | `/payments`           | View own payment records             |
| GET    | `/coins`              | View all earned coins                |
| GET    | `/coins/summary`      | Total coins + last 10 coins          |
| GET    | `/coins/total`        | Total coin count (single number)     |

### Teacher (`/api/teacher`) - TEACHER & ADMIN

| Method | Endpoint                              | Description                     |
|--------|---------------------------------------|---------------------------------|
| GET    | `/dashboard`                          | Teacher dashboard               |
| GET    | `/groups`                             | List own groups                 |
| POST   | `/students`                           | Create a student user           |
| GET    | `/students`                           | List own students               |
| GET    | `/students/{id}`                      | Get student details             |
| PUT    | `/students/{id}`                      | Update student                  |
| DELETE | `/students/{id}`                      | Delete student                  |
| POST   | `/attendance`                         | Mark attendance                 |
| PUT    | `/attendance/{id}`                    | Update attendance               |
| DELETE | `/attendance/{id}`                    | Delete attendance               |
| GET    | `/attendance/student/{studentId}`     | View student's attendance       |
| GET    | `/attendance/group/{groupId}`         | View group attendance           |
| POST   | `/payments`                           | Record a payment                |
| PUT    | `/payments/{id}`                      | Update a payment                |
| DELETE | `/payments/{id}`                      | Delete a payment                |
| GET    | `/payments/student/{studentId}`       | View student's payments         |
| GET    | `/payments/stats`                     | Payment statistics              |
| POST   | `/coins`                              | Award coins to student          |
| GET    | `/coins/student/{studentId}`          | View student's coins            |
| GET    | `/coins/group/{groupId}`              | View group's coins              |
| GET    | `/coins/leaderboard/{groupId}`        | Group coin leaderboard          |

### Admin (`/api/admin`) - ADMIN only

| Method | Endpoint                              | Description                     |
|--------|---------------------------------------|---------------------------------|
| GET    | `/dashboard`                          | Admin dashboard                 |
| POST   | `/users`                              | Create any user                 |
| GET    | `/users`                              | List all users                  |
| GET    | `/users/{id}`                         | Get user details                |
| PUT    | `/users/{id}`                         | Update any user                 |
| DELETE | `/users/{id}`                         | Delete any user (cascade)       |
| GET    | `/users/role/{role}`                  | Filter users by role            |
| GET    | `/students/orphaned`                  | Students not in any group       |
| POST   | `/groups`                             | Create group for any teacher    |
| PUT    | `/groups/{id}`                        | Update any group                |
| GET    | `/groups`                             | List all groups                 |
| POST   | `/payments`                           | Create payment for any group    |
| PUT    | `/payments/{id}`                      | Update any payment              |
| DELETE | `/payments/{id}`                      | Delete any payment              |
| GET    | `/attendance/student/{studentId}`     | View any student's attendance   |
| GET    | `/attendance/group/{groupId}`         | View any group's attendance     |
| GET    | `/coins/student/{studentId}`          | View any student's coins        |
| GET    | `/coins/group/{groupId}`              | View any group's coins          |
| GET    | `/coins/leaderboard/{groupId}`        | Any group leaderboard           |

### Groups (`/api/groups`) - TEACHER & ADMIN

| Method | Endpoint                              | Description                     |
|--------|---------------------------------------|---------------------------------|
| POST   | `/`                                   | Create a group                  |
| PUT    | `/{id}`                               | Update a group                  |
| GET    | `/`                                   | List all groups                 |
| GET    | `/{id}`                               | Get group details               |
| GET    | `/{id}/students`                      | Get students in group           |
| POST   | `/{groupId}/enroll/{studentId}`       | Enroll student in group         |
| DELETE | `/{groupId}/students/{studentId}`     | Remove student from group       |

## Coin System (Gamification)

The coin system is a gamification feature that allows teachers to reward students.

### How Coins Work

1. **Awarding**: Teachers award coins to students in their groups via `POST /api/teacher/coins`
2. **Accumulation**: Coins accumulate over time with no expiration
3. **Tracking**: Each coin record stores who awarded it, to which student, in which group, the amount, and a reason
4. **Leaderboard**: Students are ranked by total coins within each group

### How a Student Gets Their Coins

A student can retrieve their coin information through three endpoints:

#### 1. Get All Coins - `GET /api/student/coins`
Returns a list of all coin awards the student has received.

```bash
curl -H "Authorization: Bearer <token>" \
  https://magister-production-a4a6.up.railway.app/api/student/coins
```

**Response:**
```json
[
  {
    "id": 1,
    "studentId": 5,
    "studentName": "John Doe",
    "groupId": 2,
    "groupName": "Math 101",
    "teacherName": "Jane Smith",
    "amount": 10,
    "reason": "Excellent homework",
    "awardedDate": "2026-02-15T10:30:00"
  }
]
```

#### 2. Get Coin Summary - `GET /api/student/coins/summary`
Returns the total coins earned plus the 10 most recent coin awards.

```bash
curl -H "Authorization: Bearer <token>" \
  https://magister-production-a4a6.up.railway.app/api/student/coins/summary
```

**Response:**
```json
{
  "totalCoins": 150,
  "recentCoins": [
    {
      "id": 10,
      "amount": 5,
      "reason": "Great participation",
      "awardedDate": "2026-02-16T14:00:00"
    }
  ]
}
```

#### 3. Get Total Coins - `GET /api/student/coins/total`
Returns the total number of coins earned (a single integer).

```bash
curl -H "Authorization: Bearer <token>" \
  https://magister-production-a4a6.up.railway.app/api/student/coins/total
```

**Response:**
```json
150
```

#### 4. Via Dashboard - `GET /api/student/dashboard`
The student dashboard also includes coin information alongside groups, attendance, and payments.

### Coin Award Flow

```
Teacher                          System                         Database
  │                                │                               │
  │  POST /api/teacher/coins       │                               │
  │  {studentId, groupId,          │                               │
  │   amount, reason}              │                               │
  │───────────────────────────────▶│                               │
  │                                │  Validate:                    │
  │                                │  - Student exists?            │
  │                                │  - Teacher owns group?        │
  │                                │  - Student enrolled (ACTIVE)? │
  │                                │  - Amount > 0?                │
  │                                │───────────────────────────────▶│
  │                                │           Save Coin record    │
  │                                │◀───────────────────────────────│
  │◀───────────────────────────────│                               │
  │         CoinDTO response       │                               │
```

## Workflow Summary

### Complete Student Lifecycle

```
1. REGISTRATION
   Admin/Teacher creates student account
        │
        ▼
2. ENROLLMENT
   Teacher enrolls student in a group
        │
        ▼
3. ACTIVE LEARNING
   ├── Teacher marks attendance (PRESENT/ABSENT/LATE)
   ├── Teacher records payments (CASH/CARD/TRANSFER)
   └── Teacher awards coins (gamification)
        │
        ▼
4. STUDENT ACCESS
   Student views via API:
   ├── Dashboard  → GET /api/student/dashboard
   ├── Groups     → GET /api/student/groups
   ├── Attendance → GET /api/student/attendance
   ├── Payments   → GET /api/student/payments
   └── Coins      → GET /api/student/coins
        │
        ▼
5. COMPLETION
   Teacher marks enrollment as COMPLETED or DROPPED
```

## Configuration

### Environment Variables

| Variable      | Default        | Description                      |
|---------------|----------------|----------------------------------|
| `PGHOST`      | `localhost`    | PostgreSQL host                  |
| `PGPORT`      | `5432`         | PostgreSQL port                  |
| `PGDATABASE`  | `railway`      | Database name                    |
| `PGUSER`      | `postgres`     | Database username                |
| `PGPASSWORD`  | `1111`         | Database password                |
| `JWT_SECRET`  | (default key)  | JWT signing secret (min 256 bits)|
| `APP_BASE_URL`| `https://magister-production-a4a6.up.railway.app/` | API base URL |
| `PORT`        | `8080`         | Server port                      |

### Running Locally

```bash
# Ensure PostgreSQL is running
# Set environment variables or use defaults in application.properties

mvn spring-boot:run
```

### API Documentation

Swagger UI is available at:
- **Local**: `http://localhost:8080/swagger-ui.html`
- **Production**: `https://magister-production-a4a6.up.railway.app/swagger-ui.html`

## Project Structure

```
src/main/java/com/example/magister/
├── config/
│   ├── SecurityConfig.java        # Security rules, JWT filter, role-based access
│   ├── CorsConfig.java            # CORS configuration (all origins allowed)
│   └── OpenApiConfig.java         # Swagger/OpenAPI setup
├── controller/
│   ├── AuthController.java        # Login, register, profile
│   ├── StudentController.java     # Student-facing endpoints
│   ├── TeacherController.java     # Teacher-facing endpoints
│   ├── AdminController.java       # Admin-facing endpoints
│   ├── GroupController.java       # Group management
│   └── UserController.java        # User CRUD (used by admin/teacher)
├── service/
│   ├── AuthService.java           # Authentication logic
│   ├── UserService.java           # User management
│   ├── GroupService.java          # Group management
│   ├── CoinService.java           # Coin awarding & queries
│   ├── PaymentService.java        # Payment recording
│   ├── AttendanceService.java     # Attendance tracking
│   └── DashboardService.java      # Dashboard aggregation
├── entity/
│   ├── User.java                  # User entity
│   ├── Group.java                 # Group entity
│   ├── GroupStudent.java          # Enrollment join entity
│   ├── Coin.java                  # Coin entity
│   ├── Payment.java               # Payment entity
│   ├── Attendance.java            # Attendance entity
│   └── UserRole.java              # Role enum (ADMIN, TEACHER, STUDENT)
├── repository/
│   ├── UserRepository.java
│   ├── GroupRepository.java
│   ├── GroupStudentRepository.java
│   ├── CoinRepository.java
│   ├── PaymentRepository.java
│   └── AttendanceRepository.java
├── security/
│   ├── JwtTokenProvider.java      # JWT generation & validation
│   └── JwtAuthenticationFilter.java # Request authentication filter
├── dto/                           # Data Transfer Objects (31 classes)
└── exception/                     # Custom exception handling
```

## Security

- **Authentication**: Stateless JWT-based authentication
- **Password Storage**: BCrypt hashing
- **Authorization**: Role-based access control (RBAC) enforced at both URL and method levels
- **CORS**: Configured for cross-origin requests
- **CSRF**: Disabled (appropriate for stateless REST APIs)
- **Session**: Stateless (no server-side sessions)
