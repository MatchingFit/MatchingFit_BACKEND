# 1단계: 빌드 스테이지
FROM gradle:8.7-jdk21 AS builder

WORKDIR /build
COPY . .
RUN gradle bootJar --no-daemon

# 2단계: 실행 스테이지
FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY --from=builder /build/build/libs/*.jar app.jar

ARG PROFILE
ENV PROFILE=${PROFILE}

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]