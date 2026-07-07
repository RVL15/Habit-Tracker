# Premium Spring Boot Habit Tracker

A modern, SaaS-like Habit Tracker web application built using **Spring Boot**, **Hibernate**, **Thymeleaf**, **Bootstrap 5**, and **MySQL**. The application features dynamic real-time analytics, habit performance ranking widgets, interactive calendar grids, and a timezone-locked verification layer.

---

## 🚀 Key Features

* **SaaS Dashboard Layout**: Two-column layout with metrics summary cards, goal tracking status meters, and dynamic motivational quotes.
* **Animated Progress Charts**: Beautiful circular progress doughnuts powered by **Chart.js** displaying Overall, Today's, Weekly, and Monthly completions.
* **Timezone Locked Calendar Grid (`Asia/Kolkata`)**: 
  - **Live IST Clock**: Centered ticking dashboard clock formatting day/time parameters dynamically.
  - **Interactive Locking**: Past days are read-only (`✔` for completed, `—` for missed), future dates are disabled, and today's column is highlighted and editable.
  - **Midnight Auto-Refresh**: Auto-reloads at midnight in IST to lock yesterday's cells.
  - **Secure Endpoint Guard**: Rejects updates for dates other than today.
* **Weekly Performance Analysis**: Cards representing Done vs Pending ratios and mini progress meters for the trailing 7 days.
* **Monthly Aggregates**: Summarizes completed, missed, and perfect days (days where all active habits are completed) for the current month.
* **Performance Rankings**: Identifies the best and worst-performing habits based on monthly consistency rates.
* **BCrypt Registration & Login**: User registration with remember-me token settings and session validation rules.

---

## 🛠 Tech Stack

* **Java Version**: 21
* **Framework**: Spring Boot 3.3.1 (Spring MVC, Spring Data JPA, Spring Security)
* **Template Engine**: Thymeleaf (with Spring Security integration)
* **Frontend Libraries**: Bootstrap 5, Bootstrap Icons, Chart.js (via CDN)
* **Database**: MySQL 8.0 (Database: `habit_tracker_db`)
* **Utilities**: Lombok, JSR-380 Validation

---

## 🗄 Database Configuration

Ensure MySQL is running and create the target schema:
```sql
CREATE DATABASE IF NOT EXISTS habit_tracker_db;
```

Update details in `src/main/resources/application.properties` if needed:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/habit_tracker_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```

---

## ⚙ Build & Run

### Compiling Code
Build and compile classes using Maven:
```bash
mvn clean compile
```

### Running Tests
Execute the integrational mock MVC and repository test suites:
```bash
mvn test
```

### Starting Server
Launch the Spring Boot web server locally:
```bash
mvn spring-boot:run
```
Visit the application in your browser: [http://localhost:8080/dashboard](http://localhost:8080/dashboard).

---

## 📁 Key File Locations

* **DTO Layer**: [`DashboardAnalyticsDto.java`](file:///c:/Users/Admin/Desktop/PROJECTS/Java%20Projects/src/main/java/com/habittracker/dto/DashboardAnalyticsDto.java)
* **Service Layer**: [`DashboardAnalyticsService.java`](file:///c:/Users/Admin/Desktop/PROJECTS/Java%20Projects/src/main/java/com/habittracker/service/DashboardAnalyticsService.java)
* **Controller Layer**: [`HabitController.java`](file:///c:/Users/Admin/Desktop/PROJECTS/Java%20Projects/src/main/java/com/habittracker/controller/HabitController.java)
* **UI Views**: [`index.html`](file:///c:/Users/Admin/Desktop/PROJECTS/Java%20Projects/src/main/resources/templates/index.html)
* **Test Suite**: [`HabitServiceAndTrackerTests.java`](file:///c:/Users/Admin/Desktop/PROJECTS/Java%20Projects/src/test/java/com/habittracker/HabitServiceAndTrackerTests.java)
