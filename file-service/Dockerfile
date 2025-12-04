FROM gradle:9.2.1-jdk17-alpine AS builder
WORKDIR /usr/src/

COPY settings.gradle.kts ./
COPY build.gradle.kts ./

COPY src src

RUN gradle build --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /usr/src/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
