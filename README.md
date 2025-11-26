EcoSpace - Subscription Management System
Overview
EcoSpace is a comprehensive subscription management platform built with Spring Boot that enables businesses to manage subscriptions, process payments, and handle customer communications efficiently. The system consists of a main application and a dedicated microservice for notifications.

🚀 Tech Stack
Main Application (Eco-Space)
Java 17

Spring Boot 3.4.0

Spring Security - Authentication & Authorization

Spring Data JPA - Database Operations

Spring MVC - Web Layer

Thymeleaf - Server-side Templating

MySQL - Primary Database

Spring Cache - Caching Support

Lombok - Code Generation

Spring Boot DevTools - Development Tools

Validation API - Input Validation

Bootstrap 5.3.2 & jQuery 3.7.1 - Frontend Assets

Notification Microservice (message-svc)
Java 17

Spring Boot 3.5.7

Spring Data JPA - Database Operations

Spring Web - REST API

MySQL - Database

Twilio SDK 9.0.0 - SMS Notifications

Spring Mail - Email Notifications

Validation API - Input Validation

Lombok - Code Generation

📋 Core Features
Subscription Management
Create, Edit, Delete subscription packages

Multiple Subscription Types (Maintenance, Design, etc.)

Flexible Pricing Models

Package Descriptions & Metadata

User Management
User Registration & Authentication

Role-based Access Control (Admin, Client)

User Profile Management

Phone Number & Email Integration

Subscription Lifecycle
Purchase Subscriptions

Renew Subscription Products

Cancel Subscriptions

Automatic Expiration Handling

7-day Grace Period Management

Payment Integration
PayFast Payment Gateway Integration

Payment Status Tracking

Pending Payment Management

Payment Verification & Reconciliation

Notification System
SMS Notifications via Twilio

Email Notifications for subscription renewals

Expiration Reminders

Company Communication Channel

🔄 System Integrations
External Services
PayFast - Payment processing

Twilio - SMS notifications

SMTP/Email - Email delivery

MySQL - Data persistence

Microservice Architecture
Main Application: Handles core business logic, user management, and subscriptions

Notification Service: Dedicated service for all communication (SMS, Email)

🛠️ Functionalities
Admin Features
Full CRUD operations on subscription packages

User management and role assignment

System monitoring and health checks

View all users and their subscriptions

Client Features
Browse available subscriptions

Purchase and manage subscriptions

Profile management

View subscription history

Cancel subscriptions

Automated Processes
Scheduled Expiration Checks - Hourly monitoring

Automatic Cleanup - Remove expired subscriptions after 7 days

Notification Scheduling - Pre-expiration reminders

Cache Management - Automatic cache refresh

Communication Features
For Registered Users
Subscription renewal notifications via SMS/Email

Expiration warnings

Payment confirmations

Profile update notifications

For Unregistered Users
Contact company via email

General inquiries

Service information requests

 Security Features
Spring Security integration

Password encryption

Role-based access control

Secure payment processing

Input validation and sanitization

 Database Schema
Key Entities
User - User accounts and profiles

Subscription - Subscription package definitions

Product - User-purchased subscription instances

Payment - Payment transaction records.

 Monitoring & Maintenance
Scheduled Tasks
Hourly: Expired subscription cleanup

Daily: Notification status reset

Regular: Cache refresh

Continuous: Payment status reconciliation

Logging
Comprehensive logging for all operations

Error tracking and monitoring

Performance metrics

API Endpoints
Main Application
GET/POST /subscriptions - Subscription management

GET/POST /users - User management

GET/POST /payments - Payment processing

GET/POST /profile - User profile management

Notification Service
POST /api/notifications/sms - Send SMS

POST /api/notifications/email - Send Email

POST /api/notifications/reminder - Send renewal reminders

 Future Enhancements
Multi-currency support

Advanced analytics and reporting

Mobile application

Webhook integrations

Advanced payment methods

Subscription tier upgrades/downgrades
