# Digital Banking & Fraud Detection System

An **event-driven digital banking backend** built with **Java and Spring Boot**, designed using a **microservices architecture** to simulate secure money transfers, real-time fraud detection, asynchronous notifications, OTP verification, and compensating refunds.

The system uses **Apache Kafka** for asynchronous communication, **Redis** for short-lived OTP storage, **MySQL** for persistent data, and **Docker Compose** for containerized infrastructure.

> **Goal:** Model how a distributed banking system can process a transaction, evaluate it for fraud, complete or compensate the transaction, and notify users without tightly coupling individual services.

---

## Key Features

* **Money Transfer** — Transfer funds between sender and receiver accounts.
* **Real-Time Fraud Detection** — Evaluate transactions against configurable fraud rules before completion.
* **Compensating Refunds** — Automatically refund transactions when fraud is detected or payment processing fails.
* **OTP Verification** — Generate and store short-lived OTPs using Redis TTL.
* **Event-Driven Architecture** — Services communicate asynchronously through Apache Kafka.
* **Asynchronous Notifications** — Process transaction success, failure, refund, and fraud events independently.
* **API Gateway** — Provides a single entry point and routes requests to internal services.
* **Database per Service** — Each service maintains ownership of its persistent data.
* **Dockerized Environment** — Infrastructure and services can be started using Docker Compose.
* **Centralized Error Handling** — Consistent error responses across REST APIs.

---

## Architecture

```text
                           ┌───────────────────┐
                           │      Client       │
                           └─────────┬─────────┘
                                     │
                                     ▼
                           ┌───────────────────┐
                           │    API Gateway    │
                           │  Routing / Entry  │
                           └─────────┬─────────┘
                                     │
              ┌──────────────────────┼──────────────────────┐
              │                      │                      │
              ▼                      ▼                      ▼
      ┌──────────────┐      ┌──────────────┐      ┌──────────────┐
      │    Account   │      │ Transaction  │      │   Payment    │
      │    Service   │      │    Service   │      │   Service    │
      └──────┬───────┘      └──────┬───────┘      └──────┬───────┘
             │                     │                     │
             │                     └──────────┬──────────┘
             │                                │
             │                                ▼
             │                        ┌──────────────┐
             │                        │ Apache Kafka │
             │                        └──────┬───────┘
             │                               │
             │                 ┌─────────────┴─────────────┐
             │                 ▼                           ▼
             │        ┌──────────────────┐       ┌──────────────────┐
             └───────►│ Fraud Detection  │       │  Notification    │
                      │     Service      │       │     Service      │
                      └────────┬─────────┘       └──────────────────┘
                               │
                               ▼
                         Fraud Result
                               │
                               ▼
                      Account / Transaction
                         State Update


      MySQL → Persistent banking and transaction data
      Redis → OTP storage with expiration (TTL)
```

---

## Transaction Flow

A typical transfer follows this lifecycle:

```text
Client
  │
  ▼
API Gateway
  │
  ▼
Transaction Service
  │
  │ Create transaction
  │ status = PENDING
  ▼
Kafka
  │
  ▼
Fraud Detection Service
  │
  ├─────────────── Clean ───────────────┐
  │                                     ▼
  │                              Payment Service
  │                                     │
  │                            Debit → Credit
  │                                     │
  │                                     ▼
  │                              COMPLETED
  │
  └────────────── Suspicious ───────────┐
                                        ▼
                                  Refund Flow
                                        │
                                        ▼
                                    REFUNDED

                    Final Event
                         │
                         ▼
                Notification Service
```

### Transaction states

```text
PENDING
   │
   ├── Fraud check passed ──► COMPLETED
   │
   └── Fraud / Payment failure
              │
              ▼
           REFUNDED
```

---

## Microservices

| Service                     | Responsibility                                             |
| --------------------------- | ---------------------------------------------------------- |
| **API Gateway**             | Single entry point and request routing                     |
| **Account Service**         | Account management, balance operations, debit/credit       |
| **Transaction Service**     | Transaction creation, tracking, and lifecycle management   |
| **Payment Service**         | Executes fund movement between accounts                    |
| **Fraud Detection Service** | Evaluates transactions against fraud detection rules       |
| **Notification Service**    | Consumes transaction events and handles user notifications |

