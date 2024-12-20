# 1. Java 17 베이스 이미지 선택 (빌드 단계)
FROM eclipse-temurin:17-jdk-jammy as builder

# 2. 작업 디렉터리 설정
WORKDIR /app

# 9. 빌드 단계에서 생성된 JAR 파일 복사
COPY /build/libs/*.jar app.jar

# 10. 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
