# 1. Java 17 베이스 이미지 선택 (플랫폼 명시)
FROM --platform=linux/amd64 eclipse-temurin:17-jdk-jammy AS builder

# 2. 작업 디렉터리 설정
WORKDIR /app

# 3. 빌드된 JAR 파일 복사
COPY /build/libs/*.jar app.jar

# 4. 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]