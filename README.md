# 🎫 Flash Ticket System

A high-concurrency event ticketing backend built with **Java 21**, **Spring Boot 3**, **PostgreSQL**, and **Redis**. Designed to handle flash-sale traffic surges with sub-millisecond cache latency and atomic inventory decrement to prevent overselling and race conditions.

---

## 🌟 Key Features & Engineering Highlights

* **Layered Architecture (3-Tier):** Strict separation of concerns across **Presentation Layer** (`@RestController`), **Domain/Business Layer** (`@Service`), **Data Access Layer** (`JpaRepository`), and contract encapsulation using **DTOs**.
* **Cache-Aside Pattern with Redis:** Mitigates database read bottlenecks on high-traffic endpoints using Spring Cache (`@Cacheable`) with configurable **TTL (Time-To-Live)**.
* **Automatic Cache Invalidation:** Guarantees data consistency across distributed reads using `@CacheEvict` upon ticket creation and stock modification events.
* **Atomic Concurrency Control (Flash Sale):** Eliminates race conditions and prevents inventory overselling during simultaneous booking requests by executing in-memory **Atomic Decrements (`opsForValue().decrement`)** in Redis before persisting state transitions to PostgreSQL.
* **Rollback Mechanism:** Automatically reverts Redis decrements if stock reaches below zero, safely rejecting over-allocation attempts.
* **Containerized Dependencies:** Fully containerized development environment with **Docker Compose** managing PostgreSQL 16 and Redis 7.

---

## 🛠️ Tech Stack

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Language** | Java 21 | Modern LTS Java runtime |
| **Framework** | Spring Boot 3.3+ | Spring Web MVC, Spring Data JPA, Spring Data Redis, Spring Cache |
| **Database** | PostgreSQL 16 | ACID-compliant relational persistence store |
| **In-Memory Store** | Redis 7 | High-performance cache & atomic counter |
| **ORM** | Hibernate ORM | Database schema auto-generation & object relational mapping |
| **Tooling** | Lombok, Maven | Boilerplate reduction & dependency management |
| **Infrastructure** | Docker & Docker Compose | Containerized local environment |

---

## 🏛️ System Architecture & Data Flow

```
[ Client / Postman / Frontend ]
               │
               ▼ HTTP REST (JSON)
┌─────────────────────────────────────────────────────────────┐
│  Spring Boot Application                                    │
│                                                             │
│  1. Controller Layer  ──► HTTP Request Routing & Validation │
│  2. Service Layer     ──► Business Rules & Concurrency Lock │
│  3. Repository Layer  ──► Spring Data JPA Queries           │
└──────────────┬───────────────────────────────┬──────────────┘
               │                               │
       (Spring Data JPA)              (Spring Data Redis)
               ▼                               ▼
    ┌──────────────────────┐        ┌──────────────────────┐
    │  PostgreSQL (Docker) │        │    Redis (Docker)    │
    │  Port: 5433          │        │    Port: 6380        │
    │  [Persistent State]  │        │  [Fast Cache & DECR] │
    └──────────────────────┘        └──────────────────────┘
```

### ⚡ Purchase Concurrency Flow
1. Client issues a `POST /api/tickets/purchase` request.
2. The Service layer intercepts and executes an **atomic decrement** against Redis key `ticket:stock:{id}`.
3. **If remaining stock < 0:** Redis counter is immediately rolled back via atomic increment, and an exception is raised (`Ticket SOLD OUT!`). PostgreSQL is never hit.
4. **If remaining stock >= 0:** Ticket quantity is safely updated in PostgreSQL, the cached ticket detail is evicted, and a confirmation payload is returned.

---

## 🚀 Getting Started

### Prerequisites
* **Java Development Kit (JDK 17 or 21)**
* **Docker Desktop** (Engine running)
* **Git**

### 1. Clone the Repository
```bash
git clone https://github.com/rakanuarinugraha/flash-ticket-system.git
cd flash-ticket-system
```

### 2. Start Infrastructure Containers
Start PostgreSQL and Redis in the background:
```bash
docker compose up -d
```
Verify running containers:
```bash
docker compose ps
```

### 3. Run the Spring Boot Backend
Navigate to the `backend` directory and execute the Maven wrapper:
```bash
cd backend
./mvnw spring-boot:run
```
The server will start at: `http://localhost:8000`

---

## 📡 API Reference

### 1. Get All Tickets
Fetches all available tickets. Cached in Redis under key `tickets::all`.

* **Endpoint:** `GET /api/tickets`
* **Response:** `200 OK`
```json
[
  {
    "id": 1,
    "title": "Coldplay Music of the Spheres Jakarta",
    "description": "Exclusive live concert at Gelora Bung Karno Stadium",
    "price": 1500000.00,
    "totalStock": 100,
    "availableStock": 98,
    "createdAt": "2026-09-18T19:51:39.335515"
  }
]
```

---

### 2. Get Ticket by ID
Retrieves ticket details by identifier. Cached in Redis under key `ticket::{id}`.

* **Endpoint:** `GET /api/tickets/{id}`
* **Response:** `200 OK`

---

### 3. Create Ticket
Creates a new ticket item and automatically invalidates the `tickets::all` cache.

* **Endpoint:** `POST /api/tickets`
* **Headers:** `Content-Type: application/json`
* **Request Body:**
```json
{
  "title": "Coldplay Music of the Spheres Jakarta",
  "description": "Exclusive live concert at Gelora Bung Karno Stadium",
  "price": 1500000.00,
  "totalStock": 100
}
```
* **Response:** `201 Created`

---

### 4. Purchase Ticket (Flash Sale)
Attempts to purchase a specified quantity of a ticket using Redis atomic operations.

* **Endpoint:** `POST /api/tickets/purchase`
* **Headers:** `Content-Type: application/json`
* **Request Body:**
```json
{
  "ticketId": 1,
  "quantity": 2
}
```

* **Success Response (`200 OK`):**
```json
{
  "success": true,
  "message": "Tiket berhasil dibeli",
  "ticketId": 1,
  "purchasedQuantity": 2,
  "remainingStock": 98,
  "timestamp": "2026-09-21T08:10:00.123456"
}
```

* **Sold Out Response (`500 / Error`):**
```json
{
  "message": "Ticket SOLD OUT!"
}
```

---

## 👤 Author

* **Rakanuari Dwi Nugraha**
  * LinkedIn: [linkedin.com/in/rakanuari-dwi-nugraha](https://linkedin.com/in/rakanuari-dwi-nugraha)
  * GitHub: [@rakanuarinugraha](https://github.com/rakanuarinugraha)