---

## Event-Driven Communication

Apache Kafka is used to decouple services and allow transaction processing to happen asynchronously.

Instead of tightly coupling services through synchronous REST calls:

```text
Transaction Service
        │
        ▼
     Kafka Event
        │
   ┌────┴─────────────┐
   ▼                  ▼
Fraud Detection   Notification
```

This allows individual services to react to events independently.

### Example event flow

```text
Transaction Created
        ↓
transaction event
        ↓
Fraud Detection
        ↓
fraud result
        ↓
Payment / Refund
        ↓
Final transaction event
        ↓
Notification
```

---

## Redis & OTP

Redis is used for OTP storage because OTPs are temporary data.

```text
Generate OTP
     │
     ▼
Store in Redis
     │
     └── TTL
          │
          ▼
      Auto Expiration
```

Using Redis TTL eliminates the need to manually remove expired OTP records from a relational database.

---

## Compensating Refunds

Distributed financial operations can involve multiple services and therefore cannot simply rely on a single database transaction across the entire system.

The project uses a **compensating action** approach.

For example:

```text
Debit Sender
     │
     ▼
Payment Failure / Fraud
     │
     ▼
Compensating Refund
     │
     ▼
Restore Sender Balance
```

This prevents a failed transaction from leaving the system in an inconsistent state.

---

## Technology Stack

| Category                  | Technology     |
| ------------------------- | -------------- |
| Language                  | Java           |
| Framework                 | Spring Boot    |
| Architecture              | Microservices  |
| API                       | REST           |
| Messaging                 | Apache Kafka   |
| Database                  | MySQL          |
| Cache / Temporary Storage | Redis          |
| Containerization          | Docker         |
| Orchestration             | Docker Compose |
| Build Tool                | Maven          |

---

## Project Structure

```text
Digital-Banking-Fraud-Detection-System/
│
├── api-gateway/
│
├── account-service/
│
├── transaction-service/
│
├── payment-service/
│
├── fraud-detection-service/
│
├── notification-service/
│
└── docker-compose.yml
```

---

## Getting Started

### Prerequisites

Make sure you have:

* Java 17+
* Maven 3.8+
* Docker
* Docker Compose

### Clone the repository

```bash
git clone https://github.com/MrPopZzz/Digital-Banking-Fraud-Detection-System.git

cd Digital-Banking-Fraud-Detection-System
```

### Start the application

```bash
docker-compose up --build
```

Docker Compose starts the services and infrastructure defined in the project configuration.

### Run an individual service locally

For example:

```bash
cd transaction-service

mvn spring-boot:run
```

The application can then be accessed through the configured API Gateway.

---

## Key Design Decisions

### Event-driven communication

Kafka decouples services and allows fraud detection and notifications to process transaction events asynchronously.

### Database per service

Each microservice owns its persistent data, reducing direct coupling between services.

### Redis for OTPs

OTP data is temporary and therefore benefits from Redis's fast access and TTL-based expiration.

### Compensating transactions

Instead of attempting a distributed database transaction across services, failed operations can trigger compensating actions such as refunds.

### API Gateway

The Gateway provides a single public entry point while keeping internal service endpoints isolated.

---

## Future Improvements

The project can be extended with:

* [ ] JWT authentication and role-based authorization
* [ ] Idempotency keys for duplicate-transfer protection
* [ ] Kafka retry mechanisms and Dead Letter Topics
* [ ] Circuit breakers and resilience patterns
* [ ] Distributed tracing with Micrometer / Zipkin
* [ ] Centralized logging
* [ ] Machine-learning-based fraud scoring
* [ ] Unit and integration testing with Testcontainers
* [ ] CI/CD using GitHub Actions
* [ ] OpenAPI / Swagger documentation
* [ ] Observability with Prometheus and Grafana

---

## Author

**Sayan Chakraborty**

Java Backend Developer

GitHub: [@MrPopZzz](https://github.com/MrPopZzz)
