# Stage 1: 빌드 환경 (builder stage)
FROM openjdk:17-jdk-slim AS builder

WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY src src
RUN ./gradlew clean build bootJar --no-daemon

# Stage 2: 실행 환경 (final stage)
# JRE 이미지를 사용하려면 'jdk' 태그를 사용하거나, 더 최적화된 이미지를 선택해야 합니다.
# FROM openjdk:17-jre-slim <-- 오류 발생

FROM openjdk:17-jdk-slim

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]