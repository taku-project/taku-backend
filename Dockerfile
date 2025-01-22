FROM gradle:7.4-jdk17 AS builder
WORKDIR /build

# 그래들 파일이 변경되었을 때만 새롭게 의존패키지 다운로드 받게함.
COPY build.gradle settings.gradle /build/
RUN ./gradlew build -x test --parallel --continue > /dev/null 2>&1 || true

COPY . /build

# gradlew 실행 권한 부여
RUN chmod +x ./gradlew

# Gradle 빌드 실행
RUN ./gradlew build -x test --parallel 


#COPY ./build
#RUN gradle build -x test --parallel



# 1. Java 17 베이스 이미지 선택 (플랫폼 명시)
FROM --platform=linux/arm64 eclipse-temurin:17.0.10_7-jre 

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
# COPY /src/main/resources/application.yml application.yml

COPY --from=builder /build/build/libs/*.jar app.jar

# 여기서는 Java 애플리케이션(JAR 파일)을 실행합니다.
CMD ["java", "-jar","-Dspring.data.redis.port=6379","-Dspring.data.redis.host=52.79.201.184","-Dspring.data.redis.password=1111", "app.jar" ]
