# Stage 1: 빌드 환경
FROM openjdk:17-jdk-slim AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY src src
RUN chmod +x ./gradlew
RUN ./gradlew clean build bootJar --no-daemon

# Stage 2: 실행 환경
FROM openjdk:17-jdk-slim
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
