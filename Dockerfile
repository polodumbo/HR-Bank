# Stage 1: 빌드 환경 (builder stage)
FROM openjdk:17-jdk-slim AS builder

WORKDIR /app

# Gradle Wrapper 및 빌드 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x ./gradlew

# 애플리케이션 빌드
RUN ./gradlew clean build bootJar --no-daemon
