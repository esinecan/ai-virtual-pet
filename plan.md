## 1. Project Overview

*   **Project Name:** CyberCore Companion
*   **Project Type:** AI-Driven Pet Simulator (MVP)
*   **Project Purpose:** Demonstrate backend architecture, async AI integration, and DevOps/observability skills with tools like Spring Boot, Kafka, Prometheus, and Kubernetes.

## 2. System Architecture

### 2.1. Component Breakdown

*   **Component 1: Spring Boot API**
    *   Implement REST endpoints for user and Coreling management.
    *   Integrate with Kafka for asynchronous AI processing.
*   **Component 2: AI Interaction Service**
    *   Develop a Kafka consumer to process interactions.
    *   Implement RAG workflow using pgvector for memory-based responses.
    *   Integrate a local LLM (Ollama via Spring AI) for generating conversational feedback.
*   **Component 3: Database & Caching**
    *   Utilize pgvector (PostgreSQL extension) to store Coreling state and vector embeddings.
*   **Component 4: Observability**
    *   Set up Prometheus for metrics collection (API latency, Kafka throughput).
    *   Configure Grafana for dashboards to monitor system health.
    *   Implement logging (details to be specified, e.g., ELK Stack).
*   **Component 5: Kubernetes (Optional)**
    *   Plan for deployment on a local minikube cluster.
    *   Consider EKS deployment as a future enhancement.

## 3. Core Feature Implementation

### 3.1. Async AI Workflow
*   User sends interaction via API.
*   Message published to Kafka topic.
*   AI service consumes message and processes with RAG.
*   Coreling state is updated in PostgreSQL.
*   Implement `GET /interaction/{id}/status` endpoint (status likely stored in Redis or similar).

### 3.2. Personality via Vectors
*   Store recent interactions as vector embeddings in pgvector.
*   Utilize RAG with similarity search in pgvector to contextualize LLM responses.

### 3.3. Monitoring Setup
*   Expose metrics from Spring Boot Actuator.
*   Configure Prometheus to scrape these metrics.
*   Create Grafana dashboards to visualize key metrics.

## 4. API Endpoints (Key Examples)

### 4.1. User/Coreling Management
*   `POST /api/auth/register`
*   `POST /api/auth/login`
*   `GET /api/coreling/{userAccountId}`
*   `POST /api/coreling/{userAccountId}/interact`

### 4.2. Async Interaction Status
*   `GET /api/interaction/{interactionId}/status` (returns status: "processing/complete", and response if complete).

## 5. Technical Stack

*   **Backend:** Java 17, Spring Boot 3, Spring Kafka, Spring AI.
*   **AI:** Local LLM (Ollama via Spring AI), pgvector.
*   **Database:** PostgreSQL (with pgvector extension).
*   **Event Broker:** Kafka.
*   **Observability:** Prometheus, Grafana, ELK Stack (for logs).
*   **DevOps:** Docker, Kubernetes (minikube), GitHub Actions.

## 6. Roadmap

### 6.1. Phase 1 (MVP - Target: 2 Weeks)
*   Implement core Spring Boot APIs and Kafka asynchronous flow.
*   Integrate local LLM with pgvector for RAG using Spring AI.
*   Set up basic Prometheus/Grafana monitoring.
*   Provide SwaggerUI for API documentation and testing.
*   Configure GitHub Actions for CI/CD (build and test Docker images).

### 6.2. Phase 2 (Observability & Polish - Target: 1 Week)
*   Enhance Grafana dashboards (e.g., Kafka lag, API error rates).
*   Set up log aggregation (e.g., ELK Stack).
*   Implement API key authentication using Spring Security.

### 6.3. Phase 3 (AWS Expansion - Post-Certification)
*   Plan for EKS deployment with Terraform/Helm.
*   Consider S3 for storing LLM model weights.


## 7. Implementation Tasks (Current State to MVP)

