# GeminiMind: Project Context & Memory

**Project:** Shoply Backend (E-commerce Monolith)
**Target Level:** Senior Developer / Software Architect
**Current Date:** Saturday, 10 January 2026
**Current Branch:** `feature/phase1-swagger`

---

## 1. Project Status
We are transforming a "Tutorial-grade" Spring Boot app into a "Production-grade" Enterprise system.

### **Phase 1: Professionalizing the Core (In Progress)**
- [x] **Git Setup:** Initialized `main`, `develop`, and Feature branches.
- [x] **Security:** Secured `application.properties` using `.env` variables.
- [x] **Documentation:** Added `springdoc-openapi` (Swagger UI).
- [x] **Config:** Created `SwaggerConfig.java` to handle JWT Auth in UI.
- [x] **Fixes:** Enabled public access to `/api/public/**` in `WebSecurityConfig`.
- [ ] **Next:** Polish Swagger UI (Add `@Tag`, `@Operation` annotations).

### **Future Phases (Roadmap)**
- **Phase 2 (Quality Gate):** Integration Tests (Testcontainers), Global Exception Handling (RFC 7807), Flyway DB Migration.
- **Phase 3 (Scale):** Redis Caching, Optimistic Locking (Concurrency).
- **Phase 4 (DevOps):** Docker, CI/CD, Event-Driven Architecture.

---

## 2. Technical Decisions
- **Architecture:** Modular Monolith (avoiding Microservices for now).
- **DB Management:** Moving from `ddl-auto=update` to **Flyway** (Phase 2).
- **Testing:** shifting to **Testcontainers** for real DB integration tests.

## 3. Immediate Next Actions (Session Restart)
1.  **Resume Phase 1:** Open `AuthController.java` and add Swagger annotations (`@Tag`, `@Operation`) to make the docs professional.
2.  **Verify:** Ensure `deleteProduct` returns a DTO, not a String.
3.  **Merge:** Pull Request `feature/phase1-swagger` -> `develop`.
4.  **Start Phase 2:** Initialize Flyway.

## 4. Commands Reference
- **Run App:** IntelliJ (Maven Profile) or `mvn spring-boot:run`
- **Swagger URL:** `http://localhost:8080/swagger-ui/index.html`
- **Git Push:** `git push origin feature/phase1-swagger`
