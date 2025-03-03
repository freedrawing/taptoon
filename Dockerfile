# Stage 1: 빌드
FROM gradle:7.6.1-jdk17 AS builder
WORKDIR /build
COPY build.gradle settings.gradle /build/
COPY src /build/src
RUN gradle build -x test --parallel --no-daemon

# Stage 2: 실행
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# netcat-openbsd 설치 추가
RUN apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*
COPY --from=builder /build/build/libs/*.jar app.jar
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV TZ=Asia/Seoul
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]