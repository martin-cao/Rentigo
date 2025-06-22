# Rentigo

A modern car rental platform built with Spring Boot and Vue.js.

## Overview

Rentigo is a full-featured car rental management system that allows users to:
- Browse and rent vehicles
- Process payments with Stripe
- Track rental status
- Manage deposits and overtime fees
- Handle user authentication and authorization

## Technology Stack

### Backend
- Java 21
- Spring Boot 3.x
- MySQL 8.0
- Stripe Payment API
- JWT Authentication

### Frontend
- Vue.js 3
- Vite
- TypeScript
- Tailwind CSS

## How to Build

### Prerequisites

1. JDK 21 or above
2. Maven 3.8+
3. MySQL 8.0
4. Node.js 16+
5. Stripe account and API keys

### Database Setup

1. Install MySQL 8.0
2. Create a new database:
```sql
CREATE DATABASE rentigo;
```
then run `init_db.sql`
3. Update database configuration in `backend/rentigo-backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/rentigo
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Stripe Configuration

1. Sign up for a Stripe account at https://stripe.com
2. Get your API keys from the Stripe Dashboard
3. Configure Stripe keys in `application.properties`:
```properties
stripe.secret-key=your_stripe_secret_key
stripe.webhook.secret=your_stripe_webhook_secret
```

> [!WARNING]
> Please create a Stripe Sandbox for testing first. DO NOT directly use your production environment!

### Building the Backend

1. Navigate to the backend directory:
```bash
cd backend/rentigo-backend
```

2. Build with Maven:
```bash
./mvnw clean package
```

3. Run the application:
```bash
./mvnw spring-boot:run
```

The backend API will be available at `http://localhost:8080`

### Building the Frontend

1. Navigate to the frontend directory:
```bash
cd frontend/rentigo-frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start development server:
```bash
npm run dev
```

The frontend will be available at `http://localhost:5173`

## Configuration

### Application Properties

Key application properties (`application.properties`):

```properties
# Server
server.port=8080

# Database
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=your_jwt_secret_key
jwt.expiration=86400000

# Stripe
stripe.secret-key=your_stripe_secret_key
stripe.webhook.secret=your_stripe_webhook_secret
app.frontend.success-url=http://localhost:5173/payment/success
app.frontend.cancel-url=http://localhost:5173/payment/cancel
```

## Testing

Run backend tests:
```bash
cd backend/rentigo-backend
./mvnw test
```

Run frontend tests:
```bash
cd frontend/rentigo-frontend
npm run test
```
