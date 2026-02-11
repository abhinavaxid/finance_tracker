# Smart Expense Tracker with Budgeting & Analytics Dashboard

A comprehensive full-stack financial management application built with Java 8, Spring Boot, Angular 18, and PostgreSQL.

## 📋 Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Database Setup](#database-setup)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Deployment](#deployment)

## 🎯 Overview

Smart Expense Tracker is a production-ready financial management system that helps users track income and expenses, set budgets, receive alerts, and gain insights into their spending patterns. The system features role-based access control, session management with Redis, and comprehensive analytics.

## ✨ Features

### 🔐 Authentication & Security
- User registration and login
- Session-based authentication using Spring Session with Redis
- Role-Based Access Control (USER, ADMIN)
- Secure password hashing with BCrypt
- Session timeout and logout functionality
- Audit logging for all user actions

### 💸 Transaction Management
- Add, edit, and delete income and expense transactions
- Support for multiple payment methods
- Transaction categorization with color-coded categories
- File upload for receipts and invoices
- Search and filter transactions
- Tag support for better organization

### 🗂 Category Management
- System-defined default categories
- User-defined custom categories
- Category icons and color coding
- Separate categories for income and expenses

### 🎯 Budgeting System
- Create monthly budgets per category
- Real-time budget tracking
- Automatic alerts at 80% threshold (configurable)
- Budget exceeded notifications
- Budget status overview (Normal/Warning/Exceeded)

### 🔄 Recurring Transactions
- Set up recurring income (salary, investments)
- Set up recurring expenses (rent, subscriptions)
- Flexible frequency options (Daily, Weekly, Monthly, Quarterly, Yearly)
- Automatic transaction creation

### 📊 Analytics Dashboard
- Income vs Expense comparison
- Category-wise spending breakdown
- Monthly trends and patterns
- Savings calculation
- Visual charts and graphs

### 🧠 Smart Insights
- Rule-based spending analysis
- Overspending detection
- Spending trend analysis
- Low savings warnings
- Unusual activity alerts

### 📬 Notifications & Email
- Welcome email on signup
- Budget alert emails
- Budget exceeded notifications
- Monthly summary reports
- In-app notification center

### 🔍 Search & Export
- Advanced filtering (date range, category, amount)
- Full-text search on descriptions
- Export to CSV format
- Export to PDF format

### 📎 File Management
- Upload receipt images
- Link files to transactions
- Secure file storage
- Supported formats: JPG, PNG, PDF, GIF

### 📜 Audit Trail
- Complete audit logging
- Track all user actions (LOGIN, CREATE, UPDATE, DELETE, etc.)
- IP address and user agent tracking
- Historical data preservation

## 🛠 Technology Stack

### Backend
- **Java**: 1.8
- **Spring Boot**: 2.7.18
- **Spring Security**: Authentication & Authorization
- **Spring Session**: Redis-backed session management
- **Spring Data JPA**: Database operations
- **Hibernate**: ORM framework
- **PostgreSQL**: Primary database
- **Redis**: Session storage and caching
- **Lombok**: Reduce boilerplate code
- **SpringDoc OpenAPI**: API documentation (Swagger)
- **Apache Commons CSV**: CSV export
- **iText**: PDF generation

### Frontend (To be implemented)
- **Angular**: 18
- **Angular Material**: UI components
- **Tailwind CSS**: Utility-first styling
- **RxJS**: Reactive programming
- **Chart.js**: Data visualization

### DevOps
- **Maven**: Build tool
- **Docker**: Containerization
- **GitHub Actions**: CI/CD pipeline
- **JUnit & Mockito**: Testing

## 📁 Project Structure

