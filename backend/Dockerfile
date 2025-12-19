############################
# 1️⃣ Build Stage (Gradle)
############################
# Spring Boot 애플리케이션을 빌드하기 위한 Gradle + JDK 17 이미지
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# Gradle 설정 파일 먼저 복사 (캐시 최적화)
COPY build.gradle settings.gradle ./
COPY gradle gradle
RUN gradle dependencies --no-daemon

# 소스 코드 복사 후 빌드
COPY . .
RUN gradle clean build -x test --no-daemon


############################
# 2️⃣ Runtime Stage
############################
# Spring Boot 애플리케이션 실행용 Java 17 JDK 이미지
# eclipse-temurin은 경량 + 안정성 때문에 많이 사용됨
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# 2-1. 빌드 결과 JAR 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 2-2. wait-for-it.sh 복사 및 실행 권한 설정
# DB(MySQL)가 완전히 기동되기 전에 백엔드가 실행되는 문제 방지
COPY wait-for-it.sh wait-for-it.sh
RUN chmod +x wait-for-it.sh

# 3. 포트 설정 (문서화 용도)
EXPOSE 8080

# 4. 애플리케이션 실행
# DB:3306 포트가 열릴 때까지 대기 후 Spring Boot 실행
ENTRYPOINT ["sh", "./wait-for-it.sh", "db:3306", "--", "java", "-jar", "app.jar"]