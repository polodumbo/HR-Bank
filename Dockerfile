# 사용할 자바 런타임 이미지 지정
FROM openjdk:17-jdk-slim

# JAR 파일이 생성될 위치를 설정
ARG JAR_FILE=build/libs/*.jar

# 애플리케이션의 작업 디렉터리를 /app으로 설정
WORKDIR /app

# JAR 파일을 /app으로 복사
COPY ${JAR_FILE} app.jar

# 애플리케이션 실행 명령어
ENTRYPOINT ["java", "-jar", "/app/app.jar"]