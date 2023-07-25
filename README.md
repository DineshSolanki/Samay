# 🕒 TimeZoneInterceptor

[![Maven Central](https://img.shields.io/maven-central/v/io.github.dineshsolanki/TimeZoneInterceptor)](https://search.maven.org/artifact/io.github.dineshsolanki/TimeZoneInterceptor)
![GitHub](https://img.shields.io/github/license/dineshsolanki/TimeZoneInterceptor)
![Lines of code](https://sloc.xyz/github/DineshSolanki/TimeZoneInterceptor)

## Overview

🌐 TimeZoneInterceptor is a Java library for Spring Boot that provides automatic handling of timezone information in incoming HTTP requests. It allows you to extract and store the timezone from a custom header, making it easily accessible in your application's controller or service methods.

## Features

- ✨ Automatic extraction of timezone information from a custom header in incoming requests.
- 🧵 Stores the timezone information in a `ThreadLocal` variable, accessible within the same request thread.
- 🚀 Seamless integration with Spring Boot applications.
- ⚙️ Customizable header to change where it looks for timezone

## Installation

Add the following dependency to your project's `pom.xml` to use TimeZoneInterceptor:

```xml
<dependency>
    <groupId>io.github.dineshsolanki</groupId>
    <artifactId>TimeZoneInterceptor</artifactId>
    <version>RELEASE</version>
</dependency>
```

TimeZoneInterceptor is available on Maven Central.

## Usage

### 1. Configuration

By default, TimeZoneInterceptor expects the timezone information to be provided in the `X-TimeZone` header of incoming requests. You can customize the header name by adding the following property to your `application.properties`:

```properties
time-zone-interceptor.header-name=Your-Custom-TimeZone-Header
```

### 2. Accessing the Timezone

In your Spring Boot application's controller or service methods, you can access the timezone information using the `TimeZoneInterceptor.getTimeZone()` method:

```java
import io.github.dineshsolanki.TimeZoneInterceptor;

@RestController
public class YourController {

    @GetMapping("/your-endpoint")
    public ResponseEntity<String> yourEndpoint() {
        TimeZone timeZone = TimeZoneInterceptor.getTimeZone();
        // Your logic using the timeZone information
        return ResponseEntity.ok("Endpoint executed with timezone: " + timeZone.getID());
    }
}
```

### 3. Spring Boot Auto-Configuration (Optional)

TimeZoneInterceptor automatically registers the interceptor in Spring Boot applications using Spring Boot's auto-configuration feature. You don't need to explicitly configure the interceptor.

### 4. Interceptor Removal

The library automatically cleans up the `ThreadLocal` storage after the request is processed. You don't need to worry about manual cleanup.

## License

TimeZoneInterceptor is distributed under the GPL-3 License. See [LICENSE](https://github.com/DineshSolanki/TimeZoneInterceptor/blob/master/LICENSE) for more information.

## Contributions

🤝 Contributions are welcome! If you encounter any issues, have suggestions, or want to contribute, please feel free to open an issue or submit a pull request.

---

### For Library Developers

To build the library from source, clone the repository and run:

```
mvn clean install
```

This will build and install the library into your local Maven repository.

```
git clone https://github.com/DineshSolanki/TimeZoneInterceptor.git
cd TimeZoneInterceptor
mvn clean install
```
---
TimeZoneInterceptor
---
Discover the world of effortless timezone retrieval with TimeZoneInterceptor! 🌏 Simplify your Spring Boot applications and say goodbye to timezone-related headaches. Let TimeZoneInterceptor take care of the heavy lifting, so you can focus on creating amazing applications. Give it a try and see the difference today! 🚀

For any queries, support, or discussions, don't hesitate to join our vibrant community! 🎉 We look forward to having you onboard! Happy coding! 👨‍💻👩‍💻
