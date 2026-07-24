# Smart Employee & Project Management System

A full-stack web application for managing employees, projects, and tasks with secure role-based access control (Admin / Employee). The backend is built using **Spring Boot**, **Spring Security (JWT)**, and **MySQL**, while the frontend uses **React**, **Material UI**, and **Axios**.

---

## Overview

This application helps organizations manage employees, projects, and daily tasks from a single platform. Administrators can manage employees, assign projects and tasks, monitor progress, generate reports, and view activity logs. Employees have access only to the projects and tasks assigned to them.

The application includes secure JWT authentication, email OTP-based password reset, dark mode, dashboard analytics, notifications, reporting, and activity tracking.

---

## Screenshots

### Key app screens

![Login Page](docs/images/Loginpage.jpg)

![Register Page](docs/images/Registerpage.jpg)

![Dashboard Page](docs/images/Dashboardpage.jpg)

![Employees Page](docs/images/Employeepage.jpg)

![Projects Page](docs/images/Projectspage.jpg)

![Tasks Page](docs/images/Taskpage.jpg)

![Reports Page](docs/images/Reportspage.jpg)

![Activity Log Page](docs/images/Activitylogpage.jpg)

---

## Features

### Authentication & Security

- JWT-based Login and Registration
- BCrypt password encryption
- Role-based authorization (ADMIN / EMPLOYEE)
- OTP-based Forgot Password
- Password Reset using Email OTP
- Global exception handling
- Input validation

### Employee Management

- Create, update, delete, and restore employees
- Search, sorting, and pagination
- Automatic Employee creation/linking during registration
- Soft delete support

### Project Management

- Create and manage projects
- Assign employees to projects
- Status and priority management
- Deadline tracking
- Automatic project progress calculation
- Soft delete with restore support

### Task Management

- Create and assign tasks
- Track progress
- Update task status
- Add remarks
- Soft delete and restore
- Automatic task notifications

### Dashboards

#### Admin Dashboard

- Total Employees
- Total Projects
- Total Tasks
- Active Projects
- Completed Projects
- Pending Tasks
- Completed Tasks

#### Employee Dashboard

- Assigned Tasks
- Completed Tasks
- Upcoming Deadlines
- Personal task overview

### Additional Features

- Email OTP using JavaMailSender
- In-app notifications with unread badge
- Activity Log (Admin only)
- Dark Mode (stored in LocalStorage)
- Swagger API Documentation
- Excel & PDF Reports
- Responsive UI
- Toast Notifications
- Loading states and error handling

---

## Role-Based Access

### Admin

- Manage Employees
- Manage Projects
- Manage Tasks
- Assign Tasks
- View Reports
- View Activity Logs
- Restore Deleted Records

### Employee

- View Assigned Projects
- View Assigned Tasks
- Update Task Progress
- View Dashboard
- Reset Password

The backend strictly enforces role-based access. Employees can only access data assigned to them. This restriction is implemented server-side and cannot be bypassed from the frontend.

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | React 18, React Router 6, Material UI, Axios |
| Backend | Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Database | MySQL 8 |
| Authentication | JWT |
| Email | Spring Mail (JavaMailSender) |
| Reports | Apache POI, iText7 |
| API Documentation | Swagger UI (springdoc-openapi) |
| Build Tools | Maven, npm |

---

## Project Structure

```
project/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/smartepm/
│       ├── config/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── entity/
│       ├── dto/
│       ├── security/
│       └── exception/
│
├── frontend/
│   └── src/
│       ├── components/
│       ├── pages/
│       ├── services/
│       ├── context/
│       └── routes/
│
├── database/
├── postman/
└── docs/
```

---

## Prerequisites

Before running the project, make sure the following are installed:

- Java 17 or later
- Maven 3.8+
- Node.js 18+
- npm
- MySQL 8+
- Gmail account (or another SMTP provider) for OTP emails

---

## Database Setup

Create a MySQL database.

```sql
CREATE DATABASE smart_epm_db;
```

Hibernate uses `ddl-auto=update`, so all required tables are created automatically when the application starts.

The `database/schema.sql` file is included only as a reference.

---

## Backend Setup

Move into the backend directory.

```bash
cd backend
```

Update the database configuration inside:

```
src/main/resources/application.properties
```

## 🔐 Environment Configuration

Sensitive information like database credentials and email passwords are not included in this repository.

Before running the backend, update the file:

backend/src/main/resources/application.properties

Add your own values:

spring.datasource.url=jdbc:mysql://localhost:3306/smart_epm_db  
spring.datasource.username=your-username  
spring.datasource.password=your-password  

spring.mail.username=your-email@gmail.com  
spring.mail.password=your-app-password  

⚠️ Do not upload real credentials to GitHub.
```
\docs\images\Activitylogpage.jpg
### Configure Email

Add your email credentials.

```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-character-app-password
```

For Gmail, enable Two-Factor Authentication and generate an App Password.

If email is not configured, the Forgot Password feature will not send OTP emails, but the remaining application will continue to work normally.

### Run the Backend

```bash
mvn clean install
mvn spring-boot:run
```

The backend runs at:

```
http://localhost:8080
```

A default administrator account is automatically created.

```
Username : admin
Password : Admin@123
```

---

## Frontend Setup

```bash
cd frontend
npm install
npm start
```

The React application runs at:

```
http://localhost:3000
```

---

## Using the Application

### Admin

Login using:

```
Username : admin
Password : Admin@123
```

The administrator can manage employees, projects, tasks, reports, notifications, and activity logs.

### Employee

Register a new account with the **EMPLOYEE** role.

Employee records are automatically linked (or created) using the registration email.

Employees only have access to projects and tasks assigned to them.

If no assignments exist, the application displays a friendly message instead of showing empty tables.

### Forgot Password

Click **Forgot Password** on the login page.

1. Enter your email
2. Receive OTP
3. Verify OTP
4. Set a new password

---

## API Documentation

Swagger UI

```
http://localhost:8080/swagger-ui.html
```

Additional documentation is available inside the `docs` folder.

---

## Postman Collection

Import:

```
postman/Smart-EPM.postman_collection.json
```

Set the following variables:

- baseUrl
- token

---

## Notes

- Hibernate uses `ddl-auto=update`, which is suitable for development.
- JWT secret should be stored using environment variables in production.
- Login and OTP requests do not currently include rate limiting.
- Employee accounts created during registration receive default information such as email, username, and an **Unassigned** department. Administrators can update the remaining employee details later.

---

## Not Included

The following features were intentionally excluded from this version:

- Docker / Containerization
- File Upload (Profile Pictures / Project Attachments)

---