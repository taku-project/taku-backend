# 1. Java 17 베이스 이미지 선택 (플랫폼 명시)
FROM --platform=linux/arm64 eclipse-temurin:17-jdk-jammy AS builder

# 작업 디렉토리를 /app으로 설정합니다. 이 위치를 기준으로 이후 명령이 실행됩니다.
WORKDIR /app

# Spring Boot 애플리케이션에서 사용할 빌드 시 설정 가능한 변수들을 정의합니다.
ARG SPRING_PROFILES_ACTIVE
ARG SPRING_DATA_REDIS_HOST
ARG SPRING_DATA_REDIS_PORT
ARG SPRING_DATA_REDIS_PASSWORD

# 위에서 정의한 ARG 값을 Docker 컨테이너 환경 변수로 설정합니다.
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}
ENV SPRING_DATA_REDIS_HOST=${SPRING_DATA_REDIS_HOST}
ENV SPRING_DATA_REDIS_PORT=${SPRING_DATA_REDIS_PORT}
ENV SPRING_DATA_REDIS_PASSWORD=${SPRING_DATA_REDIS_PASSWORD}

# 호스트 머신의 build/libs 디렉토리에서 .jar 파일을 컨테이너의 app.jar로 복사합니다.
COPY /build/libs/*.jar app.jar
COPY /src/main/resources/*.yml .
# 컨테이너가 시작될 때 실행할 명령을 지정합니다.
# 여기서는 Java 애플리케이션(JAR 파일)을 실행합니다.
CMD ["java", "-jar", "-Dspring.profiles.active=default,local", "app.jar" ]
