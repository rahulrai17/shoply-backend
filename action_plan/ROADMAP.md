# Transformation Roadmap: Shoply Backend

**Goal:** Elevate the codebase from a standard tutorial project to a production-grade, architecturally robust system suitable for a Senior Developer/Architect portfolio.

**Estimated Timeline:** ~2 Weeks (Part-time)

---

## Phase 0: Immediate Cleanup & Standardization (Day 1)
**Focus:** Fix "Junior" mistakes and set a professional baseline.

1.  **Refactor Magic Strings:**
    *   Create `OrderConstants.java` and `AppRole.java` (Enum).
    *   Replace hardcoded "Order Accepted !" and "ROLE_USER" strings.
2.  **API Documentation (OpenAPI/Swagger):**
    *   Add `springdoc-openapi-starter-webmvc-ui`.
    *   Configure `SwaggerConfig` to expose JWT Auth options in the UI.
3.  **Standardize API Responses:**
    *   Ensure `CartController.deleteProduct` returns a JSON object (DTO), not a plain String.

## Phase 1: Professionalizing the Core (Days 2-3)
**Focus:** Database integrity and production readiness.

1.  **Database Migration (Flyway):**
    *   Disable `ddl-auto`.
    *   Create initial migration script `V1__init.sql` capturing the current schema.
    *   Implement `V2__...` scripts for any future schema changes.
2.  **Global Exception Handling 2.0:**
    *   Implement RFC 7807 (Problem Details for HTTP APIs).
    *   Ensure all exceptions (Auth, Validation, Business) return a consistent JSON structure.

## Phase 2: The "Senior" Quality Gate (Days 4-7)
**Focus:** Testing and Reliability. **Crucial for credibility.**

1.  **Integration Testing (Testcontainers):**
    *   Set up `AbstractIntegrationTest` base class with a specialized PostgreSQL container.
    *   Write "Happy Path" tests for: `OrderService.placeOrder` and `AuthService.register`.
2.  **Unit Testing:**
    *   Increase Service layer coverage to >80% using Mockito.
3.  **Static Analysis:**
    *   Add `Spotless` or `Checkstyle` Maven plugin to enforce Google Java Style.

## Phase 3: Architecting for Scale (Days 8-10)
**Focus:** Handling high concurrency and performance.

1.  **Concurrency Control (Optimistic Locking):**
    *   Add `@Version` field to `Product` entity.
    *   Handle `ObjectOptimisticLockingFailureException` in `OrderService` (e.g., retry or fail gracefully).
2.  **Caching (Redis):**
    *   Spin up a Redis container.
    *   Annotate `getAllProducts` and `getCategories` with `@Cacheable`.
    *   Implement `@CacheEvict` on Admin updates.

## Phase 4: Async & Deployment (Days 11-13)
**Focus:** Decoupling and DevOps.

1.  **Event-Driven Notification:**
    *   Define `OrderPlacedEvent`.
    *   Create `OrderEventListener` to simulate Email sending (log output).
    *   *Optional:* Move this to RabbitMQ if time permits.
2.  **Dockerization:**
    *   Create `Dockerfile` (Multi-stage build).
    *   Create `docker-compose.yml` (App + Postgres + Redis).
3.  **CI/CD Pipeline (GitHub Actions):**
    *   Build workflow: Checkout -> Test (with Testcontainers) -> Build Image.

---

## Showcase Strategy
*   **README:** Add System Architecture Diagram (Mermaid.js).
*   **Demo:** Host live Swagger UI on a free cloud tier.
*   **Artifacts:** Include Test Coverage Report screenshot in the repo.
