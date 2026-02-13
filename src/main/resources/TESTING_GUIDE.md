# Magister CRM - Testing Guide

This guide provides ready-to-use requests for testing every API endpoint. Use these with Postman, curl, or any HTTP client.

**Base URL**: `http://localhost:8080` (local) or your deployed URL.

---

## Setup

### 1. Start the application

```bash
./mvnw spring-boot:run
```

### 2. Verify it's running

```bash
curl http://localhost:8080/swagger-ui.html
```

---

## Testing Flow

Follow this order to test the full application:

1. Register/Login users
2. Create groups
3. Enroll students
4. Record attendance, payments, coins
5. Check dashboards
6. Test deletion

---

## 1. Authentication

### Register an Admin

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@test.com",
    "password": "admin123",
    "fullName": "Admin User",
    "phone": "+998901234567",
    "role": "ADMIN"
  }'
```

### Register a Teacher

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teacher@test.com",
    "password": "teacher123",
    "fullName": "John Teacher",
    "phone": "+998901234568",
    "role": "TEACHER"
  }'
```

### Register a Student

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@test.com",
    "password": "student123",
    "fullName": "Alice Student",
    "phone": "+998901234569",
    "role": "STUDENT"
  }'
```

### Login (get token)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@test.com",
    "password": "admin123"
  }'
```

**Response** (save the `token` and `userId`):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "email": "admin@test.com",
  "fullName": "Admin User",
  "role": "ADMIN"
}
```

### Set variables for subsequent requests

```bash
# After login, set these:
TOKEN="eyJhbGciOiJIUzI1NiJ9..."
ADMIN_ID=1
TEACHER_ID=2
STUDENT_ID=3
```

### Get Profile

```bash
curl http://localhost:8080/api/auth/profile \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Id: $ADMIN_ID"
```

### Update Profile

```bash
curl -X PUT http://localhost:8080/api/auth/profile \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Id: $ADMIN_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Admin Updated",
    "phone": "+998909999999"
  }'
```

---

## 2. Admin Endpoints

> Login as admin first. Use admin's token.

### User Management

#### Get all users

```bash
curl http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer $TOKEN"
```

#### Get user by ID

```bash
curl http://localhost:8080/api/admin/users/1 \
  -H "Authorization: Bearer $TOKEN"
```

#### Get users by role

```bash
curl http://localhost:8080/api/admin/users/role/STUDENT \
  -H "Authorization: Bearer $TOKEN"
```

#### Get orphaned students

```bash
curl http://localhost:8080/api/admin/students/orphaned \
  -H "Authorization: Bearer $TOKEN"
```

#### Create a user (admin can create any role)

```bash
curl -X POST http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newstudent@test.com",
    "password": "pass123",
    "fullName": "New Student",
    "phone": "+998901111111",
    "role": "STUDENT"
  }'
```

#### Update a user

```bash
curl -X PUT http://localhost:8080/api/admin/users/3 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Alice Updated",
    "phone": "+998902222222",
    "email": "alice.updated@test.com"
  }'
```

#### Delete a user (cascades all related data)

```bash
curl -X DELETE http://localhost:8080/api/admin/users/4 \
  -H "Authorization: Bearer $TOKEN"
```

### Dashboard

```bash
curl http://localhost:8080/api/admin/dashboard \
  -H "Authorization: Bearer $TOKEN"
```

### View data (read-only)

```bash
# All groups
curl http://localhost:8080/api/admin/groups \
  -H "Authorization: Bearer $TOKEN"

# Group details
curl http://localhost:8080/api/admin/groups/1 \
  -H "Authorization: Bearer $TOKEN"

# Students in group
curl http://localhost:8080/api/admin/groups/1/students \
  -H "Authorization: Bearer $TOKEN"

# Payments by teacher
curl http://localhost:8080/api/admin/payments/teacher/$TEACHER_ID \
  -H "Authorization: Bearer $TOKEN"

