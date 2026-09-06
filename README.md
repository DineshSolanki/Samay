# Samay v4.0

[![Maven Central](https://img.shields.io/maven-central/v/io.github.dineshsolanki/Samay)](https://search.maven.org/artifact/io.github.dineshsolanki/Samay)
![GitHub](https://img.shields.io/github/license/dineshsolanki/Samay)

Production-grade timezone propagation for Spring Boot. Resolves the client's timezone once per request and makes it available everywhere — controllers, services, `@Async` threads, `CompletableFuture` chains, and JSON responses.

## Features

- **Pluggable resolution** — header, JWT claim, or fixed timezone
- **Async propagation** — `TaskDecorator` for `@Async`, `SamayCompletableFuture`, and `SamayTaskExecutor`
- **Convenience API** — `Samay.now()`, `Samay.atZone()`, `Samay.format()`, `Samay.startOfDay()`
- **Jackson integration** — opt-in per-field timezone conversion with `@SamayFormat`
- **JPA support** — `SamayZoneAttributeConverter` for UTC storage

## Modules

| Artifact | Purpose |
|----------|---------|
| `samay-core` | Interceptor, strategies, async support, convenience API, JPA converter |
| `samay-jackson` | Jackson auto-configuration with `@SamayFormat` annotation |
| `samay-spring-boot-starter` | Auto-configuration — add this to your project |

## Quick Start

```xml
<dependency>
    <groupId>io.github.dineshsolanki</groupId>
    <artifactId>samay-spring-boot-starter</artifactId>
    <version>4.0.0</version>
</dependency>

<!-- Optional: per-field JSON timezone conversion -->
<dependency>
    <groupId>io.github.dineshsolanki</groupId>
    <artifactId>samay-jackson</artifactId>
    <version>4.0.0</version>
</dependency>
```

## Configuration

```properties
# ─── Core ────────────────────────────────────────────
samay.enabled=true                    # disable to turn off all timezone handling
samay.strategy=header                 # header | jwt | fixed
samay.header-name=X-TimeZone          # HTTP header name (header strategy)
samay.jwt-claim=timezone              # JWT claim name (jwt strategy)
samay.fixed-zone=UTC                  # timezone ID (fixed strategy)
samay.thread.inheritable=false        # use InheritableThreadLocal for child threads

# ─── Jackson (optional) ─────────────────────────────
samay.jackson.auto-convert=false      # set true to enable @SamayFormat conversion
samay.jackson.global=false            # set true to convert ALL date fields (not just annotated)
samay.jackson.date-time-pattern=yyyy-MM-dd'T'HH:mm:ssXXX
```

## Usage

### 1. Access the client's timezone anywhere

```java
@RestController
public class EventController {

    @GetMapping("/events")
    public List<EventDto> getEvents() {
        ZoneId userZone = Samay.getZoneId();
        // use userZone for queries, formatting, etc.
    }
}
```

### 2. Convenience timestamp API

```java
ZonedDateTime userNow = Samay.now();
ZonedDateTime display = Samay.atZone(entity.getCreatedAt());
String formatted = Samay.format(entity.getCreatedAt(), "MMM dd, yyyy HH:mm");

// Date-range queries against UTC-stored timestamps
Instant start = Samay.startOfDay(selectedDate);
Instant end = Samay.endOfDay(selectedDate);
```

### 3. Async propagation

```java
// Option A: SamayTaskExecutor (pre-configured)
@Bean
public SamayTaskExecutor taskExecutor() {
    SamayTaskExecutor executor = new SamayTaskExecutor();
    executor.setCorePoolSize(4);
    return executor;
}

// Option B: TaskDecorator with your own executor
@Bean
public TaskDecorator samayTaskDecorator() {
    return new SamayTaskDecorator();
}

// Option C: CompletableFuture with timezone propagation
SamayCompletableFuture.supplyAsync(() -> {
    // Samay.getZoneId() works here — timezone propagated from caller
    return service.findEvents(Samay.now());
});
```

### 4. Per-field JSON conversion (samay-jackson)

Annotate only the fields that should be timezone-converted:

```java
public class EventDto {
    @SamayFormat
    private Instant startTime;   // converted to user's timezone in JSON

    private Instant createdAt;   // stays UTC
}

// application.properties
// samay.jackson.auto-convert=true
```

Set `samay.jackson.global=true` to convert all date/time fields without annotating each one.

### 5. JPA support

```java
// Entity — store as UTC
@Column(name = "created_at")
@Convert(converter = SamayZoneAttributeConverter.class)
private Instant createdAt;

// Display in user's timezone
ZonedDateTime display = Samay.atZone(entity.getCreatedAt());

// Date-range queries
Instant start = Samay.startOfDay(selectedDate);
Instant end = Samay.endOfDay(selectedDate);
```

### 6. JWT timezone resolution

```properties
samay.strategy=jwt
samay.jwt-claim=timezone
```

Extracts timezone from the JWT payload (base64-decoded, no signature verification). Falls back to the `X-TimeZone` header if no JWT is present. **Requires `jackson-databind` on the classpath.**

### 7. Fixed timezone

```properties
samay.strategy=fixed
samay.fixed-zone=Asia/Kolkata
```

## Migration from v3.x

| v3.x | v4.0 |
|------|------|
| `Samay.getTimeZone()` → `TimeZone` | `Samay.getZoneId()` → `ZoneId` |
| Single jar | Multi-module: add `samay-spring-boot-starter` |
| Header only | Header, JWT, or fixed strategy |
| No async support | `SamayTaskDecorator`, `SamayCompletableFuture` |
| No JSON support | `@SamayFormat` per-field conversion |

`Samay.getTimeZone()` is deprecated but still works.

## License

GPL-3.0 — see [LICENSE](LICENSE).
