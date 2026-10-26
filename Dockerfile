# syntax=docker/dockerfile:1

# ---------- build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY common ./common
COPY web ./web

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl web -am -DskipTests package

# ---------- runtime ----------
FROM eclipse-temurin:17-jre-jammy

ENV TZ=Asia/Shanghai \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

RUN groupadd --gid 1001 app \
    && useradd --uid 1001 --gid app --system --no-create-home --home-dir /app app

WORKDIR /app

# Thin jar: app.jar holds only application classes, dependencies live in libs/
COPY --from=build --chown=app:app /workspace/web/target/app.jar /app/app.jar
COPY --from=build --chown=app:app /workspace/web/target/libs /app/libs

USER app

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dloader.path=/app/libs -jar /app/app.jar"]
