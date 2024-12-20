# 1. Java 17 베이스 이미지 선택 (빌드 단계)
FROM eclipse-temurin:17-jdk-jammy as builder

# 2. 작업 디렉터리 설정
WORKDIR /app

# 3. 프로젝트 소스 코드 복사
COPY . .

# 4. Gradle 캐시를 활용하기 위해 Gradle 래퍼 파일과 설정 파일만 먼저 복사
COPY gradle /app/gradle
COPY gradlew /app/gradlew
COPY build.gradle /app/build.gradle
COPY settings.gradle /app/settings.gradle

# 5. Gradle 종속성 미리 다운로드 (캐시 활용)
RUN ./gradlew dependencies

# 6. 애플리케이션 빌드
RUN ./gradlew build --no-daemon

# 7. Java 17 런타임 베이스 이미지 선택
FROM eclipse-temurin:17-jre-jammy

# 8. 작업 디렉터리 설정
WORKDIR /app

# 9. 빌드 단계에서 생성된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 10. 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]