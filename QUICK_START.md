# 🚀 Smart Expense Tracker - Quick Start Guide

## Prerequisites Setup

The application requires:
1. PostgreSQL Database
2. Redis (for session management)
3. Java 8+

### Option 1: Quick Start with Docker (Recommended)

#### Install Docker
- Download from [docker.com](https://www.docker.com/products/docker-desktop)

#### Start PostgreSQL & Redis with Docker Compose

Create a `docker-compose.yml` file in the project root:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:13-alpine
    container_name: finance-tracker-postgres
    environment:
      POSTGRES_DB: expense_tracker_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/schema.sql:/docker-entrypoint-initdb.d/schema.sql
    networks:
      - finance-tracker-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: finance-tracker-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - finance-tracker-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
  redis_data:

networks:
  finance-tracker-network:
    driver: bridge
```

#### Run Docker Compose

```bash
# Navigate to project root
cd d:\programs\finance_tracker

# Start services
docker-compose up -d

# View logs
docker-compose logs -f postgres redis

# Stop services
docker-compose down
```

---

### Option 2: Manual Setup (Windows)

#### Step 1: Install PostgreSQL

1. Download from [postgresql.org](https://www.postgresql.org/download/windows/)
2. Run installer (use default password `postgres` or remember your password)
3. Select default options
4. **Port: 5432**

#### Step 2: Create Database

```bash
# Open Command Prompt or PowerShell
psql -U postgres

# In psql prompt, run:
CREATE DATABASE expense_tracker_db;

# Verify
\l

# Exit
\q
```

#### Step 3: Run Schema

```bash
# From project root
psql -U postgres -d expense_tracker_db -f database/schema.sql
```

#### Step 4: Install Redis

1. Download **Redis Windows** from [microsoftarchive](https://github.com/microsoftarchive/redis/releases)
2. Extract and run `redis-server.exe`
3. Redis will start on `localhost:6379`

---

## 🎯 Starting the Application

### Method 1: From IDE (VS Code)

```bash
# In terminal, navigate to backend folder
cd backend

# Build and run
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Method 2: From JAR

```bash
# Navigate to backend
cd backend

# Build
mvn clean install -DskipTests

# Run
java -jar target/expense-tracker-1.0.0.jar --spring.profiles.active=dev
```

### Method 3: Docker Container

```bash
# Build Docker image (from project root)
docker build -t expense-tracker:latest ./backend

# Run container (make sure PostgreSQL and Redis are running)
docker run -p 8080:8080 \
  --network finance-tracker-network \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/expense_tracker_db \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  -e SPRING_REDIS_HOST=redis \
  -e SPRING_REDIS_PORT=6379 \
  expense-tracker:latest
```

---

## ✅ Verify Application is Running

Once started, you should see:
```
...
2026-02-11 12:00:00 - Tomcat started on port(s): 8080 (http)
2026-02-11 12:00:00 - Started ExpenseTrackerApplication in X.XXX seconds
```

### Test API Health

```bash
# Check if app is running
curl http://localhost:8080/api/actuator/health

# Should respond with:
{"status":"UP"}
```

### Access Swagger UI

Open browser and navigate to:
```
http://localhost:8080/api/swagger-ui.html
```

---

## 🔧 Troubleshooting

### Error: "password authentication failed for user postgres"

**Solution**: Update `application-dev.yml` with correct credentials:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/expense_tracker_db
    username: postgres           # Your PostgreSQL username
    password: postgres           # Your PostgreSQL password
```

### Error: "Connection to Redis refused"

**Solution**: 
1. Verify Redis is running: `redis-cli ping` (should return `PONG`)
2. Check Redis on localhost:6379
3. If not installed, follow Option 1 (Docker) or install Redis manually

### Error: "Database does not exist"

**Solution**: Create the database first:
```bash
psql -U postgres -c "CREATE DATABASE expense_tracker_db;"
psql -U postgres -d expense_tracker_db -f database/schema.sql
```

### Port Already in Use

If port 8080 is in use, change in `application-dev.yml`:
```yaml
server:
  port: 8081  # or any available port
```

---

## 📚 API Documentation

Once running, access interactive API docs at:

**Swagger UI**: http://localhost:8080/api/swagger-ui.html  
**OpenAPI JSON**: http://localhost:8080/api/api-docs

---

## 🧪 Testing the API

### Login Example
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

### Get Transactions
```bash
curl -X GET http://localhost:8080/api/transactions \
  -H "Cookie: EXPENSE_TRACKER_SESSION=<session-id>"
```

---

## 📁 Environment Variables

For production, set environment variables:

```bash
# Database
set DATABASE_URL=jdbc:postgresql://localhost:5432/expense_tracker_db
set DATABASE_USERNAME=postgres
set DATABASE_PASSWORD=postgres

# Redis
set REDIS_HOST=localhost
set REDIS_PORT=6379
set REDIS_PASSWORD=

# Email (optional)
set SMTP_HOST=smtp.gmail.com
set SMTP_PORT=587
set SMTP_USERNAME=your-email@gmail.com
set SMTP_PASSWORD=your-app-password
set EMAIL_FROM=noreply@expensetracker.com
```

---

## 🚀 Next Steps

1. ✅ **Build & Start** - Complete!
2. 📝 **Create Repository Layer** - In progress
3. 🎯 **Create DTO Layer** - Pending
4. 🔧 **Create Service Layer** - Pending
5. 🌐 **Create Controllers** - Pending
6. 🔐 **Configure Security** - Pending
7. 📊 **Create Frontend** - Pending

---

## 📞 Support

For issues:
1. Check logs in `logs/expense-tracker.log`
2. Verify database connection
3. Check Redis connectivity
4. Review application.yml configuration

---

**Version**: 1.0.0  
**Last Updated**: Feb 2026
