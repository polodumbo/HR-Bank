# 사용할 자바 런타임 이미지 지정
FROM openjdk:17-jdk-slim

# Gradle Wrapper를 사용하기 위해 빌드 도구 설치
# 필요한 의존성을 먼저 설치해 레이어 캐싱을 활용합니다.
COPY gradlew .
COPY gradle gradle
RUN chmod +x ./gradlew

# 애플리케이션 소스 코드 복사
COPY src src

# 프로젝트 빌드
RUN ./gradlew bootJar

# 빌드된 JAR 파일을 /app 디렉터리로 복사
COPY build/libs/*.jar /app/app.jar

# 애플리케이션 실행 명령어
ENTRYPOINT ["java", "-jar", "/app/app.jar"]