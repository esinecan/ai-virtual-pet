# CyberCore Companion MVP Design Document
Project Name: CyberCore Companion
Type: AI-Driven Pet Simulator (MVP)
Purpose: Demonstrate backend architecture, async AI integration, and DevOps/observability skills with tools like Spring Boot, Kafka, Prometheus, and Kubernetes.
________________


1. Overview
1.1. Objective
Build an MVP showcasing:
* Backend Engineering: REST APIs, event-driven microservices, and database design.
* AI Integration: Local LLM-driven interactions with RAG (Retrieval-Augmented Generation).
* Observability: Prometheus/Grafana monitoring and logging.
* DevOps: CI/CD with GitHub Actions, Kubernetes deployments.
1.2. Scope
* Core Features:
   * User/Coreling management APIs.
   * Async AI interaction processing via Kafka.
   * Coreling personality shaping using pgvector (PostgreSQL).
   * Monitoring with Prometheus/Grafana.
* AWS Touchpoints (Minimal):
   * Optional deployment to EKS (if time permits).
   * Secrets management via Kubernetes Secrets (not AWS Secrets Manager).
________________


2. System Architecture
2.1. Architecture Diagram
```
                   +----------------------+
                   |   Client (SwaggerUI) |
                   +----------+-----------+
                              |
                    REST API (Spring Boot)
                              |
        +---------------------+----------------------+
        |                                            |
+-------+--------+                          +--------+--------+
|  User/Coreling |                          |   AI Interaction |
|  Management    |                          |   Service (Kafka)|
|  (Spring Boot) |                          +--------+---------+
+-------+--------+                                   |
        |                                  +---------v---------+
+-------v--------+                        |    Local LLM       |
| PostgreSQL      |                        |  (Llama 3 + RAG)  |
| (pgvector)      |                        +---------+---------+
+-------+--------+                                   |
        |                                  +---------v---------+
+-------v--------+                        |   Redis (Cache)    |
|   Kafka        |                        +--------------------+
| (Event Broker) |
+----------------+
        |
+-------v--------+
|   Prometheus   |
|   + Grafana    |
+----------------+
```

2.2. Component Breakdown
1. Spring Boot API:
   * REST endpoints for user/Coreling management.
   * Integrated with Kafka for async AI processing.
2. AI Interaction Service:
   * Kafka consumer processing interactions.
   * RAG workflow using pgvector for memory-based responses.
   * Local LLM (Llama 3) for generating conversational feedback.
3. Database & Caching:
   * PostgreSQL: Stores Coreling state + vector embeddings via pgvector.
   * Redis: Caches frequently accessed Coreling states.
4. Observability:
   * Prometheus: Metrics collection (API latency, Kafka throughput).
   * Grafana: Dashboards for system health monitoring.
5. Kubernetes (Optional):
   * Local minikube cluster or EKS deployment for container orchestration.
________________


3. Feature Details
3.1. Core Mechanics
* Async AI Workflow:
   * User sends interaction → Kafka message → AI service processes with RAG → Updates Coreling state.
   * Status Tracking: GET /interaction/{id}/status endpoint (status stored in Redis).
* Personality via Vectors:
   * Recent interactions stored as vectors in PostgreSQL.
   * RAG uses similarity search to contextualize responses.
* Monitoring:
   * Spring Boot Actuator exposes metrics → Prometheus scrapes → Grafana visualizes.
3.2. API Endpoints (Key Examples)
* User/Coreling Management:
   * POST /api/auth/register, POST /api/auth/login, GET /api/coreling/{userAccountId}, POST /api/coreling/{userAccountId}/interact.
* Async Interaction:
   * GET /api/interaction/{interactionId}/status → Returns { status: "processing/complete", response: "..." }.
________________


4. Technical Stack
4.1. Core Stack
* Backend: Java 17, Spring Boot 3, Spring Kafka.
* AI: Local Llama 3 (via Hugging Face Transformers), pgvector.
* Database: PostgreSQL, Redis.
* Event Broker: Kafka.
* Observability: Prometheus, Grafana, ELK Stack (logs).
* DevOps: Docker, Kubernetes (minikube), GitHub Actions.
4.2. Strategic AWS Additions (Optional)
* EKS Deployment: If time allows, deploy to EKS with a Terraform/Helm setup (mirroring your CompuGroup Kubernetes experience).
* S3 Bucket: Store LLM model weights (showcases cloud storage basics).
________________


5. Roadmap
Phase 1 (MVP – 2 Weeks)
* Core Features:
   * Implement Spring Boot APIs + Kafka async flow.
   * Integrate local LLM with pgvector for RAG.
   * Set up Prometheus/Grafana monitoring.
* Documentation:
   * SwaggerUI for API testing.
   * GitHub Actions CI/CD (build/test Docker images).
Phase 2 (Observability & Polish – 1 Week)
* Enhance Monitoring:
   * Grafana dashboards for Kafka lag, API error rates.
   * Log aggregation with ElasticSearch (mirroring your CV).
* Security:
   * Add API key authentication via Spring Security.
Phase 3 (AWS Expansion – Post-Certification)
* Deploy to EKS: Add Terraform/EKS configuration files.
* Replace Redis with ElastiCache.
________________

### **Design for the Initial Spring Boot Application**
---

## **1. Core Objectives of the Spring Boot Application**
- **User Authentication & Management**: Handles user registration, login, and authentication (JWT-based).
- **Coreling State Management**: Allows users to retrieve and update their Coreling's state with synchronous actions.
- **Asynchronous AI Processing**: Publishes and consumes messages for LLM interactions via Kafka.
- **Persistence Layer**: Uses PostgreSQL with **pgvector** for memory embeddings.
- **Observability**: Includes logging, metrics, and monitoring via Prometheus/Grafana.
- **API Documentation**: Exposes endpoints via Swagger UI for easy testing.

