# Experiment 9: CI/CD Pipeline with Testing & Deployment

A lightweight, practical-exam-ready application and interactive dashboard demonstrating:
- **Experiment 9.1**: Continuous Integration Pipeline Setup (JUnit 5, MockMvc, Pact, Testcontainers, GitHub Actions).
- **Experiment 9.2**: Continuous Deployment & Release Strategies (Docker Multi-stage build, Canary Traffic Distribution, Rollback, Telemetry).

---

## 1. Features & Architecture

1. **Unit Testing (JUnit 5)**
   - Tests core algorithmic methods (`CalculatorService`) in isolation using `@Test`, `@DisplayName`, `assertEquals`, and `assertThrows`.
2. **API Integration Testing (MockMvc)**
   - Verifies HTTP `GET /api/posts` controller, status code 200, JSON serialization, and response payloads without starting a network server.
3. **Contract Testing (Pact)**
   - Implements consumer-driven contract verification.
   - Demonstrates how schema drift (renamed or omitted JSON fields) fails contract validation before production releases.
4. **Real Infrastructure Testing (Testcontainers)**
   - Demonstrates spinning up real Dockerized dependencies (PostgreSQL 15) for testing.
   - Dynamically evaluates local Docker daemon availability: executes live container tests if Docker is active; skips cleanly if Docker is not installed/running.
5. **CI Automation (GitHub Actions)**
   - Configured in `.github/workflows/ci.yml`.
   - Triggers on `push` or `pull_request` to `main`, provisions JDK 17, compiles code, runs test suites, and archives reports.
6. **Containerization (Docker)**
   - Production multi-stage `Dockerfile` (Maven builder + Eclipse Temurin 17 JRE runtime).
   - Non-root user compliance, health checks, and small footprint.
7. **Continuous Deployment & Canary Release Strategy**
   - Configured in `.github/workflows/cd.yml`.
   - Distributes incoming traffic between **Stable (v1.0.0)** and **Canary (v2.0.0)** with configurable traffic splitting (10%, 25%, 50%, 100%).
   - One-click instant rollback to Stable (0% Canary) upon health anomaly.
   - Interactive live request router to verify probabilistic traffic distribution.
8. **Reports & Logs**
   - Live execution console with timestamped entries and clear functionality.

---

## 2. Requirements

- **Java JDK 17 or higher** (Tested on Java 17 and JDK 24)
- **Apache Maven 3.9+** (Located at `C:\Users\Kushaan\AppData\Local\apache-maven-3.9.6\bin\mvn.cmd`)
- *(Optional)* **Docker Desktop** for live container spin-up (dashboard automatically detects Docker status).

---

## 3. How to Run Locally

### Option A: Using `run.bat`
Double-click `run.bat` in the project root folder:
```cmd
L:\FULL STACK\experiment-9-cicd-pipeline\run.bat
```

### Option B: Using Maven Command Line
Open PowerShell or Command Prompt in `L:\FULL STACK\experiment-9-cicd-pipeline`:
```cmd
& "C:\Users\Kushaan\AppData\Local\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run
```

Once started, open your web browser at:
```
http://localhost:8089
```

---

## 4. How to Run Automated Tests

To execute all unit, integration, and contract tests from the terminal:
```cmd
& "C:\Users\Kushaan\AppData\Local\apache-maven-3.9.6\bin\mvn.cmd" test
```

Test reports are generated in:
```
target/surefire-reports/
```

---

## 5. College Viva Voce Questions & Answers

1. **What is the difference between Continuous Integration (CI) and Continuous Deployment (CD)?**
   - *CI* automates the integration of code changes into a shared repository followed by automated building and testing.
   - *CD* extends CI by automatically deploying all code changes that pass the CI stage directly into production.

2. **Why use Testcontainers instead of H2 in-memory database?**
   - In-memory databases like H2 have different SQL dialects and lack vendor-specific features (e.g., PostgreSQL JSONB, array types, specific triggers). Testcontainers ensures integration tests execute against the exact same engine used in production.

3. **What is Consumer-Driven Contract Testing (Pact)?**
   - In microservices, the consumer defines an explicit contract of what request it sends and what JSON fields it expects. The provider validates its response against this contract. This prevents breaking changes when backend teams alter schema field names without updating the consumer.

4. **Explain Canary Deployment vs Blue-Green Deployment.**
   - *Blue-Green*: Two identical production environments exist. 100% of user traffic is flipped from Blue to Green at once.
   - *Canary*: Both versions run simultaneously. A small percentage of traffic (e.g., 10%) is routed to the new version to measure real-world performance, error rates, and latency before gradually increasing to 100% or rolling back.

5. **How are credentials handled securely in GitHub Actions?**
   - Passwords and API tokens are never written into workflow files. They are stored in encrypted **GitHub Secrets** and referenced via context variables like `${{ secrets.DOCKER_HUB_TOKEN }}`.
