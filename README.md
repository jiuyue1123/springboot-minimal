# Spring Boot Minimal

A minimal, multi-module Spring Boot template for new services. It ships the cross-cutting
pieces most services need on day one: a unified response envelope, error codes, exception
handling, trace ids, access logs, persistence with Flyway, Redis, API documentation and
container deployment assets.

## Tech stack

| Area | Choice |
| --- | --- |
| Java | 17 |
| Framework | Spring Boot 3.5.16 |
| Persistence | MyBatis-Plus 3.5.17, Flyway, MySQL 8.4 |
| Cache | Spring Data Redis, Redis 8 |
| Validation | Jakarta Bean Validation (Hibernate Validator) |
| API docs | springdoc-openapi 2.9.1 |
| Utilities | Lombok, MapStruct |
| Testing | JUnit 5, MockMvc, Testcontainers |
| Runtime | Actuator, Logback, Docker |

## Project layout

```
.
├── common/                      # Pure Java shared module, no Spring dependencies
│   └── src/main/java/com/example/common
│       ├── constant/            # MDC key and header names
│       ├── exception/           # ErrorCode, BusinessException
│       ├── result/              # Result, PageResult
│       └── utils/               # ThrowUtils, TraceIdUtils
├── web/                         # Spring Boot application
│   ├── src/main/java/com/example/web
│   │   ├── config/              # MyBatis-Plus, Redis and OpenAPI configuration
│   │   ├── controller/          # Sample endpoints
│   │   ├── exception/           # GlobalExceptionHandler
│   │   └── filter/              # TraceIdFilter, AccessLogFilter
│   ├── src/main/resources
│   │   ├── db/migration/        # Flyway migrations
│   │   ├── application*.yml     # Base and profile configuration
│   │   └── logback-spring.xml   # Logging configuration
│   └── src/test/java/com/example/web
│       ├── AbstractIntegrationTest.java      # Testcontainers test base
│       ├── TestcontainersConfiguration.java  # MySQL and Redis containers
│       └── controller/                       # Sample integration test
├── deploy/                      # Docker Compose for development and production
├── Dockerfile
└── pom.xml                      # Parent aggregator and dependency management
```

## Requirements

- JDK 17+
- Maven 3.9+
- Docker (for the Compose development dependencies and the Testcontainers integration tests)

The parent sets `maven.compiler.proc=full`, so Lombok keeps working when the build runs on
JDK 23 or newer, where implicit annotation processing is disabled by default.

## Quick start

1. Start the local dependencies.

   ```bash
   docker compose -f deploy/docker-compose-dev.yaml up -d
   ```

2. Create the local development configuration.

   ```bash
   cp web/src/main/resources/application-dev.yml.example \
      web/src/main/resources/application-dev.yml
   ```

   The example defaults to `localhost:3306/springboot_minimal` with `root/root`.

3. Start the application from the IDE (`WebApplication`) or from the command line.

   ```bash
   mvn -pl web -am -DskipTests package
   java -Dloader.path=web/target/libs -jar web/target/app.jar
   ```

4. Verify.

   | Endpoint | URL |
   | --- | --- |
   | Sample API | http://localhost:8080/test/success |
   | Swagger UI | http://localhost:8080/swagger-ui.html |
   | OpenAPI JSON | http://localhost:8080/v3/api-docs |
   | Health | http://localhost:8080/actuator/health |

## Configuration

### Profiles

| Profile | Purpose |
| --- | --- |
| `dev` | Local development (active by default). Swagger UI enabled, debug logging, all Actuator endpoints exposed. |
| `test` | Integration tests. Swagger UI enabled, database `springboot_minimal_test`. |
| `prod` | Production. Swagger UI disabled, INFO logging, database credentials required. |

`application-dev.yml` is git-ignored; `application-dev.yml.example` is the committed template.

### Environment variables

| Variable | Dev / test default | Required in prod | Description |
| --- | --- | --- | --- |
| `DB_HOST` | `localhost` | yes | MySQL host |
| `DB_PORT` | `3306` | no, defaults to `3306` | MySQL port |
| `DB_NAME` | `springboot_minimal` / `springboot_minimal_test` | yes | Database name |
| `DB_USERNAME` | `root` | yes | Database user |
| `DB_PASSWORD` | `root` | yes | Database password |
| `REDIS_HOST` | `localhost` | yes | Redis host |
| `REDIS_PORT` | `6379` | no, defaults to `6379` | Redis port |
| `REDIS_DATABASE` | `0` | no, defaults to `0` | Redis database index |
| `REDIS_PASSWORD` | none | yes | Redis password |

`.env.example` documents the variables. Spring Boot does not load `.env` automatically; pass
them through the IDE run configuration, Compose, or the shell.

### Database

- Flyway runs at startup and reads scripts from `web/src/main/resources/db/migration`. Name
  them `V1__init.sql`, `V2__add_user.sql`, and so on.
- MyBatis-Plus generates snowflake ids (`assign_id`), stores enums by name and limits pages to
  500 rows.
