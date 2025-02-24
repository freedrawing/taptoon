FROM gradle:7.6.1-jdk17 AS builder
WORKDIR /build

COPY build.gradle settings.gradle /build/
COPY src /build/src

RUN gradle build -x test --parallel --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=builder /build/build/libs/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENV TZ=Asia/Seoul

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]