This section outlines the necessary development tasks to bridge the gap between the current codebase and the Minimum Viable Product (MVP) described in the sections above (which mirror the project's README).

### A. Core Service Implementation

1.  **Module: `cybercore-companion-rest` (API Layer)**
    *   **Entities:**
        *   Define JPA entity for `UserAccount` (e.g., with username, password hash).
        *   Define JPA entity for `Coreling` (e.g., name, personality traits, link to UserAccount).
    *   **Repositories:**
        *   Create Spring Data JPA repositories for `UserAccount` and `Coreling`.
    *   **Services:**
        *   Implement `UserAccountService` for registration (`/api/auth/register`) and login (`/api/auth/login`) logic. Consider JWT generation on successful login.
        *   Implement `CorelingService` for Coreling creation, retrieval (`GET /api/coreling/{userAccountId}`).
        *   Implement `InteractionService` to handle `POST /api/coreling/{userAccountId}/interact`:
            *   Validate input.
            *   Create an interaction event/message.
            *   Publish the message to a Kafka topic (e.g., `coreling-interactions`).
            *   Store initial interaction status (e.g., "pending") if `GET /api/interaction/{interactionId}/status` is to be implemented.
    *   **Controllers:**
        *   Implement `AuthController` for `/api/auth/` endpoints.
        *   Implement `CorelingController` for `/api/coreling/` endpoints.
        *   Implement `InteractionController` for the `/interact` endpoint and the `/api/interaction/{interactionId}/status` endpoint.
    *   **Kafka Producer:**
        *   Configure and implement Kafka producer logic within the `InteractionService` to send messages.

2.  **Module: `cybercore-companion-llm` (AI/Worker Layer)**
    *   **Kafka Consumer:**
        *   Implement a Kafka listener/consumer for the `coreling-interactions` topic.
    *   **Entities & Repositories (pgvector):**
        *   Define JPA entity for storing interaction embeddings (e.g., `InteractionEmbedding`) including the vector field. Ensure it's compatible with pgvector and Spring AI's `PgVectorStore`.
        *   Create Spring Data JPA repository for `InteractionEmbedding`.
    *   **AI Services:**
        *   Implement `AIService` or similar:
            *   Integrate with Spring AI's Ollama client (`ChatClient`).
            *   Develop RAG (Retrieval-Augmented Generation) logic:
                *   On receiving an interaction message from Kafka:
                    1.  Generate an embedding for the incoming user interaction text.
                    2.  Query pgvector using `PgVectorStore` to find similar past interactions (for context/memory).
                    3.  Construct a prompt for the LLM, including the current interaction and retrieved context.
                    4.  Call the LLM to get a response.
                    5.  Store the new interaction and its embedding in pgvector for future context.
            *   Update Coreling state/personality (details TBD, could be simple state update or more complex vector-based personality updates).
    *   **(Optional) Kafka Producer:** If the LLM service needs to report back results or state changes via Kafka, implement a producer here.

3.  **Module: `cybercore-companion-kafka` (Shared Kafka Infrastructure)**
    *   **Message DTOs:** Define Java classes (DTOs) for messages exchanged over Kafka (e.g., `CorelingInteractionEvent`). These will be used by producers in the REST module and consumers in the LLM module.
    *   **(Optional) Configuration:** Centralized Kafka topic configurations or bean definitions if not adequately covered by Spring Boot auto-configuration in the respective modules.

### B. Database and Configuration

1.  **pgvector Setup (in `cybercore-companion-llm`):**
    *   Ensure the PostgreSQL database has the pgvector extension enabled (this is an external setup step, document it).
    *   Correct the JPA configuration in `cybercore-companion-llm/src/main/resources/application.yml`:
        *   Change `spring.jpa.properties.hibernate.dialect` from `org.hibernate.dialect.PostgreSQLDialect` to a pgvector-compatible dialect. If using `com.pgvector:pgvector-hibernate`, this would be `com.pgvector.PGVectorDialect`. Alternatively, verify if Spring AI's `PgVectorStore` auto-configuration handles this if a specific dependency (like `spring-ai-pgvector-store-spring-boot-starter`) is used and correctly configured.
    *   Remove or clarify the `com.cybercore.companion.config.PGVectorDialect` from `cybercore-companion-rest`'s `application.yml` as it's unlikely to be needed there.
2.  **Interaction Status Tracking:**
    *   Decide on and implement a storage mechanism for `GET /api/interaction/{interactionId}/status` (e.g., Redis, a new DB table, or in-memory for initial MVP).

### C. Versioning and Dependencies

1.  **Spring Boot/Kafka Version Alignment:**
    *   Standardize Spring Boot versions across all modules (e.g., to 3.1.5 as used in LLM module or latest stable).
    *   Align `spring-kafka` versions similarly.
    *   Update parent POM and module POMs accordingly.

### D. Observability

1.  **REST API Metrics (`cybercore-companion-rest`):**
    *   In `cybercore-companion-rest/src/main/resources/application.yml`, add/ensure configuration for Actuator to expose Prometheus metrics:
        ```yaml
        management:
          endpoints:
            web:
              exposure:
                include: health,info,metrics,prometheus # Add prometheus
          metrics:
            tags:
              application: cybercore-rest # Or similar app tag
        ```
2.  **Logging for ELK (Optional - Post-MVP Polish):**
    *   Plan for adding Logstash appenders to logging configurations if ELK stack integration is pursued.

### E. DevOps

1.  **Application Containerization:**
    *   Create a `Dockerfile` for `cybercore-companion-rest`.
    *   Create a `Dockerfile` for `cybercore-companion-llm`.
    *   Create a `Dockerfile` for `cybercore-companion-kafka` (if it becomes a runnable application; if it's just a library, this might not be needed, but the current structure suggests it's a separate module).
    *   Ensure Dockerfiles are optimized for Spring Boot applications (e.g., layered jars).
2.  **CI/CD (GitHub Actions):**
    *   Create a basic GitHub Actions workflow (`.github/workflows/build.yml`):
        *   Trigger on push to `main` and PRs.
        *   Set up Java environment.
        *   Run Maven build and tests (`mvn clean install`).
        *   (Optional Stage) Build Docker images for each service and push to a registry (e.g., Docker Hub, GitHub Container Registry).
3.  **Kubernetes Manifests (Local - Minikube):**
    *   Create Kubernetes YAML manifests for:
        *   `cybercore-companion-rest` (Deployment, Service).
        *   `cybercore-companion-llm` (Deployment, Service).
        *   (If applicable) `cybercore-companion-kafka` (Deployment, Service).
        *   PostgreSQL (Deployment, Service, PersistentVolumeClaim) - if not using an external instance.
        *   Kafka (Deployment, Service, PersistentVolumeClaim) - if not using an external instance.
        *   ConfigMaps for application configurations.
    *   Consider using Helm charts for easier management as a future step.
4.  **Documentation for Setup:**
    *   Update `prerequisite_containers/howto.txt` or create new documentation explaining how to run the complete system (services + backing stores) locally, ideally using Docker Compose and/or Minikube.

### F. API Security (Initial Steps for MVP)

1.  **JWT Implementation:**
    *   If implementing `UserAccountService` with JWTs:
        *   Include a lightweight JWT library (if Spring Security isn't fully set up yet, though `spring-boot-starter-security` is preferred).
        *   Protect Coreling and interaction endpoints, requiring a valid JWT.
    *   This is a stretch for initial MVP if time is very constrained but foundational for anything beyond basic testing. The README lists API key auth for Phase 2.

This detailed task list will form the basis for the actual implementation work required.
