# Expense Tracker — Personal Expense Tracker with Categories

A modern, production-ready Java Spring Boot REST API for personal and student expense tracking with categories, monthly category budgets, automated 90% and 100% budget alerts, and month-over-month spending trend analytics.

---

## Table of Contents

1. [Project Title](#1-project-title)
2. [Project Description](#2-project-description)
3. [Real-World Scenario](#3-real-world-scenario)
4. [Features](#4-features)
5. [Technology Stack](#5-technology-stack)
6. [Prerequisites](#6-prerequisites)
7. [Java Installation Requirement](#7-java-installation-requirement)
8. [Maven Requirement](#8-maven-requirement)
9. [MySQL Installation and Setup](#9-mysql-installation-and-setup)
10. [Database Creation](#10-database-creation)
11. [Configuration](#11-configuration)
12. [How to Run the Project](#12-how-to-run-the-project)
13. [Project Structure](#13-project-structure)
14. [Entity Relationships](#14-entity-relationships)
15. [Business Rules](#15-business-rules)
16. [API Endpoint List](#16-api-endpoint-list)
17. [Example Requests](#17-example-requests)
18. [Example Responses](#18-example-responses)
19. [Validation Errors](#19-validation-errors)
20. [Budget Alert Explanation (90% & 100%)](#20-budget-alert-explanation-90--100)
21. [Monthly Summary Explanation](#21-monthly-summary-explanation)
22. [Month-over-Month Trend Explanation](#22-month-over-month-trend-explanation)
23. [Dashboard Explanation](#23-dashboard-explanation)
24. [Filtering Explanation](#24-filtering-explanation)
25. [Postman Testing](#25-postman-testing)
26. [Swagger Testing (OpenAPI 3)](#26-swagger-testing-openapi-3)
27. [Automated Tests](#27-automated-tests)
28. [Common Errors and Solutions](#28-common-errors-and-solutions)

---

## 1. Project Title
**Expense Tracker — Personal Expense Tracker with Categories**

---

## 2. Project Description
The **Expense Tracker API** is a robust Spring Boot 3 RESTful backend application designed to help individuals and college students take full control of their financial life. It tracks expenses categorized under heads such as Food, Travel, and Stationery, manages monthly limits per category, calculates live spending totals directly in MySQL, and triggers intelligent alerts before spending limits are breached.

---

## 3. Real-World Scenario
Students and young professionals often receive a monthly allowance or stipend. Without active tracking, they frequently exhaust their funds well before month-end due to unmonitored micro-spending on canteen food, cab rides, subscriptions, or stationary. 

This application solves that exact problem by:
- Organizing spending into customized categories (**Food**, **Travel**, **Stationery**, **Entertainment**, etc.).
- Allowing students to define realistic monthly spending ceilings for each category.
- Recalculating spending immediately on every logged expense.
- Sending a prominent **90% Warning** when approaching the limit, and an **Exceeded Alert** when over budget, while still recording the expense.
- Providing **Month-over-Month Trends** so students can see their spending patterns develop over time.

---

## 4. Features
- **User Management**: Register and manage user profiles with unique email validation.
- **Dynamic Category Management**: Create and search custom expense categories.
- **Monthly Budgeting**: Set monthly spending limits per category per user in `YYYY-MM` format.
- **Budget Duplicate Prevention**: Enforces business and database constraints preventing duplicate budgets for the same user, category, and month.
- **Instant 90% Budget Alert**: Immediately recalculates total monthly category spend upon logging any new expense and warns when spending reaches or exceeds 90% of the budget.
- **100% Budget Exceeded Alert**: Flags when expenses cross 100% of the category budget without blocking the valid transaction.
- **Current Month Category Summaries**: Live breakdown of spending, budget limits, remaining balance, and percentage used.
- **Month-over-Month Spending Trends**: Database-driven aggregation grouping expenses across recorded months.
- **Global Search & Filter**: Filter expenses by user, category, and custom date ranges (`startDate` / `endDate`).
- **High-Level Dashboard**: Real-time totals of users, categories, expenses, budgets, and all-time spending.
- **Automated Sample Data**: Pre-seeded demo users (John, Alex), categories, budgets, and expenses for zero-friction testing.
- **Interactive Swagger Documentation**: Built-in OpenAPI 3 UI for testing every endpoint directly in the browser.

---

## 5. Technology Stack
- **Language**: Java 21 LTS
- **Framework**: Spring Boot 3.3.4
- **Web**: Spring Web (Spring MVC REST)
- **Persistence**: Spring Data JPA, Hibernate 6.5
- **Database**: MySQL 8.0 (Production / Dev) & H2 (In-Memory for unit/integration tests)
- **Validation**: Jakarta Bean Validation (`spring-boot-starter-validation`)
- **Documentation**: Springdoc OpenAPI / Swagger UI (`springdoc-openapi-starter-webmvc-ui` 2.6.0)
- **Build Tool**: Apache Maven 3.9+ (includes Maven Wrapper `mvnw` / `mvnw.cmd`)
- **Testing**: JUnit 5, Mockito, Spring Boot Test (`MockMvc`)

---

## 6. Prerequisites
Before running the application, ensure the following are available on your machine:
- **Operating System**: Windows, macOS, or Linux
- **Java Development Kit (JDK)**: JDK 21 (or compatible JDK 17+)
- **Maven**: Version 3.8+ (or use the included `mvnw.cmd` wrapper)
- **MySQL Server**: Version 8.0+ running locally on port `3306`

---

## 7. Java Installation Requirement
Verify your Java version:
```bash
java -version
```
Expected output should indicate version 21 or higher:
```
java version "21.0.12" 2026-07-21 LTS
Java(TM) SE Runtime Environment (build 21.0.12+7-LTS-205)
```
If not installed, download and install OpenJDK 21 or Oracle JDK 21 from [https://adoptium.net/](https://adoptium.net/) or [https://www.oracle.com/java/technologies/downloads/](https://www.oracle.com/java/technologies/downloads/). Ensure `JAVA_HOME` is added to your environment path.

---

## 8. Maven Requirement
Verify Maven is installed:
```bash
mvn -version
```
If Maven is not installed globally, you can execute all commands using the bundled wrapper:
- **Windows**: `.\mvnw.cmd <command>`
- **macOS/Linux**: `./mvnw <command>`

---

## 9. MySQL Installation and Setup
1. Ensure MySQL Server 8.0 is running.
   - On Windows (PowerShell as Admin):
     ```powershell
     Start-Service MySQL80
     ```
   - On Linux/macOS:
     ```bash
     sudo systemctl start mysql
     ```
2. Verify MySQL connectivity on default port `3306`:
   ```bash
   mysql -u root -p
   ```

---

## 10. Database Creation
Execute the provided script [setup-database.sql](file:///c:/Users/mithil%20pranav/Downloads/JAVA%20SPRINGBOOT%20expensejar/setup-database.sql) or run the SQL command below in MySQL Workbench or the `mysql` CLI:

```sql
CREATE DATABASE IF NOT EXISTS expense_tracker
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

---

## 11. Configuration
Database and server configurations are defined in `src/main/resources/application.properties`:

```properties
# Server Port
server.port=8080

# MySQL Connection (Reads environment variables or falls back to standard defaults)
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/expense_tracker?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:root}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:root}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate Auto Schema Update
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Swagger OpenAPI Path
springdoc.swagger-ui.path=/swagger-ui.html
```

> **Tip**: You can override your MySQL username and password at runtime without modifying code:
> ```powershell
> $env:SPRING_DATASOURCE_USERNAME="your_username"
> $env:SPRING_DATASOURCE_PASSWORD="your_password"
> ```

---

## 12. How to Run the Project

### Option A: Using the Maven Wrapper (Recommended)
**Windows**:
```powershell
.\mvnw.cmd spring-boot:run
```

**macOS / Linux**:
```bash
./mvnw spring-boot:run
```

### Option B: Using Installed Maven
```bash
mvn spring-boot:run
```

### Option C: Build and Run the Packaged JAR
```bash
mvn clean package -DskipTests
java -jar target/expense-tracker-0.0.1-SNAPSHOT.jar
```

Once started, open:
- **API Base URL**: `http://localhost:8080`
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **API Docs (JSON)**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 13. Project Structure
```
expense-tracker/
├── pom.xml                                      # Maven dependencies & build configuration
├── mvnw / mvnw.cmd                              # Maven wrappers
├── setup-database.sql                           # MySQL database creation script
├── expense-tracker.postman_collection.json      # Postman test collection
├── README.md                                    # Comprehensive system documentation
└── src/
    ├── main/
    │   ├── java/com/example/expensetracker/
    │   │   ├── ExpenseTrackerApplication.java  # Spring Boot main class
    │   │   ├── config/
    │   │   │   ├── DataInitializer.java        # Safe sample data seeder
    │   │   │   └── OpenApiConfig.java          # OpenAPI / Swagger 3 config
    │   │   ├── controller/
    │   │   │   ├── UserController.java         # User endpoints
    │   │   │   ├── CategoryController.java     # Category endpoints
    │   │   │   ├── ExpenseController.java      # Expense, summary & trend endpoints
    │   │   │   ├── BudgetController.java       # Budget & status endpoints
    │   │   │   └── DashboardController.java    # Dashboard metrics endpoint
    │   │   ├── dto/
    │   │   │   ├── UserRequest.java / UserResponse.java
    │   │   │   ├── CategoryRequest.java / CategoryResponse.java
    │   │   │   ├── ExpenseRequest.java / ExpenseResponse.java
    │   │   │   ├── BudgetRequest.java / BudgetResponse.java
    │   │   │   ├── BudgetAlertDto.java
    │   │   │   ├── BudgetStatusResponse.java
    │   │   │   ├── ExpenseSummaryResponse.java
    │   │   │   ├── SpendingTrendResponse.java / MonthlySpendingItem.java
    │   │   │   ├── DashboardResponse.java
    │   │   │   └── ErrorResponse.java
    │   │   ├── entity/
    │   │   │   ├── User.java                   # User entity
    │   │   │   ├── Category.java               # Category entity
    │   │   │   ├── Expense.java                # Expense entity
    │   │   │   └── Budget.java                 # Budget entity
    │   │   ├── exception/
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   ├── DuplicateResourceException.java
    │   │   │   ├── InvalidExpenseException.java
    │   │   │   ├── InvalidBudgetException.java
    │   │   │   └── GlobalExceptionHandler.java # @RestControllerAdvice
    │   │   ├── repository/
    │   │   │   ├── UserRepository.java
    │   │   │   ├── CategoryRepository.java
    │   │   │   ├── ExpenseRepository.java
    │   │   │   ├── BudgetRepository.java
    │   │   │   └── projection/
    │   │   │       ├── CategorySpendingProjection.java
    │   │   │       └── MonthlySpendingProjection.java
    │   │   └── service/
    │   │       ├── UserService.java
    │   │       ├── CategoryService.java
    │   │       ├── ExpenseService.java
    │   │       ├── BudgetService.java
    │   │       └── ExpenseReportService.java
    │   └── resources/
    │       └── application.properties           # MySQL config & app settings
    └── test/
        ├── java/com/example/expensetracker/
        │   ├── ExpenseTrackerIntegrationTest.java # Full 35-case integration test suite
        │   └── service/
        │       ├── ExpenseServiceTest.java       # 11 unit tests (90%/100% alert thresholds)
        │       └── BudgetServiceTest.java        # 7 unit tests (budgets & calculations)
        └── resources/
            └── application.properties           # H2 in-memory test configuration
```

---

## 14. Entity Relationships

```
        ┌─────────────────────────┐
        │          User           │
        │ ─────────────────────── │
        │ id: Long (PK)           │
        │ name: String            │
        │ email: String (Unique)  │
        └────────────┬────────────┘
                     │ 1
                     │
         ┌───────────┴───────────┐
         │                       │
       * │                     * │
┌────────▼────────┐     ┌────────▼────────┐
│     Expense     │     │     Budget      │
│ ─────────────── │     │ ─────────────── │
│ id: Long (PK)   │     │ id: Long (PK)   │
│ amount: Dec(12) │     │ amount: Dec(12) │
│ date: LocalDate │     │ month: YYYY-MM  │
│ desc: String    │     │                 │
└────────▲────────┘     └────────▲────────┘
       * │                     * │
         │                       │
         └───────────┬───────────┘
                     │ 1
        ┌────────────┴────────────┐
        │        Category         │
        │ ─────────────────────── │
        │ id: Long (PK)           │
        │ name: String (Unique)   │
        │ description: String     │
        └─────────────────────────┘
```

- **User**: Has many **Expenses** (`1:N`) and has many **Budgets** (`1:N`).
- **Category**: Has many **Expenses** (`1:N`) and has many **Budgets** across months (`1:N`).
- **Expense**: Belongs to one **User** (`N:1`) and one **Category** (`N:1`).
- **Budget**: Belongs to one **User** (`N:1`) and one **Category** (`N:1`). Unique constraint on `(user_id, category_id, budget_month)`.

---

## 15. Business Rules
1. **Positive Expense Amount**: Every expense amount must strictly be greater than zero (`amount > 0`). Zero or negative amounts are rejected immediately.
2. **Positive Budget Amount**: Every budget limit must strictly be greater than zero (`amount > 0`).
3. **Immediate Alert Recalculation**: Budget status and spending percentage must be recalculated immediately when a new expense is logged.
4. **Pre-Save Validation**: Input constraints and business checks are validated **before** attempting to save invalid records to the database.
5. **Fast Fail on Invalid Input**: Malformed dates, blank names, and invalid emails are rejected at the request boundary with HTTP 400.
6. **Clean Error Responses**: No Java stack traces are exposed to clients. Clear, structured JSON error objects are returned.
7. **User Existence**: A user must exist in the database before expenses or budgets can be linked to that user.
8. **Category Existence**: A category must exist in the database before expenses or budgets can be linked to that category.
9. **No Duplicate Budgets**: A user cannot define multiple budgets for the same category in the same month. Duplicate attempts return HTTP 409 Conflict.
10. **Database-Driven Calculations**: Spending totals and monthly trends are computed directly using SQL database aggregation queries, avoiding memory-heavy in-memory loops.

---

## 16. API Endpoint List

| Component | Method | Endpoint | Description | Status Code |
|---|---|---|---|---|
| **User** | `POST` | `/api/users` | Register a new user | `201 Created` |
| **User** | `GET` | `/api/users` | List all users | `200 OK` |
| **User** | `GET` | `/api/users/{id}` | Get user by ID | `200 OK` |
| **Category** | `POST` | `/api/categories` | Create a spending category | `201 Created` |
| **Category** | `GET` | `/api/categories` | List categories (`?name=Food`) | `200 OK` |
| **Category** | `GET` | `/api/categories/{id}` | Get category by ID | `200 OK` |
| **Expense** | `POST` | `/api/expenses` | Log an expense (checks 90% alert) | `201 Created` |
| **Expense** | `GET` | `/api/expenses` | List/Filter expenses | `200 OK` |
| **Expense** | `GET` | `/api/expenses/{id}` | Get expense by ID | `200 OK` |
| **Expense** | `PUT` | `/api/expenses/{id}` | Update expense | `200 OK` |
| **Expense** | `DELETE`| `/api/expenses/{id}` | Delete expense | `204 No Content` |
| **Budget** | `POST` | `/api/budgets` | Set category monthly budget | `201 Created` |
| **Budget** | `GET` | `/api/budgets` | List budgets (`?userId=1&month=2026-09`) | `200 OK` |
| **Budget** | `GET` | `/api/budgets/{id}` | Get budget by ID | `200 OK` |
| **Budget** | `PUT` | `/api/budgets/{id}` | Update budget limit | `200 OK` |
| **Budget** | `DELETE`| `/api/budgets/{id}` | Delete budget | `204 No Content` |
| **Budget** | `GET` | `/api/budgets/status` | Check budget status & alert | `200 OK` |
| **Reporting**| `GET` | `/api/expenses/summary/current` | Current month category summaries | `200 OK` |
| **Reporting**| `GET` | `/api/expenses/summary` | Specific category & month summary | `200 OK` |
| **Reporting**| `GET` | `/api/expenses/trend` | Month-over-month spending trend | `200 OK` |
| **Dashboard**| `GET` | `/api/dashboard` | Aggregate counts and totals | `200 OK` |

---

## 17. Example Requests

### 1. Add User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "John", "email": "john@example.com"}'
```

### 2. Create Category
```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name": "Food", "description": "Groceries, cafeteria, and canteen meals"}'
```

### 3. Set Monthly Budget
```bash
curl -X POST http://localhost:8080/api/budgets \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "categoryId": 1, "amount": 5000.00, "month": "2026-09"}'
```

### 4. Log an Expense
```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "categoryId": 1, "amount": 500.00, "date": "2026-09-28", "description": "Lunch"}'
```

### 5. Get Current Month Category Summary
```bash
curl -X GET "http://localhost:8080/api/expenses/summary/current?userId=1"
```

### 6. Get Month-over-Month Spending Trend
```bash
curl -X GET "http://localhost:8080/api/expenses/trend?userId=1"
```

### 7. Filter Expenses by User and Category
```bash
curl -X GET "http://localhost:8080/api/expenses?userId=1&categoryId=1"
```

### 8. Get System Dashboard
```bash
curl -X GET "http://localhost:8080/api/dashboard"
```

---

## 18. Example Responses

### Expense Created (Triggering 90% Budget Alert)
```json
{
  "id": 12,
  "amount": 400.00,
  "date": "2026-09-28",
  "description": "Department store dinner",
  "userId": 1,
  "userName": "John",
  "categoryId": 1,
  "categoryName": "Food",
  "budgetAlert": {
    "hasBudget": true,
    "budgetAmount": 5000.00,
    "totalSpent": 4600.00,
    "remainingBudget": 400.00,
    "percentageUsed": 92.0,
    "alertLevel": "WARNING",
    "warning": true,
    "message": "Warning: Food spending has reached 92.0% of the monthly budget."
  },
  "warningMessage": "Warning: Food spending has reached 92.0% of the monthly budget."
}
```

### Current Month Category Spending Summary (`GET /api/expenses/summary/current?userId=1`)
```json
[
  {
    "categoryId": 1,
    "category": "Food",
    "month": "2026-09",
    "totalSpent": 4200.00,
    "budget": 5000.00,
    "remaining": 800.00,
    "percentageUsed": 84.0,
    "status": "NORMAL"
  },
  {
    "categoryId": 2,
    "category": "Travel",
    "month": "2026-09",
    "totalSpent": 2800.00,
    "budget": 3000.00,
    "remaining": 200.00,
    "percentageUsed": 93.3,
    "status": "WARNING"
  },
  {
    "categoryId": 3,
    "category": "Stationery",
    "month": "2026-09",
    "totalSpent": 700.00,
    "budget": 1500.00,
    "remaining": 800.00,
    "percentageUsed": 46.7,
    "status": "NORMAL"
  }
]
```

### Month-over-Month Spending Trend (`GET /api/expenses/trend?userId=1`)
```json
{
  "userId": 1,
  "userName": "John",
  "monthlySpending": [
    {
      "month": "2026-07",
      "total": 4200.00
    },
    {
      "month": "2026-08",
      "total": 5100.00
    },
    {
      "month": "2026-09",
      "total": 7700.00
    }
  ]
}
```

### System Dashboard (`GET /api/dashboard`)
```json
{
  "totalUsers": 2,
  "totalCategories": 6,
  "totalExpenses": 11,
  "totalBudgets": 4,
  "totalSpendingAllTime": 17000.00
}
```

---

## 19. Validation Errors
When an invalid request is supplied, the API returns a standard `400 Bad Request` or `409 Conflict` response with clear field details:

### Zero / Negative Expense Amount
```json
{
  "timestamp": "2026-09-28T10:29:15.123",
  "status": 400,
  "error": "Validation Failed",
  "message": "Expense amount must be greater than zero",
  "path": "/api/expenses",
  "fieldErrors": {
    "amount": "Expense amount must be greater than zero"
  }
}
```

### Duplicate Monthly Budget (409 Conflict)
```json
{
  "timestamp": "2026-09-28T10:29:16.456",
  "status": 409,
  "error": "Duplicate Resource",
  "message": "A budget already exists for user 'John' in category 'Food' for month 2026-09",
  "path": "/api/budgets"
}
```

### Non-Existing User / Category (404 Not Found)
```json
{
  "timestamp": "2026-09-28T10:29:17.789",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 999",
  "path": "/api/expenses"
}
```

---

## 20. Budget Alert Explanation (90% & 100%)
The system automatically monitors category spending against the configured budget for that month:

$$\text{Percentage Used} = \left(\frac{\text{Total Spent in Category for Month}}{\text{Category Monthly Budget}}\right) \times 100$$

1. **Below 90% (`alertLevel = "NONE"`)**:
   - `warning = false`
   - Normal operation, expenses recorded seamlessly.
2. **At or Above 90% and Below 100% (`alertLevel = "WARNING"`)**:
   - `warning = true`
   - Returns message: `"Warning: Food spending has reached 92.0% of the monthly budget."`
   - Alert is included in the response body so frontend clients or mobile apps can display a banner or push notification.
3. **At or Above 100% (`alertLevel = "EXCEEDED"`)**:
   - `warning = true`
   - Returns message: `"Budget exceeded. Food spending has reached 104.0% of the monthly budget."`
   - **Crucial Rule**: The expense is **still saved** in the database. Valid financial records are never lost or dropped.

---

## 21. Monthly Summary Explanation
- **Endpoint**: `GET /api/expenses/summary/current?userId=1`
- Fetches all expenses for the calling user within the current month (`LocalDate.now()`).
- Calculates category totals using high-performance SQL aggregation.
- Reconciles against each category's budget and produces remaining balance and percentage utilized.
- Marks each category status: `NORMAL`, `WARNING`, `EXCEEDED`, or `NO_BUDGET`.

---

## 22. Month-over-Month Trend Explanation
- **Endpoint**: `GET /api/expenses/trend?userId=1`
- Uses database grouping on `YEAR(date)` and `MONTH(date)` to group and order historical expenditure.
- Delivers an array of `{ month: "YYYY-MM", total: amount }`.
- Also supports category-scoped trends:
  ```bash
  GET /api/expenses/trend?userId=1&categoryId=1
  ```

---

## 23. Dashboard Explanation
- **Endpoint**: `GET /api/dashboard`
- Delivers system-wide aggregate statistics:
  - `totalUsers`: Active registered users
  - `totalCategories`: Active spending categories
  - `totalExpenses`: Cumulative recorded expense count
  - `totalBudgets`: Total budgets configured across all months
  - `totalSpendingAllTime`: Total cumulative money tracked

---

## 24. Filtering Explanation
List endpoints support intuitive query parameters:
- **Filter expenses by user**: `GET /api/expenses?userId=1`
- **Filter expenses by category**: `GET /api/expenses?categoryId=2`
- **Filter expenses by user and category**: `GET /api/expenses?userId=1&categoryId=2`
- **Filter expenses by date range**: `GET /api/expenses?startDate=2026-09-01&endDate=2026-09-30`
- **Search categories by name**: `GET /api/categories?name=Food`
- **Filter budgets by user and month**: `GET /api/budgets?userId=1&month=2026-09`

---

## 25. Postman Testing
The project includes a ready-to-import Postman collection:
`expense-tracker.postman_collection.json`

### How to use:
1. Open **Postman**.
2. Click **Import** (top left).
3. Drag and drop `expense-tracker.postman_collection.json` from the project root.
4. All organized folders (**Users**, **Categories**, **Budgets**, **Expenses**, **Reporting & Trends**, **Edge Cases & Validation**) will be ready to test with pre-configured requests.

---

## 26. Swagger Testing (OpenAPI 3)
1. Start the application (`mvn spring-boot:run`).
2. Open your web browser and go to:
   [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
3. You can click **"Try it out"** on any endpoint, execute requests directly, and view real-time responses.

---

## 27. Automated Tests
The project features **53 automated tests** covering normal execution, business edge cases, and threshold validations using JUnit 5, Mockito, and Spring Boot MockMvc:

```bash
# Run all automated tests
mvn clean test
```

### Coverage Highlights:
- **Unit Tests (`BudgetServiceTest`, `ExpenseServiceTest`)**:
  - Positive amounts validation (rejections on `<= 0`).
  - Recalculation logic and threshold checks:
    - `< 90%` -> `NONE`
    - `== 90%` -> `WARNING`
    - `> 90%` -> `WARNING`
    - `== 100%` -> `EXCEEDED`
    - `> 100%` -> `EXCEEDED` (and still saved)
  - Duplicate budget prevention.
- **End-to-End Integration Suite (`ExpenseTrackerIntegrationTest`)**:
  - Full CRUD lifecycle across User, Category, Expense, and Budget.
  - Current month category spending summary calculations.
  - Month-over-month trend calculations.
  - Missing field validations and malformed payloads.
  - Search and filter queries.
  - System dashboard metrics.

---

## 28. Common Errors and Solutions

### 1. `Access denied for user 'root'@'localhost'`
- **Cause**: Your local MySQL root password differs from the default `root`.
- **Solution**: Set the password via environment variable:
  ```powershell
  $env:SPRING_DATASOURCE_PASSWORD="your_actual_password"
  mvn spring-boot:run
  ```

### 2. `Communications link failure / Connection refused`
- **Cause**: MySQL server is not running on port `3306`.
- **Solution**: Start MySQL:
  ```powershell
  Start-Service MySQL80
  ```

### 3. `Port 8080 is already in use`
- **Cause**: Another service is using port 8080.
- **Solution**: Override the port in `application.properties` or launch with:
  ```powershell
  .\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--server.port=8085"
  ```

### 4. `DuplicateResourceException: A budget already exists...`
- **Cause**: Attempting to set two budgets for the same user, category, and month.
- **Solution**: Use `PUT /api/budgets/{id}` to update the existing budget amount instead of creating a duplicate.
