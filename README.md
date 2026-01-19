# GreenDrop Backend

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot)
![Gradle](https://img.shields.io/badge/Gradle-Build-02303A?logo=gradle)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?logo=docker)
![Redis](https://img.shields.io/badge/Redis-Cache%20%2B%20RateLimit-DC382D?logo=redis)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203-85EA2D?logo=swagger)

------------------------------------------------------------------------

## 📦 Overview

**GreenDrop** is a backend REST API built with **Spring Boot + Java 17**, designed to support a scalable and secure platform for **product collection, routing and user location management**.

Main goals of the project:

- Secure authentication with JWT (access + refresh tokens)
- Clear separation of responsibilities (Users, Products, Routes, Locations)
- Internationalized error handling (i18n)
- Redis-backed performance and security features
- Docker-first development experience

------------------------------------------------------------------------

## 🏗 Architecture Highlights

The project follows a **clean, layered architecture**, aligned with real-world backend standards:

- Controller layer — REST endpoints & request validation
- Service layer — business rules and orchestration
- Repository layer — database access (JPA / Hibernate)
- DTOs — strict input/output contracts
- Explicit domain behaviors (avoid anemic models)
- Presenter + Policy pattern — enriched API responses
- Global exception handling — standardized API errors
- Spring Security filter chain — authentication & authorization

------------------------------------------------------------------------

## 🔐 Authentication & Security

Authentication is implemented using **JWT with access and refresh tokens**.

- Stateless authentication
- Refresh token rotation
- HttpOnly cookies
- Redis-backed logout invalidation

------------------------------------------------------------------------

## 🌍 Internationalization (i18n)

The API supports **internationalized error messages** using Spring's `MessageSource`.

Supported locales:

- pt_BR
- en_US
- es_ES

------------------------------------------------------------------------

## 📦 Products

The Products module supports:

- Product creation by authenticated users
- Ownership-based access control
- Listing products belonging to the logged-in user
- Claim / Unclaim lifecycle
- Action-based UI support via policies

------------------------------------------------------------------------

## 🔄 Product Claim / Unclaim Lifecycle

Products can be claimed and unclaimed by collectors following strict authorization and domain rules.

### Claim rules

- Only collectors can claim
- Owner cannot claim own product
- Product must be `PENDING`
- Product must not be already claimed

### Unclaim rules

- Only the collector who claimed can unclaim
- Product must be `ASSIGNED`

------------------------------------------------------------------------

## 🏷 Product Action Policy

The `ProductActionPolicy` is a read-only object embedded in product responses to inform the frontend which actions are allowed.

Available flags:

- `canEdit`
- `canDelete`
- `canClaim`
- `canUnclaim`

This ensures frontend consistency without leaking domain logic.

------------------------------------------------------------------------

## 📍 User Location

The User Location module allows authenticated users to register and manage their current geographic position.

### Features

- Create or update the authenticated user's location
- Retrieve the current user's location
- Dedicated entity (`UserLocation`) separated from `User`
- Explicit domain method for coordinate updates
- Clean mapping using MapStruct
- Debug-level logging when location is not registered

### Design decisions

- Location data is not stored directly in the User entity
- Updates are performed via explicit domain behavior
- No direct field mutation from controllers
- Read operations return `204 No Content` when no location exists

This approach improves maintainability and avoids unnecessary coupling between core user data and geolocation concerns.

------------------------------------------------------------------------

## ⚡ Rate Limiting

Redis-backed rate limiting with configurable windows and limits.

------------------------------------------------------------------------

## 🐳 Docker

Full Docker Compose environment:

- API
- MySQL
- Redis

Run:

```bash
docker compose up --build
```

------------------------------------------------------------------------

## 📘 API Documentation

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

------------------------------------------------------------------------

## 🛣 Roadmap

### ✅ Current State
- Pagination implemented on core list endpoints
- Soft delete enabled for Users and Products
- Role-based access control enforced via Spring Security
- Domain-driven authorization and rule validation
- **User location management with dedicated module**

### Next Steps
- Standardize pagination across all endpoints
- Expose product status history for auditing
- Improve collector route optimization
- Introduce fine-grained permissions
- Add metrics and monitoring
- Expand automated test coverage

------------------------------------------------------------------------

## 📦 Modules

### Users
- User registration, login, and profile management
- Role-based access control

### Products
- Product creation, listing, claim/unclaim lifecycle
- Product action policy flags for frontend consistency

### Routes
- Route creation, route stops, route completion

### User Location
- Create or update current user location (latitude & longitude)
- Get current user location
- Validation of latitude/longitude ranges
- Audit timestamps (`createdAt`, `updatedAt`)
- Standardized error handling with i18n and HTTP codes

------------------------------------------------------------------------

## 📄 License

MIT