---

## **2. High-Level Architecture**
### **2.1. Microservice & Component Overview**
```
+------------------------------------------------+
|        CyberCore Spring Boot Service          |
+------------------------------------------------+
|  +----------------------------------------+   |
|  |       REST API (Spring MVC)           |   |
|  |    - AuthController                    |   |
|  |    - CorelingController                |   |
|  |    - InteractionController             |   |
|  +----------------------------------------+   |
|  |   Service Layer                        |   |
|  |    - AuthService                        |   |
|  |    - CorelingService                    |   |
|  |    - InteractionService                 |   |
|  +----------------------------------------+   |
|  |   Persistence Layer (Spring Data JPA)  |   |
|  |    - UserRepository                     |   |
|  |    - CorelingRepository                 |   |
|  |    - ActionHistoryRepository            |   |
|  |    - InteractionRepository              |   |
|  +----------------------------------------+   |
|  |   Messaging (Kafka Producer/Consumer)  |   |
|  |    - KafkaTalkProducer                  |   |
|  |    - KafkaTalkConsumer                  |   |
|  +----------------------------------------+   |
|  |   External Integrations                 |   |
|  |    - PostgreSQL (pgvector for embeddings)|  |
|  |    - Redis (for caching responses)      |   |
|  |    - Prometheus + Grafana               |   |
|  +----------------------------------------+   |
+------------------------------------------------+
```

---

## **3. Project Structure**
A **well-structured package layout** ensures maintainability:
```
com.cybercore.companion
 ├── config              // Spring Security, Swagger, Kafka config
 ├── controller          // REST controllers: Auth, Coreling, Interaction
 ├── dto                 // Request and response DTOs
 ├── model               // Entities: UserAccount, Coreling, ActionHistory, Interaction
 ├── repository          // Spring Data JPA repositories
 ├── service             // Business logic services
 ├── messaging           // Kafka producers & consumers
 ├── util                // Utility classes (e.g., JWT, error handling)
 ├── security            // Security configuration & JWT handling
 ├── observability       // Logging, monitoring, Prometheus integration
 └── CybercoreCompanionApplication.java   // Main application entry point
```

---

## **4. Database Design & Persistence Layer**
### **4.1. Database Tables**
#### **UserAccount Table**
- Stores authentication credentials.
- Used for login and securing API access.
```sql
CREATE TABLE user_account (
  id SERIAL PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);
```
#### **Coreling Table**
- Stores a user’s Coreling state, including its personality and historical actions.
```sql
CREATE TABLE coreling (
  id SERIAL PRIMARY KEY,
  user_account_id INT REFERENCES user_account(id) ON DELETE CASCADE,
  data_integrity INT DEFAULT 100,
  processing_load INT DEFAULT 50,
  emotional_charge INT DEFAULT 50,
  last_updated TIMESTAMP DEFAULT NOW(),
  memory_vector VECTOR(1536)  -- Using pgvector for embedding storage
);
```
#### **ActionHistory Table**
- Logs every action the user takes.
```sql
CREATE TABLE action_history (
  id SERIAL PRIMARY KEY,
  user_account_id INT REFERENCES user_account(id) ON DELETE CASCADE,
  coreling_id INT REFERENCES coreling(id) ON DELETE CASCADE,
  action_type VARCHAR(50),
  payload TEXT,
  timestamp TIMESTAMP DEFAULT NOW()
);
```
#### **Interaction Table**
- Tracks asynchronous LLM processing.
```sql
CREATE TABLE interaction (
  interaction_id UUID PRIMARY KEY,
  user_account_id INT REFERENCES user_account(id) ON DELETE CASCADE,
  coreling_id INT REFERENCES coreling(id) ON DELETE CASCADE,
  status VARCHAR(20) DEFAULT 'processing',
  response TEXT,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);
```

---

## **5. REST API Design**
### **5.1. Authentication Endpoints (`/api/auth`)**
| Method | Endpoint | Description |
|--------|---------|-------------|
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Authenticate and return JWT |

---

### **5.2. Coreling Endpoints (`/api/coreling`)**
| Method | Endpoint | Description |
|--------|---------|-------------|
| `GET` | `/api/coreling/{userAccountId}` | Retrieve Coreling state |
| `POST` | `/api/coreling/{userAccountId}/interact` | Initiate an asynchronous AI conversation |

---

### **5.3. Interaction Endpoints (`/api/interaction`)**
| Method | Endpoint | Description |
|--------|---------|-------------|
| `GET` | `/api/interaction/{interactionId}/status` | Check LLM response status |

---

## **6. Kafka Messaging Design**
### **6.1. Topics**
| Topic | Description |
|--------|-------------|
| `coreling.interactions` | Handles interaction requests (published by Spring Boot) |

---

## **7. Observability (Logging & Monitoring)**
### **7.1. Logging**
- Use **Logback** for structured logs.
- Store logs in JSON format for easy parsing.
  
### **7.2. Metrics (Prometheus)**
- Monitor:
  - API request latency.
  - Kafka message processing time.
  - Database query performance.

---

## **8. Security Implementation**
### **8.1. JWT Authentication**
- Users will authenticate via `POST /api/auth/login`.
- JWT tokens will be required for all secured endpoints.

### **8.2. Role-Based Access Control**
- Certain admin-only actions will be protected.