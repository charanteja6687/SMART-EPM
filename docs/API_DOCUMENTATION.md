# API Documentation — Smart Employee & Project Management System

Base URL: `http://localhost:8080/api`

> **Interactive docs**: once the backend is running, a live, testable version of this API is available at `http://localhost:8080/swagger-ui.html`. Log in via `/api/auth/login`, copy the returned token, click "Authorize" and paste it in.

All endpoints (except `/auth/**`) require header:
```
Authorization: Bearer <JWT_TOKEN>
```

Standard response wrapper:
```json
{ "success": true, "message": "Description", "data": { }, "timestamp": "2026-07-21T10:00:00" }
```

---
## 1. Auth

### POST /auth/register
No `employeeId` field exists anywhere in this API — when registering as EMPLOYEE, the backend automatically finds an existing Employee record matching your email, or creates one on the fly.

Body:
```json
{ "username": "jdoe", "email": "jdoe@smartepm.com", "password": "Password@123", "role": "EMPLOYEE" }
```
Response 201:
```json
{ "success": true, "message": "Registration successful",
  "data": { "token": "<jwt>", "type": "Bearer", "userId": 2, "username": "jdoe", "email": "jdoe@smartepm.com", "role": "EMPLOYEE" } }
```

### POST /auth/login
```json
{ "username": "admin", "password": "Admin@123" }
```
Response 200: same shape as register.

### POST /auth/forgot-password
Generates a 6-digit OTP (valid 5 minutes) and emails it.
```json
{ "email": "jdoe@smartepm.com" }
```
Response 200: `{ "success": true, "message": "An OTP has been sent to your email. It is valid for 5 minutes." }`

### POST /auth/verify-otp
Checks the OTP is correct and not expired, without consuming it.
```json
{ "email": "jdoe@smartepm.com", "otp": "483920" }
```

### POST /auth/reset-password
Re-validates the OTP and updates the password.
```json
{ "email": "jdoe@smartepm.com", "otp": "483920", "newPassword": "NewPassword@123" }
```

---
## 2. Employees (ADMIN only — full CRUD; EMPLOYEE has no access to this resource)

| Method | Endpoint | Description |
|---|---|---|
| POST | /employees | Create employee |
| PUT | /employees/{id} | Update employee |
| DELETE | /employees/{id} | Soft-delete employee — sets `deletedAt`, row is kept |
| PATCH | /employees/{id}/restore | Restore a soft-deleted employee |
| GET | /employees/deleted | List soft-deleted employees |
| GET | /employees/{id} | Get employee by id |
| GET | /employees?keyword=&department=&page=0&size=10&sortBy=id&direction=ASC | Search + paginate + sort |

---
## 3. Projects — role-scoped

| Method | Endpoint | Description |
|---|---|---|
| POST | /projects | Create project (ADMIN) |
| PUT | /projects/{id} | Update project (ADMIN) |
| DELETE | /projects/{id} | Soft-delete project (ADMIN) — cascades to its tasks |
| PATCH | /projects/{id}/restore | Restore a soft-deleted project (ADMIN) |
| GET | /projects/deleted | List soft-deleted projects (ADMIN) |
| GET | /projects/{id} | Get project by id — **ADMIN**: any project. **EMPLOYEE**: only if assigned to it, else 404 |
| GET | /projects?keyword=&status=&priority=&page=&size= | Search + filter — **ADMIN**: all projects. **EMPLOYEE**: automatically restricted to their own assigned projects server-side, regardless of query params |
| PUT | /projects/{id}/assign-employees | Assign employees (body: `[1,2,3]`) (ADMIN) |

Sample create body:
```json
{ "name": "Smart EPM Rollout", "description": "Internal rollout", "status": "ACTIVE",
  "priority": "HIGH", "startDate": "2026-07-01", "deadline": "2026-09-30", "employeeIds": [1,2] }
```
Response `data` includes `totalTasks`, `completedTasks`, `progressPercent` (auto-computed).

---
## 4. Tasks — role-scoped

| Method | Endpoint | Description |
|---|---|---|
| POST | /tasks | Create task (ADMIN) — notifies the assignee |
| PUT | /tasks/{id} | Update task (ADMIN) — notifies the assignee if reassigned |
| PATCH | /tasks/{id}/progress | Update progress/status/remarks (ADMIN/EMPLOYEE) |
| DELETE | /tasks/{id} | Soft-delete task (ADMIN) |
| PATCH | /tasks/{id}/restore | Restore a soft-deleted task (ADMIN) |
| GET | /tasks/deleted | List soft-deleted tasks (ADMIN) |
| GET | /tasks/{id} | Get task by id — **ADMIN**: any task. **EMPLOYEE**: only if assigned to them, else 404 |
| GET | /tasks?keyword=&status=&priority=&projectId=&employeeId=&page=&size= | Search + filter — **ADMIN**: all tasks. **EMPLOYEE**: automatically restricted to tasks assigned to them server-side, regardless of the `employeeId` query param |

Sample progress update body:
```json
{ "progress": 60, "status": "IN_PROGRESS", "remarks": "On track, blocked on review" }
```

---
## 5. Dashboard

| Method | Endpoint | Description |
|---|---|---|
| GET | /dashboard/admin | Totals: employees, projects, tasks, active/completed projects, pending/completed tasks |
| GET | /dashboard/employee | Assigned tasks, completed count, upcoming deadlines (next 7 days) — resolved from the JWT principal's linked employee |

---
## 6. Reports (ADMIN only)

| Method | Endpoint | Description |
|---|---|---|
| GET | /reports/employee-tasks/pdf | Employee-wise task report (PDF) |
| GET | /reports/employee-tasks/excel | Employee-wise task report (Excel) |
| GET | /reports/project-progress/excel | Project progress report (Excel) |
| GET | /reports/pending-tasks/excel | Pending tasks report (Excel) |

---
## 7. Notifications (any authenticated user)

| Method | Endpoint | Description |
|---|---|---|
| GET | /notifications | List the current user's notifications, newest first |
| GET | /notifications/unread-count | `{ "unreadCount": 3 }` |
| PATCH | /notifications/mark-all-read | Marks all as read |
| PATCH | /notifications/{id}/read | Marks one as read |

Notifications are created automatically (e.g. `TASK_ASSIGNED`) — there's no manual "create notification" endpoint.

---
## 8. Activity Log (ADMIN only)

| Method | Endpoint | Description |
|---|---|---|
| GET | /activity-logs?entityType=&actorUsername=&page=0&size=20 | Paginated audit trail, newest first |

Every create/update/delete/restore/progress-update/login/register/password-reset action writes an entry — actor username, action type, entity type + id, description, timestamp.

---
## Error Response Shape
```json
{ "success": false, "message": "Employee not found with id: '99'", "data": null, "timestamp": "..." }
```

Status codes: `200`, `201`, `400` (validation/OTP errors), `401` (unauthenticated), `403` (forbidden), `404` (not found — also used when an EMPLOYEE tries to access a resource not assigned to them, to avoid leaking existence), `409` (duplicate / DB constraint conflict), `500` (unexpected).
