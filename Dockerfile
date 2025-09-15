# Stage 1: 빌드 환경 (builder stage)
FROM openjdk:17-jdk-slim AS builder

WORKDIR /app

# Gradle Wrapper 및 소스 코드 복사
COPY gradlew .
COPY gradle gradle
COPY src src

# gradlew 파일에 실행 권한 부여
RUN chmod +x ./gradlew

# 애플리케이션 빌드
RUN ./gradlew clean build bootJar --no-daemon

# Stage 2: 실행 환경 (final stage)
FROM openjdk:17-jre-slim

WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일만 복사
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]