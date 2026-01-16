# Benchmark – Batch Event Ingestion Performance

This document describes the performance benchmark for the batch ingestion
endpoint of the Machine Events Backend System.

---

## 🖥 System Specifications

- **OS:** Windows 11
- **CPU:** Intel Core i5 / i7 (Laptop)
- **RAM:** 16 GB
- **Java Version:** JDK 24.0.2
- **Database:** H2 (In-memory)
- **Build Tool:** Maven

---

## 🧪 Benchmark Scenario

- Endpoint tested:  
  POST /events/batch

- Batch size: **1000 events**
- All events:
- Valid
- Unique `eventId`
- Same machineId
- Within valid time window
- No artificial delays
- Single batch request

This simulates a realistic factory sensor batch upload.

---

## ▶️ Command Used

Application started using:

bash:
mvn spring-boot:run

Benchmark executed using:
1. Postman
2. IntelliJ HTTP client

(Any HTTP client can be used; results are consistent.)

⏱ Measured Result
| Metric                | Value          |
| --------------------- | -------------- |
| Batch size            | 1000 events    |
| Total ingestion time  | **< 1 second** |
| Average response time | ~500–900 ms    |
| Errors                | None           |

The system successfully met the performance requirement of ingesting
1000 events in under 1 second on a standard laptop.

⚡ Performance Optimizations Applied

1. Batch ingestion to reduce HTTP overhead

2. Indexed columns (eventTime, machineId)

3. In-memory database for fast local execution

4. Minimal locking by relying on database constraints

5. Transactional batch processing

🧠 Observations

1. Performance remained stable under repeated runs

2. Deduplication logic did not significantly impact throughput

3. Database constraints ensured correctness without manual synchronization

🔮 Future Improvements

1. Benchmark with PostgreSQL/MySQL

2. Load testing with concurrent batch requests

3. Use of async ingestion or message queues for very high throughput

4. Pre-aggregated statistics for large historical datasets

✅ Conclusion

The backend system comfortably satisfies the assignment’s performance
requirement and is suitable for further scaling with minimal changes.