```
finance_tracker/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/financetracker/
│   │   │   │   ├── ExpenseTrackerApplication.java
│   │   │   │   ├── config/                  # Configuration classes
│   │   │   │   ├── controller/              # REST Controllers
│   │   │   │   ├── dto/                     # Data Transfer Objects
│   │   │   │   ├── exception/               # Custom exceptions
│   │   │   │   ├── model/                   # JPA Entities
│   │   │   │   │   ├── enums/              # Enumerations
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Role.java
│   │   │   │   │   ├── Transaction.java
│   │   │   │   │   ├── Category.java
│   │   │   │   │   ├── Budget.java
│   │   │   │   │   ├── RecurringTransaction.java
│   │   │   │   │   ├── Notification.java
│   │   │   │   │   ├── AuditLog.java
│   │   │   │   │   ├── Insight.java
│   │   │   │   │   └── ...
│   │   │   │   ├── repository/              # JPA Repositories
│   │   │   │   ├── security/                # Security configuration
│   │   │   │   ├── service/                 # Business logic
│   │   │   │   └── util/                    # Utility classes
│   │   │   └── resources/
│   │   │       ├── application.yml          # Main configuration
│   │   │       ├── application-dev.yml      # Dev profile
│   │   │       └── application-prod.yml     # Production profile
│   │   └── test/                            # Test classes
│   ├── pom.xml                              # Maven dependencies
│   └── Dockerfile                           # Docker configuration
├── database/
│   └── schema.sql                           # PostgreSQL DDL
├── frontend/                                # Angular application (TBD)
└── README.md

```

## 🚀 Getting Started

### Prerequisites

Before running the application, ensure you have:

- **Java Development Kit (JDK)**: 1.8 or higher
- **Maven**: 3.6+ for building the project
- **PostgreSQL**: 12+ database server
- **Redis**: 6+ for session management
- **Node.js & npm**: 16+ for Angular frontend (when implemented)

### Installation Steps

#### 1. Clone the Repository

```bash
git clone <repository-url>
cd finance_tracker
```

#### 2. Set Up PostgreSQL Database

```bash
# Create database
createdb expense_tracker_db

# Run schema script
psql -U postgres -d expense_tracker_db -f database/schema.sql
```

#### 3. Set Up Redis

```bash
# Install Redis (Ubuntu/Debian)
sudo apt-get install redis-server

# Start Redis
redis-server

# Or using Docker
docker run -d -p 6379:6379 redis:latest
```

#### 4. Configure Application

Edit `backend/src/main/resources/application.yml` and update:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/expense_tracker_db
    username: your_db_username
    password: your_db_password
  
  redis:
    host: localhost
    port: 6379
    password: # Leave empty if no password
  
  mail:
    host: smtp.gmail.com
    username: your-email@gmail.com
    password: your-app-password
```

#### 5. Build the Application

```bash
cd backend
mvn clean install
```

#### 6. Run the Application

```bash
# Development mode
mvn spring-boot:run

# Or run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Production mode
java -jar target/expense-tracker-1.0.0.jar --spring.profiles.active=prod
```

The application will start on `http://localhost:8080/api`

## 🗄 Database Setup

### Complete Database Schema

The complete PostgreSQL schema is available in `database/schema.sql`. It includes:

- **Tables**: 13 core tables with proper indexes and constraints
- **Views**: Pre-built views for analytics (monthly summary, category spending, budget status)
- **Triggers**: Auto-update triggers for timestamps and budget calculations
- **Functions**: Helper functions for budget tracking
- **Default Data**: Pre-populated roles and default categories

### Key Tables

| Table | Description |
|-------|-------------|
| `users` | User accounts and authentication |
| `roles` | System roles (USER, ADMIN) |
| `user_roles` | Many-to-many user-role mapping |
| `categories` | Income/expense categories |
| `transactions` | Financial transactions |
| `recurring_transactions` | Recurring payment templates |
| `budgets` | Monthly budget allocations |
| `budget_alerts` | Budget notification history |
| `files` | Uploaded receipts/invoices |
| `notifications` | User notifications |
| `audit_log` | Complete audit trail |
| `insights` | AI-generated spending insights |
| `user_preferences` | User settings and preferences |

## ⚙ Configuration

### Application Profiles

The application supports multiple profiles:

- **dev**: Development environment (relaxed security, verbose logging)
- **prod**: Production environment (strict security, optimized performance)

