# Shoply Backend – Multi-Vendor E-Commerce REST API

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg?style=flat-square&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot 3.4.2](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen.svg?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Security 6](https://img.shields.io/badge/Spring%20Security-6-green.svg?style=flat-square&logo=springsecurity)](https://spring.io/projects/spring-security)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![Docker Compose](https://img.shields.io/badge/Docker-Compose-2496ED.svg?style=flat-square&logo=docker)](https://www.docker.com/)
[![CI/CD Pipeline](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-blue.svg?style=flat-square&logo=githubactions)](https://github.com/features/actions)

> An Enterprise-grade, multi-vendor e-commerce platform REST API engineered with **Java 17** and **Spring Boot 3.4.2**. Built with clean layered architecture, hybrid JWT security (HTTP-Only Cookies + Bearer headers), multi-vendor role-based access control, atomic order checkout transactions, and complete unit/integration test coverage.

---

## System Architecture & Engineering Highlights

* **Multi-Vendor Layered Architecture:** Engineered a multi-vendor e-commerce RESTful backend using **Java 17**, **Spring Boot 3.4**, and **PostgreSQL**, implementing an N-Tier Layered Architecture (`Controller` ➔ `Service` ➔ `Repository`) to enforce strict separation of concerns and DTO decoupling.
* **Hybrid JWT Security & Token Revocation:** Designed a hybrid authentication pipeline supporting **HTTP-Only JWT Cookies** and Bearer headers to mitigate XSS attacks; implemented stateful token revocation in `AuthTokenFilter` via database-persisted logout timestamp verification (`lastLogoutDate`).
* **Atomic Transaction Management:** Orchestrated checkout workflows using Spring **`@Transactional`** boundaries, executing inventory stock reduction, payment metadata logging, order item snapshotting, and cart purging within a single rollback-safe ACID database transaction.
* **API Resilience & Defensive Error Handling:** Built centralized exception handlers using **`@RestControllerAdvice`** to catch constraint violations, access denials, and type mismatches, preventing security error masking and delivering uniform JSON error payloads (`400`, `403`, `404`, `409`).
* **API Contract Governance & OpenAPI 3:** Standardized API documentation using **SpringDoc OpenAPI 3**, configuring 3 role-filtered Swagger UI portals (*Customer*, *Merchant*, *Admin*) with schema information hiding (`READ_ONLY` IDs) and pre-filled execution payloads.
* **Containerization & CI/CD Pipeline:** Containerized application services using **Docker & Docker-Compose** (PostgreSQL + Spring App) and configured automated **GitHub Actions CI/CD** integration workflows executing 15 unit and MockMvc integration tests on an H2 in-memory database.

---

## System Architecture & Data Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                      Client Applications                        │
│            (React Frontend, Mobile Apps, Postman)               │
└────────────────────────────────┬────────────────────────────────┘
                                 │ HTTP / JSON
                                 v
┌─────────────────────────────────────────────────────────────────┐
│                   Spring Security Filter Chain                  │
│       (WebSecurityConfig ➔ AuthTokenFilter ➔ AuthEntryPoint)    │
└────────────────────────────────┬────────────────────────────────┘
                                 │ Principal / Context
                                 v
┌─────────────────────────────────────────────────────────────────┐
│                        Controller Layer                         │
│     (AuthController, CategoryController, ProductController, etc)│
└────────────────────────────────┬────────────────────────────────┘
                                 │ DTO Objects
                                 v
┌─────────────────────────────────────────────────────────────────┐
│                         Service Layer                           │
│        (ProductServiceImpl, OrderServiceImpl, CartServiceImpl)   │
└────────────────────────────────┬────────────────────────────────┘
                                 │ Entities / Repositories
                                 v
┌─────────────────────────────────────────────────────────────────┐
│                   Persistence Layer (JPA / DB)                  │
│               (PostgreSQL Production / H2 Testing)              │
└─────────────────────────────────────────────────────────────────┘
```

---

## Quick Start & Installation

### Option 1: Run with Docker Compose (Recommended)
No local Java or PostgreSQL installation required.

```bash
# Clone the repository
git clone https://github.com/rahulrai17/shoply-backend.git
cd shoply-backend

# Launch multi-container stack (App + PostgreSQL DB)
docker-compose up --build
```
* **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
* **PostgreSQL Port:** `5432`

---

### Option 2: Run Locally (Maven Wrapper)

#### Prerequisites
* Java 17+

```bash
# Run Unit & Integration Test Suite
./mvnw test        # Linux / Mac
.\mvnw.cmd test    # Windows PowerShell

# Start Spring Boot Server locally
./mvnw spring-boot:run
```

---

## Default Seed Credentials

Seed data is initialized on boot via `CommandLineRunner`:

| Role | Username | Password | Access Rights |
| :--- | :--- | :--- | :--- |
| **Customer** | `user1` | `password1` | Browse, Cart, Address, Checkout (`ROLE_USER`) |
| **Seller** | `seller1` | `password2` | Manage own products & inventory (`ROLE_SELLER`) |
| **Admin** | `admin` | `adminPass` | Full system oversight & Category CRUD (`ROLE_ADMIN`) |

---

## Git Workflow & Contribution Rules for Developers

To maintain clean code quality and build stability, direct pushes to `main` and `develop` branches are **strictly prohibited by GitHub Branch Protection Rules**. All contributions must be made via Pull Requests (PRs).

### 🛠️ Developer Workflow & Branching Strategy

1. **Clone & Checkout Feature Branch:**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/your-feature-name
   ```

2. **Branch Naming Conventions:**
   * Features: `feature/short-description` (e.g. `feature/stripe-integration`)
   * Bug Fixes: `bugfix/short-description` (e.g. `bugfix/cart-null-pointer`)
   * Docs: `docs/short-description` (e.g. `docs/api-specs`)

3. **Local Testing Requirements:**  
   Before pushing, developers **must** run local compilation and test suites to verify 0 failures:
   ```bash
   .\mvnw.cmd clean package    # Windows
   ./mvnw clean package        # Linux / Mac
   ```

4. **Push & Open Pull Request (PR):**
   ```bash
   git add -A
   git commit -m "feat: description of changes"
   git push origin feature/your-feature-name
   ```
   * Open a **Pull Request (PR)** on GitHub targeting the `develop` branch.
   * **GitHub Actions CI/CD** will execute tests automatically. Once the status check displays **GREEN (✅)** and a reviewer approves, the PR can be merged into `develop`.

---

## Complete Project Documentation (`/docs`)

Detailed architectural documentation is available in the [`docs/`](./docs) directory:

1. [`01_introduction.md`](./docs/01_introduction.md) — Project Vision & Multi-Vendor Business Domain
2. [`02_module_overview_and_architecture.md`](./docs/02_module_overview_and_architecture.md) — Architectural Context & Package Structure
3. [`03_requirements.md`](./docs/03_requirements.md) — Functional Requirements (FR-1 to FR-6) & Permission Matrix
4. [`04_data_schema_and_er_model.md`](./docs/04_data_schema_and_er_model.md) — Mermaid ER Diagram & Database Field Specs
5. [`05_api_specification.md`](./docs/05_api_specification.md) — Complete REST API Catalog & Client Flows
6. [`06_end_to_end_sequence_diagrams.md`](./docs/06_end_to_end_sequence_diagrams.md) — Sequence Diagrams for ALL API Workflows
7. [`07_system_design_and_concepts.md`](./docs/07_system_design_and_concepts.md) — Tech Stack Rationale & System Design Patterns
8. [`08_testing_guide.md`](./docs/08_testing_guide.md) — Exhaustive API Testing Guide (All 26 APIs)

---

## Postman Collection
Import [`shoply_postman_collection.json`](./shoply_postman_collection.json) directly into Postman to test pre-configured API requests.