# Payments by student
curl http://localhost:8080/api/admin/payments/student/$STUDENT_ID \
  -H "Authorization: Bearer $TOKEN"

# Payments by group
curl http://localhost:8080/api/admin/payments/group/1 \
  -H "Authorization: Bearer $TOKEN"

# Attendance by group
curl http://localhost:8080/api/admin/attendance/group/1 \
  -H "Authorization: Bearer $TOKEN"

# Attendance by student
curl http://localhost:8080/api/admin/attendance/student/$STUDENT_ID \
  -H "Authorization: Bearer $TOKEN"

# Coins by student
curl http://localhost:8080/api/admin/coins/student/$STUDENT_ID \
  -H "Authorization: Bearer $TOKEN"

# Coins by group
curl http://localhost:8080/api/admin/coins/group/1 \
  -H "Authorization: Bearer $TOKEN"

# Leaderboard
curl http://localhost:8080/api/admin/coins/leaderboard/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

## 3. Teacher Endpoints

> Login as teacher first. Use teacher's token.

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "teacher@test.com", "password": "teacher123"}'

# Set TEACHER_TOKEN from response
TEACHER_TOKEN="..."
```

### Student CRUD

#### Create a student

```bash
curl -X POST http://localhost:8080/api/teacher/students \
  -H "Authorization: Bearer $TEACHER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "bob@test.com",
    "password": "bob123",
    "fullName": "Bob Student",
    "phone": "+998903333333"
  }'
```

> Note: Role is automatically set to STUDENT. No need to specify it.

#### Get all my students

```bash
curl http://localhost:8080/api/teacher/students \
  -H "Authorization: Bearer $TEACHER_TOKEN" \
  -H "X-User-Id: $TEACHER_ID"
```

#### Get student details

```bash
curl http://localhost:8080/api/teacher/students/3 \
  -H "Authorization: Bearer $TEACHER_TOKEN" \
  -H "X-User-Id: $TEACHER_ID"
```

#### Update student

```bash
curl -X PUT http://localhost:8080/api/teacher/students/3 \
  -H "Authorization: Bearer $TEACHER_TOKEN" \
  -H "X-User-Id: $TEACHER_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Alice Student Updated",
    "phone": "+998904444444"
  }'
```

#### Delete student (only your own students)

```bash
curl -X DELETE http://localhost:8080/api/teacher/students/3 \
  -H "Authorization: Bearer $TEACHER_TOKEN" \
  -H "X-User-Id: $TEACHER_ID"
```

### Group Management

#### Create a group (via /api/groups)

```bash
curl -X POST http://localhost:8080/api/groups \
  -H "Authorization: Bearer $TEACHER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "English B1",
    "description": "Intermediate English course",
    "teacherId": '$TEACHER_ID',
    "schedule": "Mon/Wed/Fri 10:00-11:30"
  }'
```

#### Enroll student in group

```bash
curl -X POST http://localhost:8080/api/groups/1/enroll/3 \
  -H "Authorization: Bearer $TEACHER_TOKEN"
```

#### Get my groups

```bash
curl http://localhost:8080/api/teacher/groups \
  -H "Authorization: Bearer $TEACHER_TOKEN" \
  -H "X-User-Id: $TEACHER_ID"
```

#### Get group details

```bash
curl http://localhost:8080/api/teacher/groups/1 \
  -H "Authorization: Bearer $TEACHER_TOKEN"
```

#### Get students in my group

```bash
curl http://localhost:8080/api/teacher/groups/1/students \
  -H "Authorization: Bearer $TEACHER_TOKEN"
```

#### Update group

```bash
curl -X PUT http://localhost:8080/api/groups/1 \
  -H "Authorization: Bearer $TEACHER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "English B1 - Updated",
    "schedule": "Tue/Thu 14:00-15:30",
    "status": "ACTIVE"
  }'
```

#### Remove student from group

```bash
curl -X DELETE http://localhost:8080/api/groups/1/students/3 \
  -H "Authorization: Bearer $TEACHER_TOKEN"
