# GreenDrop Backend

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk)
![Spring
Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot)
![Gradle](https://img.shields.io/badge/Gradle-Build-02303A?logo=gradle)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?logo=docker)
![Redis](https://img.shields.io/badge/Redis-Cache%20%2B%20RateLimit-DC382D?logo=redis)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203-85EA2D?logo=swagger)

------------------------------------------------------------------------

## 📦 Overview

**GreenDrop** is a backend REST API built with **Spring Boot + Java 17**, designed to support a scalable and secure platform for product collection and routing management.

Main goals of the project:

- Secure authentication with JWT (access + refresh)
- Clear separation of responsibilities (Users, Products, Routes)
- Internationalized error handling (i18n)
- Redis-backed performance and security features
- Docker-first development experience

------------------------------------------------------------------------

## 🏗 Architecture Highlights

The project follows **clean, layered architecture**, aligned with real-world backend standards:

- **Controller layer** — REST endpoints & request validation
- **Service layer** — business rules and orchestration
- **Repository layer** — database access (JPA / Hibernate)
- **DTOs** — strict input/output contracts
- **Global exception handling** — standardized API errors
- **Spring Security filter chain** — authentication & authorization

Key architectural decisions:

- Stateless authentication with JWT
- Redis as a shared infrastructure dependency
- Clear separation between domain errors and HTTP concerns

------------------------------------------------------------------------

## 🔐 Authentication & Security

Authentication is implemented using **JWT with access and refresh tokens**.

### Access Token
- Short-lived
- Sent via `Authorization: Bearer <token>`
- Used to authorize protected endpoints

### Refresh Token
- Stored in **HttpOnly cookie**
- Automatically rotated on refresh
- Blacklisted on logout
- Protected against XSS access

### Supported flows
- Login
- Refresh token rotation
- Logout with token invalidation
- Endpoint-level authorization

------------------------------------------------------------------------

## 🌍 Internationalization (i18n)

The API supports **internationalized error messages** using Spring’s `MessageSource`.

Highlights:

- Error messages resolved via `messages_*.properties`
- Locale resolved from request context
- Fallback mechanism for missing keys
- Unified error payload structure

Example:

```json
{
  "status": 401,
  "code": "INVALID_CREDENTIALS",
  "message": "Invalid credentials.",
  "path": "/api/auth/login",
  "timestamp": "2025-12-14T10:23:56"
}
```

Supported locales:

- pt_BR

- en_US

- es_ES

------------------------------------------------------------------------

### 👤 Users

User management includes:

- User registration
- Authentication
- Profile retrieval (`/me`)
- Profile update
- Password change
- Role-based access control

Security rules:
- Users can only access or modify their own data
- Admin-only operations enforced via Spring Security

------------------------------------------------------------------------

### 📦 Products

The Products module supports:

- Product creation by authenticated users
- Ownership-based access control
- Listing products belonging to the logged-in user (`/api/products/me`)
- Image validation
- Category-based restrictions

Current guarantees:
- A user cannot modify or delete another user's product
- Proper HTTP semantics (401 vs 403)
- Validation errors mapped to structured responses

------------------------------------------------------------------------

## 🔄 Product Claim / Unclaim Lifecycle

Products can be claimed and unclaimed by collectors following strict
authorization and domain rules.

### 📥 Claim a Product

A product can be claimed when:

- The authenticated user has role **COLLECTOR**
- The collector is **not the product owner**
- The product status is **PENDING**
- The product is **not already claimed**
- The product is **not linked to a RouteStop**

#### Flow:
1. Authenticate as COLLECTOR
2. Request claim endpoint
3. Authorization is validated (role + ownership)
4. Domain rules validate product state
5. Product status changes from `PENDING` → `CLAIMED`
6. Collector is assigned to the product
7. Status change is recorded in history

---

### 📤 Unclaim a Product

A product can be unclaimed when:

- The authenticated user is the **current collector**
- The product status is **CLAIMED**
- The product is currently claimed
- The product is **not linked to a RouteStop**

#### Flow:
1. Authenticate as COLLECTOR
2. Request unclaim endpoint
3. Authorization validates ownership of the claim
4. Domain rules validate product state
5. Product status changes from `CLAIMED` → `PENDING`
6. Collector is removed from the product
7. Status change is recorded in history

---

### 🧠 Design Notes

- Status transitions are centralized in `ProductService.changeStatus`
- Authorization and domain rules are separated:
    - `ProductAuthorization`: WHO can perform the action
    - `ProductRules`: WHEN the action is allowed
- Status history is tracked for auditing and future reporting


------------------------------------------------------------------------
### 🚚 Collector Routes

Collector Routes manage collection workflows:

- Route creation with validation rules
- Assignment to collectors
- Controlled lifecycle (start / finish)
- Stop ordering validation
- Domain-driven error handling

All route rules are enforced at the service layer to ensure consistency.

------------------------------------------------------------------------

## ⚡ Rate Limiting

The API includes **Redis-backed rate limiting**.

Characteristics:

- Distributed (safe for horizontal scaling)
- Per-user request limits
- Configurable window and max requests
- Automatic blocking with HTTP `429`

Example response:

```json
{
  "status": 429,
  "code": "USER_TOO_MANY_ATTEMPTS",
  "message": "Too many attempts. Try again in a few minutes."
}
```
------------------------------------------------------------------------

## 🐳 Docker & Environment

The entire stack is Dockerized for consistency.

Services:

- Spring Boot API
- MySQL 8
- Redis

Start everything:

```
docker compose up --build
```

------------------------------------------------------------------------

## 📘 API Documentation

The API is fully documented using **Swagger / OpenAPI 3**.

Access Swagger UI:

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Documentation includes:

- Endpoint descriptions
- Request/response schemas
- Error responses
- Authentication requirements

------------------------------------------------------------------------

## 🌱 Environment Variables

Create `.env` file:

    SERVER_PORT=8080

    # MySQL
    SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/greendrop
    SPRING_DATASOURCE_USERNAME=root
    SPRING_DATASOURCE_PASSWORD=123456

    # JWT
    JWT_SECRET=your_jwt_secret
    JWT_EXPIRATION_MS=3600000
    JWT_REFRESH_EXPIRATION_MS=86400000

    # Redis
    REDIS_HOST=redis
    REDIS_PORT=6379

    # Rate Limiter
    RATE_LIMIT_MAX_REQUESTS=60
    RATE_LIMIT_WINDOW_SECONDS=60

------------------------------------------------------------------------

## 🛣 Roadmap

Planned improvements and next features:

- JWT claims for roles and permissions
- Pagination for `/me/products`
- Soft delete (logical deletion flags)
- Auditing fields (`createdAt`, `createdBy`)
- Rate limiting per user (fine-grained)
- Expanded Swagger error documentation

------------------------------------------------------------------------

## 🤝 Contributing

1.  Create a feature branch\
2.  Follow Conventional Commits\
3.  Keep documentation updated\
4.  Submit PR

------------------------------------------------------------------------

## 📄 License

MIT License
