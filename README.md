# Smart Employee & Project Management System

A full-stack web application for managing employees, projects, and tasks with secure role-based access control (Admin / Employee). The backend is built using **Spring Boot**, **Spring Security (JWT)**, and **MySQL**, while the frontend uses **React**, **Material UI**, and **Axios**.

---

## Overview

This application helps organizations manage employees, projects, and daily tasks from a single platform. Administrators can manage employees, assign projects and tasks, monitor progress, generate reports, and view activity logs. Employees have access only to the projects and tasks assigned to them.

The application includes secure JWT authentication, email OTP-based password reset, dark mode, dashboard analytics, notifications, reporting, and activity tracking.

## Tech Stack

| Layer | Technology / Library | Purpose |
|-------|----------------------|---------|
| Frontend Framework | React 18 | Builds the Single Page Application (SPA) user interface |
| UI & Styling | Material UI (MUI), Emotion | Responsive UI components with dark/light theme support |
| Routing & API Communication | React Router v6, Axios | Client-side routing and REST API communication with JWT authentication |
| Backend Framework | Java 17, Spring Boot 3.2 | RESTful backend application framework |
| Security & Authentication | Spring Security, JWT | Secure authentication and Role-Based Access Control (RBAC) |
| Persistence & ORM | Spring Data JPA, Hibernate | Database interaction and object-relational mapping |
| Database | MySQL 8 | Relational database management system |
| Email Service | Spring Mail (JavaMailSender) | OTP-based password reset and email notifications |
| Reports | Apache POI, iText7 | Excel and PDF report generation |
| API Documentation | Springdoc OpenAPI (Swagger UI) | Interactive REST API documentation and testing |
| Build Tools | Maven, npm | Dependency management, build automation, and package management |

## Screenshots

- Admin dashboard:
	![Admin dashboard](docs/images/Admin_dashboard.jpg)

- Admin - Employees:
	![Admin Employees](docs/images/Adminview_Employee.jpg)

- Admin - Projects:
	![Admin Projects](docs/images/Adminview_Projects.jpg)

- Admin - Tasks:
	![Admin Tasks](docs/images/Adminview_Tasks.jpg)

- Admin - Reports:
	![Admin Reports](docs/images/Adminview_Reports.jpg)

- Activity Log:
	![Activity Log](docs/images/Adminview_Auditlogs.jpg)

- Employee view:
	![Employee view](docs/images/Employeeview.jpg)

- Login portal:
	![Login Portal](docs/images/login_portal.jpg)

- Signup portal:
	![Signup Portal](docs/images/Signup_portal.jpg)

- Architecture flowchart:
	![Architecture flowchart](docs/images/Architecture_flowchart.jpeg)

- Database ER diagram:
	![Database ER diagram](docs/images/Database_ER_diagram.jpeg)

### Authentication & Security

* [x] **JWT-based Login and Registration**: Stateless authentication with JWT tokens.
* [x] **BCrypt Password Encryption**: Secure password hashing before persistence.
* [x] **Role-based Authorization**: Fine-grained access control for ADMIN and EMPLOYEE roles.
* [x] **OTP-based Forgot Password**: Email OTP generation and verification flow using JavaMailSender.
* [x] **Global Exception Handling**: Structured REST exception responses across endpoints.
* [x] **Input Validation**: Payload validation and constraint checks.

### Employee Management

* [x] **Employee Operations**: Create, update, delete, and restore employees.
* [x] **Search, Sorting & Pagination**: Query handling across employee lists.
* [x] **Automatic Account Linking**: Automatic Employee creation/linking during user registration using email.
* [x] **Soft Delete Support**: Flag-based deletion with restore functionality.

### Project Management

* [x] **Project Operations**: Create and manage project lifecycles.
* [x] **Team Assignment**: Assign employees to projects.
* [x] **Status & Priority**: Track project progress, status, and priority levels.
* [x] **Deadline Tracking**: Start and end date management.
* [x] **Automatic Progress Calculation**: Project progress automatically updated based on completed tasks.
* [x] **Soft Delete & Restore**: Soft delete support with restoration.

### Task Management

* [x] **Task Operations**: Create, assign, and manage daily tasks.
* [x] **Progress & Status Updates**: Track task progress, status transitions, and add remarks.
* [x] **Soft Delete & Restore**: Recover soft-deleted tasks.
* [x] **Automatic Notifications**: Task notifications generated on assignment/updates.

### Dashboards & User Interface

* [x] **Admin Dashboard Counters**: Total Employees, Total Projects, Total Tasks, Active Projects, Completed Projects, Pending Tasks, Completed Tasks.
* [x] **Employee Dashboard**: Assigned Tasks, Completed Tasks, Upcoming Deadlines, Personal task overview.
* [x] **Additional Features**: In-app notifications with unread badge, Activity Log (Admin only), Dark Mode (stored in LocalStorage), Swagger API Documentation, Excel & PDF Reports (Apache POI & iText7), Responsive UI, Toast Notifications, Loading states, and error handling.


Role-Based Access Control (RBAC) Matrix
Feature / Permission	Admin (ADMIN)	Employee (EMPLOYEE)	Server-Side Guard
Manage Employees (Create, Update, Delete)	✅	❌	Server-enforced role check
Restore Deleted Records	✅	❌	Server-enforced role check
Manage Projects & Assign Teams	✅	❌	Server-enforced role check
Create & Assign Tasks	✅	❌	Server-enforced role check
View Activity Logs	✅	❌	Server-enforced role check
View Reports (Excel / PDF)	✅	❌	Server-enforced role check
View Assigned Projects	✅	✅ (Assigned Only)	Server-enforced filtering
View Assigned Tasks	✅	✅ (Assigned Only)	Server-enforced filtering
Update Task Progress & Remarks	✅	✅	Server-enforced ownership
View Personal Dashboard	✅	✅	Server-enforced role check
Reset Password (OTP)	✅	✅	Public / Authenticated

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

- Java 25
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
