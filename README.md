# 🌿 Habit Tracker — Spring Boot Full-Stack Web App

> A **production-quality**, full-stack Habit Tracker built with **Spring Boot**, **Hibernate**, **Thymeleaf**, **Bootstrap 5**, and **MySQL**. Designed to be simple, clean, and beginner-friendly — yet powerful enough to feel like a real SaaS product.

---

## ✨ Features at a Glance

### 🔐 Authentication
- Secure user **Registration** and **Login** powered by **Spring Security**
- **BCrypt** password hashing with remember-me session support

### ✅ Habit Management
- **Create, Edit, Delete** habits with name, description, color accent, and **category** (Health, Fitness, Study, Finance, etc.)
- Set display order for personalized sorting

### 📅 Monthly Habit Grid (Calendar View)
- Interactive **monthly calendar grid** — each row is a habit, each column is a date
- **Today's column** is highlighted and editable; past dates are locked (`✔` completed / `—` missed); future dates are disabled
- **Midnight auto-refresh** (IST) automatically locks yesterday's cells
- Real-time **IST live clock** on the dashboard header

### 📊 Analytics Dashboard
- **Summary Cards**: Today's Progress %, Current Streak, Longest Streak, Total Habits, Completed Today, Pending Today, Monthly Completion %
- **Chart.js Doughnut Charts**: Visual completion rates for Overall, Today, Weekly, and Monthly periods
- **Weekly Performance Analysis**: Done vs Pending per trailing 7 days
- **Monthly Summary**: Completed days, missed days, perfect days, best/worst habit, improvement rate

### 📈 Analytics & Reports Page (`/analytics`)
- **GitHub-style 365-day contribution heatmap** (5 intensity levels with dynamic **Full Mode** / **Compact Mode** weekday toggle)
- **Smart Insights**: Auto-generated tips like *"Gym completed 92% of the time"* or *"Sunday has the lowest check-ins"*
- **Activity Timeline**: Date-by-date habit log filtered by Week / Month / Year
- **Goals vs Actual**: Daily, Weekly, Monthly targets with Green/Yellow/Red status bars

### 👤 Profile & Settings Page (`/profile` & `/settings`)
- Edit Full Name, Profile Icon, Daily Goal Target, Timezone, Theme (Light & Dark Mode)
- Toggle daily reminder notifications and save settings preferences
- **Change Password** inline
- **Milestone Badges**: Auto-unlock achievements — *First Habit*, *7-Day Streak*, *30-Day Streak*, *100 Completions*, *Perfect Week*, and more

### 🛡 Admin Dashboard Page (`/admin`)
- **System Stats**: Real-time stats showing Total Users, System Administrators, Total Tracked Habits, and Global Completions
- **User CRUD Control**: Create, search, update, and delete system users with clean JPA association cascades
- **Default Seeded Admin Account**:
  - **Email**: `admin@example.com`
  - **Password**: `admin123`

### 📤 Data Export
- **Export CSV**: Download full habit completion log as a `.csv` spreadsheet
- **Export PDF**: Print-optimized HTML report with performance metrics, habit table, and user summary

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.1 (MVC, Data JPA, Security) |
| ORM | Hibernate (via Spring Data JPA) |
| Templating | Thymeleaf |
| Frontend | Bootstrap 5, Bootstrap Icons, Chart.js (CDN) |
| Fonts | Google Fonts — Inter |
| Database | MySQL 8.0 |
| Build Tool | Maven |
| Utilities | Lombok, JSR-380 Bean Validation |

---

## 🗄 Database Setup

Make sure MySQL is running, then create the database:

```sql
CREATE DATABASE IF NOT EXISTS habit_tracker_db;
```

Update your credentials in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/habit_tracker_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password_here

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

> Hibernate will auto-create all tables on first run (`ddl-auto=update`).

---

## ⚙ Build & Run

### 1. Clone the repository
```bash
git clone https://github.com/RVL15/Habit-Tracker.git
cd Habit-Tracker
```

### 2. Compile
```bash
mvn clean compile
```

### 3. Run Tests
```bash
mvn test
```

### 4. Start the Server
```bash
mvn spring-boot:run
```

Open your browser and navigate to:
```
http://localhost:8080
```

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/habittracker/
│   │   ├── controller/          # MVC Controllers (Habit, Auth, Analytics, Profile, Export)
│   │   ├── dto/                 # Data Transfer Objects (Analytics, Badge, Report, Insight...)
│   │   ├── entity/              # JPA Entities (User, Habit, HabitTracker, Badge)
│   │   ├── repository/          # Spring Data JPA Repositories
│   │   ├── service/             # Business Logic Services
│   │   └── HabitTrackerApplication.java
│   └── resources/
│       ├── templates/           # Thymeleaf HTML Views
│       │   ├── index.html       # Dashboard (habit grid + analytics cards)
│       │   ├── habits.html      # Habit management list
│       │   ├── habit-form.html  # Create/Edit habit form
│       │   ├── analytics.html   # Analytics, heatmap, insights, timeline
│       │   ├── profile.html     # Profile editor + badge showcase
│       │   ├── pdf-report.html  # Print-friendly PDF report
│       │   ├── login.html
│       │   └── register.html
│       └── application.properties
└── test/
    └── java/com/habittracker/
        ├── HabitTrackerApplicationTests.java
        └── HabitServiceAndTrackerTests.java
```

---

## 🚦 Application Routes

| URL | Description |
|---|---|
| `GET /` | Redirects to Dashboard |
| `GET /dashboard` | Main habit grid + analytics cards |
| `GET /habits` | Manage all habits (list) |
| `GET /habit/new` | Create new habit form |
| `GET /habit/edit/{id}` | Edit existing habit |
| `POST /habit/toggle` | Mark/unmark a habit for today |
| `GET /analytics` | Reports, heatmap, insights, timeline |
| `GET /profile` | Profile settings and badges |
| `POST /profile/update` | Save profile changes |
| `GET /exports/csv` | Download habit log as CSV |
| `GET /exports/pdf` | Open printable PDF report |
| `GET /login` | Login page |
| `GET /register` | Registration page |

---

## 🏆 Milestone Badges

| Badge | Unlock Condition |
|---|---|
| 🌱 First Habit | Created your first habit |
| ✅ First Check-In | Completed a habit for the first time |
| 🔥 Week Warrior | 7-day streak on any habit |
| 💎 Month Master | 30-day streak |
| 💯 Century Club | 100 total completions |
| 🌟 Perfect Week | All habits completed for 7 consecutive days |

---

## 📝 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

> Built with ❤️ using Spring Boot. Simple. Clean. Professional.
