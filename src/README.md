# Machine Events Backend System

This project implements a backend system to ingest, store, deduplicate, and analyze machine-generated events for a factory environment.  
It is built as part of a Backend Intern assignment using **Java + Spring Boot**.

The system supports:
- High-throughput batch ingestion of events
- Deduplication and update handling
- Time-window–based statistics queries
- Thread-safe concurrent ingestion
- Full test coverage for critical edge cases

---

## 🛠 Tech Stack

- **Language:** Java (JDK 24)
- **Framework:** Spring Boot
- **Database:** H2 (in-memory)
- **ORM:** Spring Data JPA (Hibernate)
- **Build Tool:** Maven
- **Testing:** JUnit 5, Spring Boot Test
- **Utilities:** Lombok

---

## 🧩 Architecture Overview

The application follows a clean layered architecture:

Controller → Service → Repository → Database


### Layers:
- **Controller:** Handles HTTP requests and responses (no business logic)
- **Service:** Core business logic (validation, deduplication, updates, stats)
- **Repository:** Data access using Spring Data JPA
- **Model:** JPA entities representing persisted data
- **DTOs:** Request/response data transfer objects

This separation improves maintainability, testability, and clarity.

---

## 📦 Data Model

### Event Entity

Each machine event is stored with the following fields:

| Field | Description |
|-----|------------|
| id | Database-generated primary key |
| eventId | Business identifier (unique) |
| eventTime | Time when event occurred (used for queries) |
| receivedTime | Time when backend received the event |
| machineId | Identifier of the machine |
| durationMs | Duration of the event |
| defectCount | Number of defects (`-1` means unknown) |
| createdAt | Record creation timestamp |

### Constraints & Indexes
- **Unique constraint on `eventId`** → prevents duplicates
- Index on `eventTime` → fast time-range queries
- Index on `machineId` → fast machine-level stats

---

## 🔁 Deduplication & Update Logic

Events are deduplicated based on `eventId`.

| Scenario | Action |
|------|------|
| Same `eventId` + identical payload | Deduped (ignored) |
| Same `eventId` + different payload + newer receivedTime | Updated |
| Same `eventId` + different payload + older receivedTime | Ignored |

- `receivedTime` is always generated server-side
- Client-provided `receivedTime` is ignored

This logic ensures idempotency and correctness under retries.

---

## ✅ Validation Rules

An event is rejected if:
- `durationMs < 0`
- `durationMs > 6 hours`
- `eventTime` is more than 15 minutes in the future

Special rule:
- `defectCount = -1` means *unknown* → stored but ignored in defect calculations

---

## 🚀 API Endpoints

### 1️⃣ Batch Ingestion
POST /events/batch

**Input:** JSON array of events  
**Output:** Counts of accepted, updated, deduped, and rejected events

---

### 2️⃣ Machine Statistics
GET /stats?machineId=...&start=...&end=...


- `start` is inclusive
- `end` is exclusive
- Uses `eventTime` for filtering

Returns:
- `eventsCount`
- `defectsCount` (ignores `-1`)
- `avgDefectRate` (defects per hour)
- `status` (`Healthy` if < 2.0, else `Warning`)

---

## 🔒 Thread Safety

The system is thread-safe due to:
- Database-level unique constraint on `eventId`
- Transactional batch ingestion (`@Transactional`)
- Idempotent deduplication logic
- HikariCP connection pooling

Concurrent ingestion is explicitly tested using parallel threads.

---

## ⚡ Performance Strategy

- Batch ingestion minimizes HTTP overhead
- Indexes on frequently queried columns
- In-memory H2 database for fast local execution
- Minimal locking, relying on DB constraints for correctness

The system comfortably processes **1000 events in under 1 second** on a standard laptop.

---

## 🧪 Testing

- Integration-style tests using `@SpringBootTest`
- H2 in-memory DB with transactional rollback
- **8 mandatory test cases implemented**, covering:
    - Deduplication
    - Updates
    - Invalid inputs
    - Future timestamps
    - Defect count rules
    - Time window correctness
    - Concurrent ingestion safety

All tests pass with exit code `0`.

---

## ▶️ Setup & Run Instructions

### Prerequisites
- Java 17+ (tested on Java 24)
- Maven
### Run
mvn spring-boot:run

🧠 Assumptions & Trade-offs

1. H2 is used for simplicity and local execution

2. Stats calculations are done in-memory for flexibility

3. No pagination implemented (batch size assumed manageable)

4. No authentication (out of scope)

🔮 Improvements With More Time

1. Persist to PostgreSQL/MySQL

2. Add pagination & filtering

3. Pre-aggregated stats for very large datasets

4. Metrics & monitoring (Micrometer, Prometheus)

5. Dockerize application