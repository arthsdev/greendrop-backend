# GreenDrop Backend

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot)
![Gradle](https://img.shields.io/badge/Gradle-Build-02303A?logo=gradle)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?logo=docker)
![Redis](https://img.shields.io/badge/Redis-Cache%20%2B%20Tokens-DC382D?logo=redis)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203-85EA2D?logo=swagger)

---

## 📦 Overview

**GreenDrop** is a backend REST API built with **Spring Boot + Java 17**, supporting:
- User management & roles (USER, COLLECTOR, ADMIN)
- Authentication & JWT (access + refresh tokens)
- Product catalog management
- Redis caching for tokens and frequently used data
- MySQL persistence
- Dockerized environment for easy setup

---

## 🔐 Authentication / Authorization

### Login
`POST /api/auth/login`

Request Body:
```json
{
  "email": "user@example.com",
  "password": "123456"
}
```

Response Body:
```json
{
  "accessToken": "jwt_access_token"
}
```

### Register
`POST /api/auth/register`

Request Body:
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "123456"
}
```

Response: 201 Created, user profile JSON

### Refresh Token
`POST /api/auth/refresh`
- Recommended: refresh token stored in HttpOnly cookie
- Returns **new access token** only

### Logout
`POST /api/auth/logout`
- Clears refresh token from Redis / cookie

---

## 👤 User Endpoints

### Get logged user
`GET /api/users/me`

### Update profile
`PUT /api/users/me`

### Update password
`PUT /api/users/me/password`

### Admin-only endpoints
- List all users: `GET /api/users`
- Find by ID: `GET /api/users/{id}`
- Delete user: `DELETE /api/users/{id}`

## 🐳 Docker & Environment

### Prerequisites
- Docker
- Docker Compose

### Start Full Environment
```sh
docker compose up --build
```

Services:
- Spring Boot app
- MySQL
- Redis

### Environment Variables
Create `.env` file with:
```
SERVER_PORT=8080
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/greendrop
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=123456
JWT_SECRET=your_jwt_secret
JWT_REFRESH_EXPIRATION_MS=86400000
REDIS_HOST=localhost
REDIS_PORT=6379
```

---

## 🛠️ Local Development

Run MySQL + Redis with Docker:
```sh
docker compose up db redis
```

Run Spring Boot locally:
```sh
./gradlew bootRun
```

---

## 📘 API Documentation

Swagger UI: http://localhost:8080/swagger-ui.html

---

## 📈 Future Improvements

- SonarCloud + Jacoco coverage
- Unit & integration tests (JUnit 5 + Mockito + Testcontainers)
- Logging improvements (Logback + TraceID)
- Collector routes module
- AI module for scoring collections
- UML / C4 diagrams in README

---

## 🤝 Contributing

1. Create a new feature branch
2. Commit with clear messages
3. Open a Pull Request
4. Keep documentation updated

---

## 📄 License
MIT License — free to use, study, and modify.

