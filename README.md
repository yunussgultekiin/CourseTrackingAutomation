# Course Tracking Automation System

A robust, monolithic desktop application built with **Spring Boot** and **JavaFX** to manage university academic processes. This system digitalizes course registration, grading, and attendance tracking, aimed at minimizing human error and supporting academic decision-making through data analysis.

> [!WARNING]
> **Local Environment & Security Note**
> This project is currently configured for a **Local Development Environment**. 
> While it implements basic authentication and password hashing (BCrypt), advanced security protocols (e.g., OAuth2, SSL/TLS, rigorous session management) are **not fully implemented** for a production deployment. It is intended for demonstration, usage within improved local networks, or as a foundation for further development.

---

## Architecture & Design Principles

The project adheres to strict software engineering principles to ensure maintainability, scalability, and readability:

*   **Monolithic Layered Architecture**: Clear separation of concerns:
    *   **Presentation Layer (UI)**: JavaFX Controllers & FXML (MVC Pattern). Uses **Turkish** for all UI elements.
    *   **Business Layer (Service)**: Contains business logic, validation, and `@Transactional` boundaries. Uses **English** for all code variables and logic.
    *   **Data Access Layer (Repository)**: Spring Data JPA interfaces for database interaction.
*   **Clean Code & SOLID**: Adherence to Single Responsibility, Open/Closed, and other SOLID principles.
*   **DRY (Don't Repeat Yourself)**: Common utilities (e.g., `AlertUtil`) prevent code duplication.
*   **DTO Pattern**: Entity objects are never exposed to the UI; Data Transfer Objects are used for safety and decoupling.

## Key Features

### 1. Role-Based Access Control
The system supports three distinct roles, each with a tailored dashboard:

*   **Admin**: System manager.
*   **Instructor**: Academic staff managing courses.
*   **Student**: End-users enrolling in courses.

### 2. Administrator Module
*   **User Management**: create, update, delete, and list users (Instructors, Students, Admins).
*   **Course Management**: Create courses, assign instructors, set credits/quotas, and manage weekly hour breakdowns (Theory/Practice).
*   **Enrollment Oversight**: View all enrollments system-wide, filter by status/course, and intervene if necessary (Drop/Enroll).
*   **System Statistics**: View high-level metrics (Total Users, Active Courses, Total Enrollments).

### 3. Instructor Module
*   **Course Roster**: View a list of assigned courses and the students enrolled in them.
*   **Grading System**: Enter **Midterm** and **Final** scores. The system automatically calculates:
    *   Average Score.
    *   Letter Grade (AA, BA, BB, etc.).
    *   Pass/Fail Status.
*   **Attendance Tracking**:
    *   Weekly attendance entry (Present/Absent).
    *   Automatic calculation of total absenteeism.
    *   **Critical Thresholds**: Visual warnings if a student exceeds the absenteeism limit based on course hours.

### 4. Student Module
*   **Dashboard**: View current GPA and a summary of subscribed courses.
*   **Course Enrollment**: Browse active courses and self-enroll (subject to quota and duplication checks).
*   **Transcript**: View detailed grade reports, attendance status, and letter grades for all taken courses.
*   **Profile**: Manage personal contact information.

## 🛠 Technology Stack

*   **Language**: Java 21
*   **Framework**: Spring Boot 3.x (Spring Data JPA, Spring Security Crypto)
*   **UI Library**: JavaFX 21 (Modular FXML design)
*   **Database**: PostgreSQL
*   **Build Tool**: Maven
*   **Tools**: Lombok, SLF4J (Logging)

## Setup & Installation

### Prerequisites
*   JDK 21 or higher
*   Maven
*   PostgreSQL Database

### Steps
1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/yunussgultekiin/CourseTrackingAutomation.git
    ```
2.  **Configure Database**:
    *   Create a PostgreSQL database (e.g., `course_automation`).
    *   Update `src/main/resources/application.properties` with your credentials:
        ```properties
        spring.datasource.url=jdbc:postgresql://localhost:5432/course_automation
        spring.datasource.username=postgres
        spring.datasource.password=your_password
        ```
3.  **Build the Project**:
    ```bash
    mvn clean install
    ```
4.  **Run the Application**:
    ```bash
    mvn clean package
    ```
    *Alternatively, run the `CourseTrackingLauncher` class from your IDE.*

### Initial Login
A default admin account is created automatically on the first run if configured in `DataSeeder`:
*   **Username**: `admin`
*   **Password**: `123`

---
*Developed with a focus on Clean Architecture and Modern Java Practices.*