- Redis is wired through Spring Data Redis. `RedisConfig` exposes a
  `RedisTemplate<String, Object>` with string keys and typed JSON values; connection settings
  use the standard `spring.data.redis.*` properties. Dev and test default to
  `localhost:6379`; prod requires `REDIS_HOST` and `REDIS_PASSWORD`.

## API conventions

### Response envelope

`Result<T>` carries only `code`, `message` and `data`. The message always comes from
`ErrorCode`; callers cannot pass a custom one.

```json
{
  "code": "00000",
  "message": "Success",
  "data": {}
}
```

`PageResult<T>` adds `total`, `pageNum` and `pageSize` around a list payload.

The trace id and server timestamp are returned as response headers instead of body fields:

| Header | Description |
| --- | --- |
| `X-Trace-Id` | Reused from the request header when present, otherwise generated |
| `X-Timestamp` | Epoch milliseconds when the request entered the filter |

### Error codes

Codes follow the `A BB CC` format:

- `A` – module; `1` is the common module, other modules use 2, 3, ...
- `BB` – error category inside that module
- `CC` – specific error

`00000` is reserved for success. Throw business errors with `ThrowUtils`:

```java
ThrowUtils.throwIf(user == null, ErrorCode.RESOURCE_NOT_FOUND);
```

`GlobalExceptionHandler` maps each `ErrorCode` to an HTTP status, for example
`INVALID_PARAMETER` to 400, `RESOURCE_NOT_FOUND` to 404 and `RATE_LIMIT_EXCEEDED` to 429.
Exception details are logged, never returned.

### Sample endpoints

| Method | Path | Behaviour |
| --- | --- | --- |
| GET | `/test/success` | Returns a successful `Result` |
| GET | `/test/page` | Returns a `PageResult` |
| GET | `/test/business-error` | Throws `BusinessException`, responds 404 |
| GET | `/test/system-error` | Throws an unexpected exception, responds 500 |
| POST | `/test/validate` | Validates a request body, responds 400 when blank |
| GET | `/test/redis?key=demo` | Writes and reads a typed JSON value through `RedisTemplate` |

## Observability

- `TraceIdFilter` stores the trace id in MDC, echoes `X-Trace-Id` and `X-Timestamp`, and clears
  the MDC after each request.
- `AccessLogFilter` writes `access start` and `access end` lines. With DEBUG enabled for
  `com.example.web.filter.AccessLogFilter` it also logs the query string, request parameters
  and headers, masking sensitive values.
- Logback writes to the console and to `logs/web.log` and `logs/web-error.log` for every
  profile, rolling daily or at 100MB, gzip-compressing archives and keeping 30 days.
- Actuator exposes `health` and `info`; the `dev` profile exposes every endpoint and shows
  health details.

## OpenAPI

- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`
- Metadata comes from `openapi.info.*`; the version is `info.app.version`.
- Component scanning and `springdoc.packages-to-scan` both cover `com.example`, so controllers
  from modules sharing that package root are documented automatically.
- Disabled in the `prod` profile.

## Deployment

### Image

The `Dockerfile` builds the thin jar in a Maven stage and runs it on
`eclipse-temurin:17-jre-alpine` as a non-root user. Dependencies are loaded from `/app/libs`
through `loader.path`. The image defaults to the `prod` profile and requires the database and
Redis variables.

### Compose

| File | Services |
| --- | --- |
| `deploy/docker-compose-dev.yaml` | `mysql` and `redis` by default; add `--profile app` to run the application container as well |
| `deploy/docker-compose-prod.yaml` | `app`, `mysql` and `redis` with health-gated startup, required secrets and log rotation |

```bash
# Development dependencies only
docker compose -f deploy/docker-compose-dev.yaml up -d

# Development dependencies plus the application container
docker compose -f deploy/docker-compose-dev.yaml --profile app up -d --build

# Production
export MYSQL_ROOT_PASSWORD='...' MYSQL_PASSWORD='...' REDIS_PASSWORD='...'
docker compose -f deploy/docker-compose-prod.yaml up -d --build
```

MySQL credentials are applied only when the data volume is initialized for the first time.

## Tests

```bash
mvn -pl web -am test

# single test class
mvn -pl web -am test -Dtest=TestControllerIntegrationTest
```

Integration tests run against disposable MySQL 8.4 and Redis containers started by
Testcontainers, so a running Docker daemon is required and no external database is needed.
The first run starts the containers and takes a minute or two; later runs reuse them.

Extend `AbstractIntegrationTest` for new integration tests. It activates the `test` profile,
imports `TestcontainersConfiguration`, prepares MockMvc and shares one cached application
context, so container startup is paid once per suite:

```java
class UserApiTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
}
```

Unit tests that need no infrastructure should not extend the base class.

## Extending

1. Add a Maven module whose parent is `springboot-minimal`.
2. Keep the package under `com.example` so component scanning, OpenAPI and error codes stay
   consistent.
3. Declare the module as a dependency of `web`; the version is managed by the parent.
4. Add migrations under `web/src/main/resources/db/migration`.
