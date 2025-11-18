# ============================
# BUILD STAGE
# ============================
FROM gradle:8.6-jdk17 AS build
WORKDIR /home/gradle/project

COPY --chown=gradle:gradle . .
RUN gradle clean bootJar --no-daemon

# ============================
# RUNTIME STAGE
# ============================
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
