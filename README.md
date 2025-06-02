# CyberCore Companion MVP Design Document (IN PROGRESS)
Project Name: CyberCore Companion
Type: AI-Driven Pet Simulator (MVP)
Purpose: Demonstrate backend architecture, async AI integration, and DevOps/observability skills with tools like Spring Boot, Kafka, Prometheus, and Kubernetes.
________________


1. Overview
1.1. Objective
Build an MVP showcasing:
* Backend Engineering: REST APIs, event-driven microservices, and database design.
* AI Integration: Local LLM-driven interactions with RAG (Retrieval-Augmented Generation) using Spring AI.
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
                   |   Client (SwaggerUI)  |
                   +----------+-----------+
                              |
                    REST API (Spring Boot)
                              |
        +---------------------+----------------------+
        |                                            |
+-------v--------+                          +--------v--------+
|  User/Coreling |                          |   Event Broker  |
|  Management    |                          |   (Kafka)       |
|  (Spring Boot) |                          +--------+--------+
+-------+--------+                                   |
        |                                  +---------v---------+
+-------v--------+                        |  AI Interaction    |
|  PostgreSQL    |                        |  Service (Spring   |
|  (pgvector)    |                        |  AI + Ollama + RAG)|
+-------+--------+                        +---------+---------+
        |                                           |
        +----------------------------+--------------+
                                     |
                             +-------v--------+
                             | Observability  |
                             | (Prometheus,   |
                             |  Grafana, Logs)|
                             +----------------+
```

2.2. Component Breakdown
1. Spring Boot API:
   * REST endpoints for user/Coreling management.
   * Integrated with Kafka for async AI processing.
2. AI Interaction Service:
   * Kafka consumer processing interactions.
   * RAG workflow using pgvector for memory-based responses.
   * Local LLM (Ollama via Spring AI) for generating conversational feedback.
3. Database & Caching:
   * pgvector (PostgreSQL extension): Stores Coreling state + vector embeddings.
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
   * Recent interactions stored as vectors in pgvector (PostgreSQL).
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
* Backend: Java 17, Spring Boot 3, Spring Kafka, Spring AI.
* AI: Local LLM (Ollama via Spring AI), pgvector.
* Database: PostgreSQL (pgvector extension for embeddings).
* Event Broker: Kafka.
* Observability: Prometheus, Grafana, ELK Stack (logs).
* DevOps: Docker, Kubernetes (minikube), GitHub Actions.
4.2. Strategic AWS Additions (Optional)
* EKS Deployment: If time allows, deploy to EKS with a Terraform/Helm setup.
* S3 Bucket: Store LLM model weights.
________________


5. Roadmap
Phase 1 (MVP – 2 Weeks)
* Core Features:
   * Implement Spring Boot APIs + Kafka async flow.
   * Integrate local LLM with pgvector for RAG via Spring AI.
   * Set up Prometheus/Grafana monitoring.
* Documentation:
   * SwaggerUI for API testing.
   * GitHub Actions CI/CD (build/test Docker images).
Phase 2 (Observability & Polish – 1 Week)
* Enhance Monitoring:
   * Grafana dashboards for Kafka lag, API error rates.
   * Log aggregation with ElasticSearch.
* Security:
   * Add API key authentication via Spring Security.
Phase 3 (AWS Expansion – Post-Certification)
* Deploy to EKS: Add Terraform/EKS configuration files.
________________
