# Stage 1: 빌드 환경 (builder stage)
FROM openjdk:17-jdk-slim AS builder

WORKDIR /app

# Gradle Wrapper 및 소스 코드 복사
COPY gradlew .
COPY gradle gradle
COPY src src

# 애플리케이션 빌드
RUN ./gradlew clean build bootJar --no-daemon

# Stage 2: 실행 환경 (final stage)
# 더 작은 런타임 이미지를 사용하여 컨테이너 크기를 줄입니다.
FROM openjdk:17-jre-slim

WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일만 복사
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]