# GreenDrop Backend API

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot)
![Gradle](https://img.shields.io/badge/Gradle-Build-02303A?logo=gradle)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?logo=docker)
![Redis](https://img.shields.io/badge/Redis-Cache%20%2B%20RateLimit-DC382D?logo=redis)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203-85EA2D?logo=swagger)

---

## 🚀 Overview

GreenDrop is a backend REST API built with **Spring Boot** and **Java 17**, designed to support a scalable and secure platform for **product collection, routing, and user location management**.

### Key Technologies

| Technology          | Main Use                                |
| :------------------ | :-------------------------------------- |
| **Java 17+**        | Primary development language            |
| **Spring Boot 3.x** | Framework for rapid API development     |
| **Gradle**          | Build automation tool                   |
| **MySQL**           | Relational database                     |
| **Redis**           | Cache and rate-limiting storage         |
| **Docker**          | Development and deployment environment  |
| **Swagger/OpenAPI** | Interactive API documentation           |
| **Flyway**          | Database migration management           |
| **MapStruct**       | Mapping DTOs to entities and vice versa |
| **Lombok**          | Boilerplate code reduction              |

---

## 🏗 Architecture Highlights

* **Controller Layer** — REST endpoints and request validation.
* **Service Layer** — Business rules and orchestration.
* **Repository Layer** — Database access (JPA / Hibernate).
* **DTOs** — Strict input/output contracts.
* **Explicit Domain Behaviors** — Avoids anemic models.
* **Presenter + Policy Pattern** — Enriched API responses and action control.
* **Global Exception Handling** — Standardized API errors.
* **Spring Security** — Authentication and authorization filter chain.

---

## 🔐 Authentication and Security

* JWT-based authentication (access + refresh tokens)
* HttpOnly cookies for refresh tokens
* Refresh token rotation
* Logout invalidation via Redis
* Role-based access control (`USER`, `ADMIN`, `COLLECTOR`)
* Rate limiting with Redis

### Login Response Example

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 3600,
  "user": {
    "id": "c667c7bf-bfac-4b10-96be-0ec024426e9e",
    "name": "Fabiano Teste",
    "email": "fabiano22324@greendrop.com",
    "role": "USER",
    "cep": "37517-000",
    "points": 0
  }
}
```

---

## 🌍 Error Handling and Validation

All endpoints follow a unified error response structure:

```json
{
  "status": 400,
  "code": "VALIDATION_FAILED",
  "message": "Falha na validação.",
  "path": "/api/products",
  "timestamp": "2026-01-22T04:38:46.680954902",
  "errors": {
    "title": "Title is required",
    "email": "Invalid email format"
  }
}
```

---

## 📦 Products Module

### Key Rules

* **Creation:** Only `USER` and `ADMIN` roles can create products. Collectors **cannot create**.
* **Listing:**

    * `/api/products/me` — lists products belonging to the authenticated user (`USER`/`ADMIN`).
    * `/api/products` — lists products available to collectors.
* **Editing/Deleting:** Only owner or `ADMIN` can modify products.
* **Claim / Unclaim Lifecycle:** Only collectors can claim. Owner cannot claim own product.
* **Action Policy:** Flags (`canEdit`, `canDelete`, `canClaim`, `canUnclaim`) guide frontend actions.

### Example: Create Product (POST /api/products)

```json
{
  "title": "Produto Teste",
  "description": "Descrição do produto",
  "weightKg": 0.5,
  "quantity": 10,
  "category": "COOKING_OIL"
}
```

**Response (201 Created)**

```json
{
  "id": "ae460093-36ca-4781-91f0-3bc3a740ee7e",
  "title": "Produto Teste",
  "description": "Descrição do produto",
  "weightKg": 0.5,
  "quantity": 10.0,
  "category": "COOKING_OIL",
  "status": "PENDING",
  "images": [],
  "postedBy": "c667c7bf-bfac-4b10-96be-0ec024426e9e",
  "actions": {
    "canEdit": true,
    "canDelete": true,
    "canClaim": false,
    "canUnclaim": false
  }
}
```

### Example: List Products (GET /api/products)

```json
{
  "meta": { "page": 0, "size": 20, "totalElements": 1, "totalPages": 1, "hasNext": false, "hasPrevious": false },
  "data": [
    {
      "id": "ae460093-36ca-4781-91f0-3bc3a740ee7e",
      "title": "Produto Teste",
      "category": "COOKING_OIL",
      "status": "PENDING",
      "actions": {
        "canEdit": true,
        "canDelete": true,
        "canClaim": true,
        "canUnclaim": false
      }
    }
  ]
}
```

### Claim / Unclaim Rules

| Action  | Rules                                                                                                 |
| ------- | ----------------------------------------------------------------------------------------------------- |
| Claim   | Only collectors can claim, cannot claim own product, status must be `PENDING` and not already claimed |
| Unclaim | Only collector who claimed can unclaim, status must be `ASSIGNED`                                     |

### Example: Claim Product (PATCH /api/products/{id}/claim)

**Success (200 OK)**

```json
{
  "id": "ae460093-36ca-4781-91f0-3bc3a740ee7e",
  "status": "ASSIGNED",
  "claimedBy": "collector-user-id",
  "actions": {
    "canEdit": false,
    "canDelete": false,
    "canClaim": false,
    "canUnclaim": true
  }
}
```

**Error: Owner tries to claim (403 Forbidden)**

```json
{
  "status": 403,
  "code": "PRODUCT_OWNER_CANNOT_CLAIM",
  "message": "Owner cannot claim own product"
}
```

---

## 📍 User Location Module

* **Endpoint:** `/api/users/me/location`
* **Create/Update:** Authenticated users can set latitude and longitude.
* **Get Location:** Returns current location or `204 No Content` if none.

**Response Example (200 OK)**

```json
{
  "latitude": -25.4242,
  "longitude": -45.4584,
  "createdAt": "2026-01-22T04:37:28.455669Z",
  "updatedAt": "2026-01-22T04:37:28.455669Z"
}
```

---

## 🗺 Routes Module (In Development)

* Route creation, stops, and completion
* Planned integration with product collection workflow

---

## ⚡ Pagination Standard

All listing endpoints return paginated responses with metadata:

```json
{
  "meta": { "page": 0, "size": 20, "totalElements": 150, "totalPages": 8, "hasNext": true, "hasPrevious": false },
  "data": [ { "id": "...", "title": "Example Product", "category": "PLASTIC", "status": "PENDING" } ]
}
```

---

## 🛠 How to Run (Docker)

```bash
docker compose up --build
```

Services included:

* API (Spring Boot)
* MySQL
* Redis

---

## 📘 API Documentation

[Swagger UI](http://localhost:8080/swagger-ui.html)

---

## 🛣 Roadmap

### ✅ Current State

* Standardized pagination
* Soft delete for Users and Products
* Role-based access control enforced
* Products module with claim/unclaim lifecycle
* User location management

### Next Steps

* Expose product status history
* Improve collector route optimization
* Fine-grained permissions
* Add metrics and monitoring
* Expand automated test coverage

---

## 📄 License

MIT
