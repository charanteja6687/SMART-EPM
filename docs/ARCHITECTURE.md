# System Architecture & Flows

## 1. High-Level Architecture

```
┌──────────────────────────────┐        HTTPS/JSON        ┌────────────────────────────────────┐
│         React Frontend        │  ───────────────────────▶ │        Spring Boot Backend          │
│  (React Router, Axios, MUI)   │ ◀─────────────────────────│  Controller → Service → Repository  │
└──────────────────────────────┘        JWT in header       └───────────────┬────────────────────┘
                                                                              │
                                                                     JPA/Hibernate │  SMTP (JavaMailSender)
                                                                              ▼             ▼
                                                              ┌────────────────────┐   ┌──────────────┐
                                                              │      MySQL DB       │   │  Email (OTP)  │
                                                              │ users, employees,   │   └──────────────┘
                                                              │ projects, tasks,    │
                                                              │ project_employee,   │
                                                              │ notifications,      │
                                                              │ activity_logs       │
                                                              └────────────────────┘
```

### Backend layers
```
Controller  -> handles HTTP, validation, delegates to Service
Service     -> business logic, transactions, role-based scoping, audit logging, notification triggers
Repository  -> Spring Data JPA interfaces (query methods + @Query)
Entity      -> JPA-mapped domain objects
DTO         -> request/response contracts (never expose entities directly)
Security    -> JWT filter + provider + UserDetailsService
Exception   -> @RestControllerAdvice global handler (incl. DB constraint safety net)
```

### Database relationships
```
User        1---1  Employee        (auto-linked by email at registration; optional — ADMIN users have none)
Employee    N---N  Project         (join table: project_employee) — drives EMPLOYEE-role project visibility
Project     1---N  Task
Employee    1---N  Task            (assignedTo) — drives EMPLOYEE-role task visibility
Employee    1---N  Notification    (recipient)
(ActivityLog is a standalone append-only audit table)
```

---
## 2. Authentication Flow (JWT)

```
 ┌────────┐                       ┌──────────────┐                     ┌────────────┐
 │ Client │                       │ AuthController│                     │  Database  │
 └───┬────┘                       └──────┬───────┘                     └─────┬──────┘
     │ POST /api/auth/login              │                                   │
     │ {username, password} ────────────▶│                                   │
     │                                   │ AuthenticationManager.authenticate│
     │                                   │  (delegates to                    │
     │                                   │   CustomUserDetailsService +      │
     │                                   │   BCryptPasswordEncoder) ────────▶│
     │                                   │◀──────────────────────────────────│
     │                                   │ generate JWT (JwtTokenProvider)   │
     │                                   │ write ActivityLog "LOGIN" entry   │
     │◀──────── 200 {token, role} ───────│                                   │
     │                                   │                                   │
     │ Subsequent requests:              │                                   │
     │ Authorization: Bearer <token> ───▶│ JwtAuthenticationFilter           │
     │                                   │  → validates token                │
     │                                   │  → loads UserPrincipal            │
     │                                   │  → sets SecurityContext           │
     │                                   │ → request proceeds with           │
     │                                   │   role-based @PreAuthorize        │
```

Key points:
- Passwords stored using **BCrypt**.
- JWT is stateless (`SessionCreationPolicy.STATELESS`).
- Role-based access enforced both at the URL level (`SecurityConfig`) and method level (`@PreAuthorize`).

---
## 3. Registration Flow (no manual Employee ID, never crashes)

```
React (Register.js)
  → { username, email, password, role }         [no employeeId field exists anywhere]
    → POST /api/auth/register
      → AuthServiceImpl.register()
        1. Check username/email uniqueness on User → DuplicateResourceException if taken
        2. If role == EMPLOYEE:
             findOrCreateEmployeeForRegistration(email):
               a. EmployeeRepository.findByEmail(email) — checks ALL rows, including soft-deleted
               b. Found + soft-deleted?  → reactivate it (clear deletedAt)
               c. Found + active?        → use it as-is
               d. Not found?             → auto-create a minimal Employee
                                             (fullName = username, department = "Unassigned")
        3. Save User, linked to that Employee (or null for ADMIN)
        4. [catch] DataIntegrityViolationException → clean 409 message, never a raw SQL error
      ← JwtResponse (auto-logged-in, same as login)
```