```

### Payments

#### Record a payment

```bash
curl -X POST http://localhost:8080/api/teacher/payments \
  -H "Authorization: Bearer $TEACHER_TOKEN" \
  -H "X-User-Id: $TEACHER_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 3,
    "groupId": 1,
    "amount": 500000.00,
    "paymentDate": "2026-02-01T10:00:00",
    "method": "CASH",
    "notes": "Monthly payment for February"
  }'
```

#### Get my payments

```bash
curl http://localhost:8080/api/teacher/payments \
  -H "Authorization: Bearer $TEACHER_TOKEN" \
  -H "X-User-Id: $TEACHER_ID"
```

#### Get payments for a student

```bash
curl http://localhost:8080/api/teacher/payments/student/3 \
  -H "Authorization: Bearer $TEACHER_TOKEN"
```

#### Get payments for a group

```bash
curl http://localhost:8080/api/teacher/payments/group/1 \
  -H "Authorization: Bearer $TEACHER_TOKEN"
```

#### Get payment statistics

```bash
curl http://localhost:8080/api/teacher/payments/stats \
  -H "Authorization: Bearer $TEACHER_TOKEN" \
  -H "X-User-Id: $TEACHER_ID"
```

### Attendance

#### Mark attendance

```bash
curl -X POST http://localhost:8080/api/teacher/attendance \
  -H "Authorization: Bearer $TEACHER_TOKEN" \
  -H "X-User-Id: $TEACHER_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 3,
    "groupId": 1,
    "lessonDate": "2026-02-13T10:00:00",
    "status": "PRESENT",
    "notes": ""
  }'
```

#### Update attendance

```bash
curl -X PUT http://localhost:8080/api/teacher/attendance/1 \
  -H "Authorization: Bearer $TEACHER_TOKEN" \
  -H "X-User-Id: $TEACHER_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "LATE",
    "notes": "Arrived 15 minutes late"
  }'
```

#### Get group attendance

```bash
curl http://localhost:8080/api/teacher/attendance/group/1 \
  -H "Authorization: Bearer $TEACHER_TOKEN"
```

#### Get student attendance

```bash
curl http://localhost:8080/api/teacher/attendance/student/3 \
  -H "Authorization: Bearer $TEACHER_TOKEN"
```

### Coins

#### Award coins

```bash
curl -X POST http://localhost:8080/api/teacher/coins \
  -H "Authorization: Bearer $TEACHER_TOKEN" \
  -H "X-User-Id: $TEACHER_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 3,
    "groupId": 1,
    "amount": 10,
    "reason": "Excellent homework"
  }'
```

#### Get student coins

```bash
curl http://localhost:8080/api/teacher/coins/student/3 \
  -H "Authorization: Bearer $TEACHER_TOKEN"
```

#### Get group coins

```bash
curl http://localhost:8080/api/teacher/coins/group/1 \
  -H "Authorization: Bearer $TEACHER_TOKEN"
```

#### Get leaderboard

```bash
curl http://localhost:8080/api/teacher/coins/leaderboard/1 \
  -H "Authorization: Bearer $TEACHER_TOKEN"
```

### Dashboard

```bash
curl http://localhost:8080/api/teacher/dashboard \
  -H "Authorization: Bearer $TEACHER_TOKEN" \
  -H "X-User-Id: $TEACHER_ID"
```

---

## 4. Student Endpoints

> Login as student first. Use student's token.

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "student@test.com", "password": "student123"}'

# Set STUDENT_TOKEN from response
STUDENT_TOKEN="..."
```

### Dashboard

```bash
curl http://localhost:8080/api/student/dashboard \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "X-User-Id: $STUDENT_ID"
```

### My Groups

```bash
curl http://localhost:8080/api/student/groups \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "X-User-Id: $STUDENT_ID"
```

### My Attendance

```bash
curl http://localhost:8080/api/student/attendance \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "X-User-Id: $STUDENT_ID"
```

