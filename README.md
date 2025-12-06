# English Learning Platform - Backend API

A comprehensive RESTful API backend for an English learning platform built with Spring Boot 3.5.5 and Java 21.

## Overview

This platform provides a complete solution for online English learning, featuring course management, AI-powered assessment for speaking and writing skills, quiz systems, e-commerce capabilities, and community features.

## Features

### Learning Management
- **Course System**: Create and manage courses with modules, lessons, and multimedia content
- **Quiz Engine**: Multiple quiz types with sections, questions, and configurable options
- **AI Assessment**: Automated grading for speaking (fluency, pronunciation, grammar, vocabulary) and writing (task response, coherence, grammar, vocabulary) submissions
- **Progress Tracking**: Enrollment management with detailed lesson progress and completion tracking
- **Study Plans**: Personalized study schedules with Google Calendar integration

### User & Instructor
- **User Management**: Registration, authentication (JWT & OAuth2/Google), email verification
- **Role-Based Access**: Admin, Instructor, and Student roles with fine-grained permissions
- **Instructor Features**: Profile management, wallet system, earnings tracking, withdrawal requests, bank account management

### E-Commerce
- **Shopping Cart**: Add courses to cart before purchase
- **Order Management**: Complete order lifecycle with multiple payment support
- **Payment Gateways**: PayOS (Vietnam VND) and PayPal (International USD)
- **Invoice Generation**: PDF invoice generation for completed orders
- **Platform Fees**: Configurable platform fee percentage for instructor earnings

### Community
- **Blog System**: Create and manage blog posts with categories and comments
- **Discussion Forum**: Forum categories, threads, posts with moderation and reporting system
- **Course Reviews**: Students can rate and review enrolled courses

### Notifications
- **Push Notifications**: Firebase Cloud Messaging (FCM) integration
- **Email Notifications**: SMTP-based email system for verification and updates

## Tech Stack

| Category | Technology |
|----------|------------|
| Framework | Spring Boot 3.5.5 |
| Language | Java 21 |
| Database | PostgreSQL |
| Caching | Redis |
| Security | Spring Security, JWT, OAuth2 |
| Storage | AWS S3 Compatible |
| Payments | PayOS, PayPal |
| Notifications | Firebase FCM, SMTP |
| API Docs | SpringDoc OpenAPI |
| AI Integration | N8N Webhooks |
| Calendar | Google Calendar API |

## Project Structure

```
src/main/java/com/english/api/
├── admin/          # Admin dashboard & overview
├── assessment/     # Speaking & Writing AI assessment
├── auth/           # Authentication & authorization
├── blog/           # Blog posts, categories, comments
├── cart/           # Shopping cart management
├── common/         # Shared utilities & media handling
├── course/         # Courses, modules, lessons, reviews
├── enrollment/     # Enrollments, progress, study plans
├── forum/          # Forum threads, posts, reports
├── mail/           # Email service
├── notification/   # Push notifications
├── order/          # Orders, payments, invoices
├── quiz/           # Quiz types, sections, questions
└── user/           # Users, instructors, wallets
```

## Getting Started

### Prerequisites
- Java 21+
- PostgreSQL 14+
- Redis 6+
- Maven 3.8+

### Environment Variables

Create a `.env` file or set the following environment variables:

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=english_db
DB_USERNAME=postgres
DB_PASSWORD=your_password
DB_SSL_MODE=disable

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_USERNAME=
REDIS_PASSWORD=
REDIS_SSL_ENABLED=false

# JWT
JWT_SECRET_KEY=your_jwt_secret
JWT_ACCESSTOKEN_EXP=3600
JWT_REFRESHTOKEN_EXP=604800

# OAuth2 (Google)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# S3 Storage
S3_ENDPOINT=https://your-s3-endpoint
S3_ACCESS_KEY=your_access_key
S3_SECRET_KEY=your_secret_key
S3_BUCKET=your_bucket
S3_PUBLIC_URL=https://your-public-url

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email
MAIL_PASSWORD=your_app_password

# Payment - PayOS (Vietnam)
PAYOS_CLIENT_ID=your_client_id
PAYOS_API_KEY=your_api_key
PAYOS_CHECKSUM_KEY=your_checksum_key

# Payment - PayPal
PAYPAL_CLIENT_ID=your_client_id
PAYPAL_CLIENT_SECRET=your_secret

# Firebase
# Place fcm-service-account.json in src/main/resources/

# AI Processing (N8N)
N8N_SPEAKING_WEBHOOK_URL=your_webhook_url
N8N_WRITING_WEBHOOK_URL=your_webhook_url
N8N_CALLBACK_SECRET=your_secret
```

### Build & Run

```bash
# Build the project
./mvnw clean package -DskipTests

# Run the application
./mvnw spring-boot:run

# Or run the JAR directly
java -jar target/api-0.0.1-SNAPSHOT.jar
```

The API will be available at `http://localhost:8080`

## API Documentation

When enabled, Swagger UI is available at `/swagger-ui.html`

## License

This project is proprietary software. All rights reserved.

---

*For the Vietnamese version of this README, see [README_VI.md](README_VI.md)*
