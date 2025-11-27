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

**GreenDrop** is a backend REST API built with **Spring Boot + Java
17**, supporting:

-   JWT Authentication (access + refresh tokens)
-   HttpOnly refresh-token cookie
-   User roles (USER, COLLECTOR, ADMIN)
-   Product management module
-   Redis caching for:
    -   User profiles (User Cache)
    -   Token blacklist
    -   Rate limiter buckets
-   Global Rate Limiting user
-   Dockerized development environment

------------------------------------------------------------------------

## 🚀 Features

### ✔ User Cache (Redis)

-   Reduces database load
-   Cache invalidation on updates
-   Full serialization with Jackson
-   Prevents repeated queries on endpoints such as `/me` and
    `/users/{id}`

### ✔ Global Rate Limiting

-   Distributed rate limiter using Redis
-   Blocks abusive clients
-   Logs violations
-   Safe for horizontal scaling

### ✔ Secure Refresh Token

-   Stored in **HttpOnly cookie**
-   Fully compatible with Docker + Prod profile
-   Auto-regenerated on each refresh
-   Protected from JavaScript access

------------------------------------------------------------------------

## 🔐 Authentication / Authorization

### Login

`POST /api/auth/login`

``` json
{
  "email": "user@example.com",
  "password": "123456"
}
```

Response:

``` json
{
  "accessToken": "jwt_access_token"
}
```

Refresh token is provided via **HttpOnly cookie**.

------------------------------------------------------------------------

### Refresh Token

`POST /api/auth/refresh`

-   Reads refresh token from HttpOnly cookie\
-   Validates blacklist\
-   Issues a **new access token**\
-   Refresh token is rotated (new cookie each refresh)

------------------------------------------------------------------------

### Logout

`POST /api/auth/logout`

-   Removes user tokens from active token cache\
-   Adds tokens to blacklist\
-   Clears HttpOnly cookie

------------------------------------------------------------------------

## ⚡ Rate Limiting

### Global policy:

-   **X requests/minute** (configurable)
-   Based on Redis buckets

Returns **429 Too Many Requests**:

``` json
{
  "error": "Rate limit exceeded. Try again later."
}
```

------------------------------------------------------------------------

## 👤 User Endpoints

-   `GET /api/users/me`
-   `PUT /api/users/me`
-   `PUT /api/users/me/password`
-   `GET /api/users`
-   `GET /api/users/{id}`
-   `DELETE /api/users/{id}`

------------------------------------------------------------------------

## 🐳 Docker & Environment

### Start full environment

``` sh
docker compose up --build
```

Services: - Spring API\
- MySQL\
- Redis

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

## 🧪 Local Development

Run MySQL + Redis:

``` sh
docker compose up db redis
```

Run API:

``` sh
./gradlew bootRun
```

------------------------------------------------------------------------

## 📘 API Documentation

Swagger UI:

    http://localhost:8080/swagger-ui.html

------------------------------------------------------------------------

## 🤝 Contributing

1.  Create a feature branch\
2.  Follow Conventional Commits\
3.  Keep documentation updated\
4.  Submit PR

------------------------------------------------------------------------

## 📄 License

MIT License
