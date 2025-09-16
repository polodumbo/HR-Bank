# Stage 1: 빌드 환경
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

# Stage 2: 실행 환경
FROM openjdk:17-jdk-slim

WORKDIR /app

# 빌드 산출물 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 설정 (Railway에서 제공하는 PORT 환경변수 사용)
ENV PORT 8080
EXPOSE 8080

# Spring Boot 실행
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=$PORT"]
