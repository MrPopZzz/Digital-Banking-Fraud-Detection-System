# Digital Banking & Fraud Detection System

An event-driven microservices backend that simulates a real digital banking flow: a sender transfers money to a receiver, every transaction is screened for fraud, failed or suspicious transfers are refunded automatically, and users are notified at each step.

Built with **Java, Spring Boot, Apache Kafka, MySQL, Redis and Docker**.

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Services](#services)
- [Transaction Flow](#transaction-flow)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [Design Decisions](#design-decisions)
- [Project Structure](#project-structure)
- [Future Improvements](#future-improvements)
- [Author](#author)

---

## Features

- **Money transfers** between sender and receiver accounts
- **Real-time fraud detection** on every transaction before it is finalized
- **Automatic refunds** when a transfer fails or is flagged
- **OTP verification** with short-lived codes stored in Redis
- **Asynchronous notifications** (transaction success, failure, refund, fraud alert)
- **API Gateway** as the single entry point for all client requests
- **Event-driven communication** between services using Kafka
- **Centralized error handling** with consistent error responses
- **One-command startup** using Docker Compose

---

## Architecture

```
                         +----------------+
        Client  ───────► |  API Gateway   |
                         +-------+--------+
                                 │
          ┌──────────────┬───────┴────────┬──────────────┐
          ▼              ▼                ▼              ▼
   +-------------+ +--------------+ +-------------+ +-----------------+
   |  Account    | | Transaction  | |  Payment    | | Fraud Detection |
   |  Service    | | Service      | |  Service    | | Service         |
   +------+------+ +------+-------+ +------+------+ +--------+--------+
          │               │                │                 │
          └───────────────┴──── Kafka ─────┴─────────────────┘
                                 │
                                 ▼
                       +--------------------+
                       | Notification       |
                       | Service            |
                       +--------------------+

   MySQL  → persistent data (accounts, transactions, payments)
   Redis  → OTP storage with TTL
```

---

## Services

| Service | Responsibility |
|---|---|
| `api-gateway` | Single entry point. Routes requests to the right service. |
| `account-service` | Manages user accounts and balances. Handles debit/credit operations. |
| `transaction-service` | Creates and tracks transactions and their lifecycle status. |
| `payment-service` | Executes the actual money movement between sender and receiver. |
| `fraud-detection-service` | Evaluates each transaction against fraud rules and flags suspicious ones. |
| `notification-service` | Consumes events and notifies users about transaction outcomes. |

---

## Transaction Flow

1. Client sends a transfer request through the **API Gateway**.
2. **Transaction Service** creates a transaction with status `PENDING` and publishes an event to Kafka.
3. **Fraud Detection Service** consumes the event and evaluates it against the fraud rules.
4. If the transaction is **clean**, **Payment Service** debits the sender and credits the receiver, and the status becomes `COMPLETED`.
5. If the transaction is **flagged** or the payment **fails**, a refund is triggered and the status becomes `FAILED` / `REFUNDED`.
6. **Notification Service** consumes the final event and notifies the user.

```
PENDING ──► (fraud check) ──► COMPLETED
                │
                └──► FLAGGED / FAILED ──► REFUNDED
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java |
| Framework | Spring Boot |
| Messaging | Apache Kafka |
| Database | MySQL |
| Cache / OTP store | Redis |
| API style | REST |
| Containerization | Docker, Docker Compose |
| Build tool | Maven |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker and Docker Compose

### 1. Clone the repository

```bash
git clone https://github.com/MrPopZzz/Digital-Banking-Fraud-Detection-System.git
cd Digital-Banking-Fraud-Detection-System
```

### 2. Start the infrastructure and services

```bash
docker-compose up --build
```

This starts all services along with their dependencies defined in `docker-compose.yml`.

### 3. Run a single service locally (optional)

```bash
cd transaction-service
mvn spring-boot:run
```

### 4. Verify

Send a request through the API Gateway.

---

---

## Design Decisions

- **Event-driven with Kafka** – services stay loosely coupled; fraud checks, payments and notifications react to events instead of calling each other synchronously.
- **Database per service** – each service owns its data, so a change in one does not break the others.
- **Redis for OTPs** – OTPs are short-lived by nature, so a key with a TTL is simpler and faster than a database table.
- **Compensating refunds** – if any step after the debit fails, the system refunds instead of leaving money in an inconsistent state.
- **API Gateway** – one public entry point keeps internal service addresses hidden and makes cross-cutting concerns (routing, auth) easy to add later.

---

## Project Structure

```
Digital-Banking-Fraud-Detection-System/
├── api-gateway/
├── account-service/
├── transaction-service/
├── payment-service/
├── fraud-detection-service/
├── notification-service/
└── docker-compose.yml
```

---

## Future Improvements

- JWT-based authentication and role-based access at the gateway
- Machine-learning based fraud scoring on top of the rule engine
- Idempotency keys to prevent duplicate transfers
- Dead-letter topics and retry policies for failed Kafka events
- Distributed tracing (Micrometer / Zipkin) and centralized logging
- Unit and integration tests with Testcontainers
- CI pipeline with GitHub Actions

---

## Author

**Sayan Chakraborty** – Java Backend Developer

- GitHub: [@MrPopZzz](https://github.com/MrPopZzz)
