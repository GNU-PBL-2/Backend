# ── Stage 1: Build ───────────────────────────────────────────
FROM gradle:8.12-jdk21 AS builder

WORKDIR /app

# Gradle 캐시 레이어: 의존성 파일이 바뀌지 않으면 재사용
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY gradlew gradlew.bat ./

# 의존성만 먼저 다운로드 → 소스 변경 시 이 레이어 캐시 재사용
RUN ./gradlew dependencies --no-daemon

# 소스 복사 후 JAR 빌드 (테스트 제외)
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# ── Stage 2: Run ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