### Attendance Summary

```bash
curl http://localhost:8080/api/student/attendance/summary \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "X-User-Id: $STUDENT_ID"
```

### My Payments

```bash
curl http://localhost:8080/api/student/payments \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "X-User-Id: $STUDENT_ID"
```

### My Coins

```bash
curl http://localhost:8080/api/student/coins \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "X-User-Id: $STUDENT_ID"
```

### Coin Summary

```bash
curl http://localhost:8080/api/student/coins/summary \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "X-User-Id: $STUDENT_ID"
```

### Total Coins

```bash
curl http://localhost:8080/api/student/coins/total \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "X-User-Id: $STUDENT_ID"
```

---

## 5. Test Scenarios

### Scenario 1: Full Student Lifecycle

```
1. Admin creates a teacher       POST /api/admin/users
2. Teacher creates a student     POST /api/teacher/students
3. Teacher creates a group       POST /api/groups
4. Teacher enrolls student       POST /api/groups/{gid}/enroll/{sid}
5. Teacher marks attendance      POST /api/teacher/attendance
6. Teacher records payment       POST /api/teacher/payments
7. Teacher awards coins          POST /api/teacher/coins
8. Student checks dashboard      GET  /api/student/dashboard
9. Teacher deletes student       DELETE /api/teacher/students/{id}
   (all payments, attendance, coins, enrollments are cascade-deleted)
```

### Scenario 2: Authorization Tests

```
1. Teacher tries to delete another teacher's student  -> 401 Unauthorized
2. Student tries to access teacher endpoints           -> 403 Forbidden
3. Teacher tries to access admin endpoints             -> 403 Forbidden
4. Expired token used                                  -> 401 Unauthorized
5. No token provided                                   -> 401 Unauthorized
```

### Scenario 3: Validation Tests

```
1. Register with existing email          -> 400 "Email already exists"
2. Login with wrong password             -> 400 "Invalid email or password"
3. Create payment for unenrolled student -> 400 "Student is not enrolled"
4. Access non-existent user              -> 404 "User not found"
5. Teacher records payment for another
   teacher's group                       -> 401 "You can only create payments for your own groups"
```

### Scenario 4: Cascade Deletion Test

```
1. Create teacher, student, group, enroll student
2. Record payment, mark attendance, award coins
3. Verify data exists (GET endpoints)
4. Delete the student
5. Verify all related data is gone:
   - Payments deleted
   - Attendance deleted
   - Coins deleted
   - Enrollment deleted
   - Other users/groups unaffected
```

---

## 6. Postman Collection Setup

### Environment Variables

| Variable | Example Value |
|----------|---------------|
| `base_url` | `http://localhost:8080` |
| `admin_token` | _(set after login)_ |
| `teacher_token` | _(set after login)_ |
| `student_token` | _(set after login)_ |
| `admin_id` | _(set after login)_ |
| `teacher_id` | _(set after login)_ |
| `student_id` | _(set after login)_ |
| `group_id` | _(set after group creation)_ |

### Auto-set token in Postman

Add this to the **Tests** tab of your login request:

```javascript
var response = pm.response.json();
pm.environment.set("token", response.token);
pm.environment.set("user_id", response.userId);

if (response.role === "ADMIN") {
    pm.environment.set("admin_token", response.token);
    pm.environment.set("admin_id", response.userId);
} else if (response.role === "TEACHER") {
    pm.environment.set("teacher_token", response.token);
    pm.environment.set("teacher_id", response.userId);
} else if (response.role === "STUDENT") {
    pm.environment.set("student_token", response.token);
    pm.environment.set("student_id", response.userId);
}
```

---

## 7. Common HTTP Status Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created (POST requests) |
| 204 | No Content (DELETE requests) |
| 400 | Bad Request / Business rule violation |
| 401 | Unauthorized / Invalid token |
| 403 | Forbidden / Wrong role |
| 404 | Resource not found |
| 500 | Server error |
