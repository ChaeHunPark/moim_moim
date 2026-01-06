FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle gradle
RUN gradle dependencies --no-daemon

COPY . .
RUN gradle clean bootJar -x test --no-daemon


FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*SNAPSHOT.jar app.jar
COPY wait-for-it.sh wait-for-it.sh
RUN chmod +x wait-for-it.sh

EXPOSE 8080
ENTRYPOINT ["sh", "./wait-for-it.sh", "db:3306", "--", "java", "-jar", "app.jar"]