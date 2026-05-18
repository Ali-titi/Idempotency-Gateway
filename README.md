# 💳 Idempotency Gateway (Pay-Once Payment System)

## 📌 Overview

The Idempotency Gateway is a Spring Boot REST API designed to prevent double payment processing in distributed systems. It ensures that a payment request is processed exactly once, even if the client retries the request multiple times due to network failures.

---

## 🎯 Problem Solved

In real-world payment systems, network timeouts can cause clients to resend the same request. Without protection, this can lead to:

- Double charging customers
- Data inconsistency
- Financial loss
- Trust issues

This system solves that using an **Idempotency-Key mechanism**.

---

## 🏗 Architecture Diagram

```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant Service
    participant Database

    Client->>Controller: POST /process-payment + Idempotency-Key
    Controller->>Service: processPayment(key, request)

    Service->>Database: Check Idempotency Key

    alt First Request
        Service->>Database: Save request hash + processing state
        Service->>Service: Simulate payment (2s delay)
        Service->>Database: Save response + mark completed
        Service-->>Controller: Return success response
    else Duplicate Request (same key, same body)
        Service-->>Controller: Return cached response (no processing)
    else Same key, different body
        Service-->>Controller: Return 409 Conflict
    end

    Controller-->>Client: Response

⚙️ Tech Stack
Java 17
Spring Boot
Spring Web
Spring Data JPA
Hibernate
PostgreSQL
Maven
🚀 How to Run the Project
1. Clone the repository
git clone <your-repo-url>
cd IdempotencyGateway
2. Set Environment Variables

You MUST set these variables in your system or IntelliJ:

DB_URL=jdbc:postgresql://localhost:5432/idempotency_gateway
DB_USERNAME=postgres
DB_PASSWORD=your_password
3. Run the application
mvn spring-boot:run
4. Application runs on
http://localhost:8080
📡 API Documentation
🔹 Endpoint: Process Payment
POST /process-payment
🔹 Headers
Idempotency-Key: unique-request-key
Content-Type: application/json
🔹 Request Body
{
  "amount": 100,
  "currency": "GHS"
}
✅ Successful Response (First Request)
{
  "message": "Charged 100 GHS",
  "status": "SUCCESS"
}
🔁 Duplicate Request (Same Idempotency-Key)
No re-processing happens
Same response is returned
Header added:
X-Cache-Hit: true
❌ Error Case (Same Key, Different Body)
{
  "error": "Idempotency key already used for a different request body"
}

Status Code: 409 Conflict

🧠 Design Decisions
SHA-256 hashing is used to validate request integrity
ConcurrentHashMap handles in-flight requests safely
Database persistence ensures idempotency across server restarts
CompletableFuture prevents race conditions
⭐ Bonus Feature (Developer Choice)

Added request hashing mechanism (SHA-256) to ensure that the same Idempotency-Key cannot be reused with modified request data. This improves security and prevents fraudulent behavior.

📌 Key Features
Prevents double payment processing
Handles retry-safe API calls
Detects duplicate requests
Protects against payload tampering
Handles race conditions
👨‍💻 Author

Built as a backend engineering challenge using Spring Boot and PostgreSQL.

🛑 Notes
Make sure PostgreSQL is running before starting the app
Ensure environment variables are set correctly
Do not hardcode credentials in the codebase

---