This guarantees registration always succeeds and never depends on the user knowing an internal numeric ID.

---
## 4. Role-Based Data Scoping (Projects & Tasks)

This is enforced **server-side**, in the service layer — the frontend cannot bypass it by omitting or forging query parameters.

```
GET /api/tasks?employeeId=<anything-or-nothing>
  → JwtAuthenticationFilter sets the authenticated UserPrincipal (has role + linked employeeId)
    → TaskController.search()
      → TaskServiceImpl.searchTasks(...)
        if (current user has ROLE_EMPLOYEE):
            effectiveEmployeeId = principal.getEmployeeId()   // OVERRIDES whatever the client sent
        else (ROLE_ADMIN):
            effectiveEmployeeId = whatever the client requested (or null = all)
        → TaskRepository.searchTasks(..., effectiveEmployeeId, ...)
      ← only tasks assigned to that employee are ever returned to an EMPLOYEE caller
```

The same pattern applies to `GET /api/projects` (via the `employees` many-to-many relation) and to direct `GET /api/{projects|tasks}/{id}` lookups — an EMPLOYEE requesting someone else's project/task by ID gets a 404 (not a 403), so the API doesn't even confirm the resource exists to someone unauthorized to see it.

The frontend reflects this by calling the exact same endpoints for both roles — there is no separate "my tasks" vs "all tasks" API; the backend decides what "all" means based on who's asking. When the result set is empty, `Projects.js`/`Tasks.js` show **"No tasks/projects are assigned to you"** for EMPLOYEE users (vs "No projects/tasks found" for ADMIN).

---
## 5. Forgot Password (OTP) Flow

```
Step 1 — Request OTP
  React (ForgotPassword.js, step 1) → POST /api/auth/forgot-password { email }
    → AuthServiceImpl.forgotPassword()
      → find User by email (404 if none)
      → generate 6-digit OTP, save with expiry = now + 5 minutes
      → EmailServiceImpl.sendOtpEmail() via JavaMailSender (SMTP)
    ← 200 "OTP sent"

Step 2 — Verify OTP (UX pre-check; does not consume the OTP)
  React (step 2) → POST /api/auth/verify-otp { email, otp }
    → validate otp matches + not expired → 200, or 400 with a clear message

Step 3 — Reset Password (re-validates + consumes the OTP)
  React (step 3) → POST /api/auth/reset-password { email, otp, newPassword }
    → re-validate otp (never trust step 2 alone)
    → BCrypt-encode newPassword, clear otp fields
    ← 200 "Password reset successfully"
  → redirect to /login
```

---
## 6. Frontend Route Map

```
/login                → Login (+ "Forgot Password?" link)
/register             → Register (no Employee ID field)
/forgot-password       → ForgotPassword (3-step: email → OTP → new password)
/dashboard            → Dashboard (role-aware: admin totals vs employee's own tasks/deadlines)
/employees            → Employees (ADMIN only) — includes soft-delete restore panel
/projects             → Projects (ADMIN: full CRUD + assign; EMPLOYEE: read-only, own projects only)
/tasks                → Tasks   (ADMIN: full CRUD; EMPLOYEE: progress updates on own tasks only)
/reports              → Reports (ADMIN only)
/activity-logs        → ActivityLog (ADMIN only)
```

`PrivateRoute` wraps authenticated pages; unauthenticated users are redirected to `/login`.

---
## 7. Deployment Topology (local dev — no Docker)

```
localhost:3000  → React dev server (npm start)
localhost:8080  → Spring Boot (embedded Tomcat)
localhost:3306  → MySQL
SMTP (e.g. Gmail) → outbound only, for OTP emails
```
