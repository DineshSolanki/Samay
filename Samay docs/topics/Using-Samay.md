# Using Samay

This section explains how to use the main features of the Samay library in your Spring Boot application.

## Timezone Extraction

By default, Samay expects the timezone in a request header named `X-TimeZone`.

To extract and store timezone on each request:

```java
@RestController
public class ExampleController {

  @GetMapping("/endpoint")
  public String handler(HttpServletRequest request) {
   
    // Samay extracts timezone from request
    // and stores in thread local
    
  }

}
```

No need to manually extract the timezone. Samay handles it automatically.

## Accessing Timezone

To access the extracted timezone in your controllers or services:

```java
import io.github.dineshsolanki.Samay;

@Service
public class MyService {

  public void businessLogic() {
  
    TimeZone tz = Samay.getTimeZone();
    
    // use timezone as needed
    
  }

}
```

The timezone is available via `Samay.getTimeZone()` anywhere downstream.

> For convenience, you can also inject Samay directly into your beans and call getTimeZone() on it.
>
{style="note"}

### Changing Header Name {collapsible=true, default-state="expanded"}

To customize the request header name:

```Java
time-zone-interceptor.header-name=My-Timezone-Header
```

Samay will now check `My-Timezone-Header` instead of default `X-TimeZone`.


> Changes to the header name require restarting the application for the new name to take effect.
>
{style="warning"}

### Installing Samay

<tabs>
  <tab title="Maven">

```xml

<dependency>
    <groupId>io.github.dineshsolanki</groupId>
    <artifactId>Samay</artifactId>
    <version>RELEASE</version>
</dependency>
```

  </tab>
  <tab title="Gradle">

```Gradle
implementation 'io.github.dineshsolanki:Samay:RELEASE'
```

  </tab>
</tabs>

## Disable Auto Configuration

To disable the auto configuration in Spring Boot:

```Java 
@SpringBootApplication(exclude = {SamayAutoConfiguration.class})
public class MyApp {

}
```

And manually configure the interceptor:

```java

@Bean 
public TimeZoneInterceptor timeZoneInterceptor() {
  return new TimeZoneInterceptor();
}
```

## ThreadLocal Cleanup

Samay automatically cleans up the thread local after the request is handled. No need for manual removal.