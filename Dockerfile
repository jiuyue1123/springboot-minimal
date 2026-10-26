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
FROM eclipse-temurin:17-jre-alpine

ENV TZ=Asia/Shanghai \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

RUN addgroup -S -g 1001 app \
    && adduser -S -D -H -u 1001 -G app -h /app app

WORKDIR /app

# Thin jar: app.jar holds only application classes, dependencies live in libs/
COPY --from=build --chown=app:app /workspace/web/target/app.jar /app/app.jar
COPY --from=build --chown=app:app /workspace/web/target/libs /app/libs

# Logback writes to logs/ relative to the working directory; the directory must
# exist and be writable by the non-root user before the appenders start.
RUN mkdir -p /app/logs \
    && chown app:app /app/logs

USER app

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dloader.path=/app/libs -jar /app/app.jar"]