### Key Configuration Properties

```yaml
# Server Configuration
server.port: 8080
server.servlet.context-path: /api

# Session Management
server.servlet.session.timeout: 30m
spring.session.store-type: redis

# File Upload
spring.servlet.multipart.max-file-size: 10MB
app.file-storage.upload-dir: ./uploads/receipts

# Budget Alerts
app.budget.default-alert-threshold: 80
app.budget.check-interval-cron: "0 0 * * * ?"  # Every hour

# Email Settings
app.email.enabled: true
app.email.from: noreply@expensetracker.com
```

## 📚 API Documentation

### Swagger/OpenAPI

Once the application is running, access interactive API documentation at:

**Swagger UI**: `http://localhost:8080/api/swagger-ui.html`  
**OpenAPI JSON**: `http://localhost:8080/api/api-docs`

### Main API Endpoints (To be implemented)

#### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `GET /api/auth/session` - Get current session

#### Transactions
- `GET /api/transactions` - List all transactions (paginated)
- `GET /api/transactions/{id}` - Get transaction by ID
- `POST /api/transactions` - Create new transaction
- `PUT /api/transactions/{id}` - Update transaction
- `DELETE /api/transactions/{id}` - Delete transaction
- `GET /api/transactions/export/csv` - Export to CSV
- `GET /api/transactions/export/pdf` - Export to PDF

#### Categories
- `GET /api/categories` - List all categories
- `GET /api/categories/{id}` - Get category by ID
- `POST /api/categories` - Create new category
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category

#### Budgets
- `GET /api/budgets` - List all budgets
- `GET /api/budgets/{id}` - Get budget by ID
- `POST /api/budgets` - Create new budget
- `PUT /api/budgets/{id}` - Update budget
- `DELETE /api/budgets/{id}` - Delete budget
- `GET /api/budgets/status` - Get budget status overview

#### Analytics
- `GET /api/analytics/monthly-summary` - Income vs Expense by month
- `GET /api/analytics/category-breakdown` - Spending by category
- `GET /api/analytics/trends` - Spending trends
- `GET /api/analytics/savings` - Savings calculation

#### Notifications
- `GET /api/notifications` - List all notifications
- `PUT /api/notifications/{id}/read` - Mark as read
- `DELETE /api/notifications/{id}` - Delete notification

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run with coverage report
mvn clean test jacoco:report
```

### Test Structure

- **Unit Tests**: Service layer and utility classes
- **Integration Tests**: Repository layer with H2 database
- **Controller Tests**: REST API endpoints with MockMvc

## 🐳 Deployment

### Docker Deployment

```bash
# Build Docker image
docker build -t expense-tracker:latest ./backend

# Run with Docker Compose
docker-compose up -d
```

### Manual Deployment

```bash
# Build production JAR
mvn clean package -Pprod

# Run on server
java -jar target/expense-tracker-1.0.0.jar \
  --spring.profiles.active=prod \
  --server.port=8080
```

## 📊 Current Progress

### ✅ Completed
1. ✅ Database schema (PostgreSQL DDL)
2. ✅ Spring Boot project structure
3. ✅ Maven configuration with all dependencies
4. ✅ Entity classes with JPA annotations
5. ✅ Enum types for all domain models
6. ✅ Application configuration files (dev, prod)
7. ✅ Main application class

### 🔄 In Progress
- Repository interfaces
- DTO classes and validation
- Service layer implementation
- REST controllers
- Spring Security configuration
- Spring Session + Redis configuration
- Swagger/OpenAPI configuration
- Audit logging implementation
- Email integration
- Exception handling
- Unit tests
- Dockerfile and CI pipeline

### 📝 Pending
- Angular 18 frontend
- UI components and services
- Charts and data visualization
- End-to-end testing
- Production deployment

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

- Finance Tracker Team

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- PostgreSQL community
- Angular team
- All open-source contributors

---

**Version**: 1.0.0  
**Last Updated**: February 2026  
**Status**: In Development
