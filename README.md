# 🕒 Samay (Formerly TimeZoneInterceptor)

[![Maven Central](https://img.shields.io/maven-central/v/io.github.dineshsolanki/Samay)](https://search.maven.org/artifact/io.github.dineshsolanki/Samay)
![GitHub](https://img.shields.io/github/license/dineshsolanki/Samay)
![Lines of code](https://sloc.xyz/github/DineshSolanki/Samay)

## Overview

🌐 Samay (Formerly TimeZoneInterceptor)
is a Java library for Spring Boot that provides automatic handling of timezone information in incoming HTTP requests.
It allows you to extract and store the timezone from a custom header,
making it easily accessible in your application's controller or service methods.

## Features

- ✨ Automatic extraction of timezone information from a custom header in incoming requests.
- 🧵 Stores the timezone information in a `ThreadLocal` variable, accessible within the same request thread.
- 🚀 Seamless integration with Spring Boot applications.
- ⚙️ Customizable header to change where it looks for timezone

## Installation

Add the following dependency to your project's `pom.xml` to use Samay:

```xml
<dependency>
    <groupId>io.github.dineshsolanki</groupId>
    <artifactId>Samay</artifactId>
    <version>RELEASE</version>
</dependency>
```

Samay (Formerly TimeZoneInterceptor) is available on Maven Central.

## Usage

### 1. Configuration

By default, Samay expects the timezone information to be provided in the `X-TimeZone` header of incoming requests. You can customize the header name by adding the following property to your `application.properties`:

```properties
samay.header-name=Your-Custom-TimeZone-Header
```
#### 1.2. Enable thread inheritance
The default behavior of Samay is to not inherit the timezone information in child threads.
You can enable thread inheritance by adding the following property to your `application.properties`:

```properties
samay.thread.inheritable=true
```

### 2. Accessing the Timezone

In your Spring Boot application's controller or service methods, you can access the timezone information using the `Samay.getTimeZone()` method:

```java
import io.github.dineshsolanki.Samay;

@RestController
public class YourController {

    @GetMapping("/your-endpoint")
    public ResponseEntity<String> yourEndpoint() {
        TimeZone timeZone = Samay.getTimeZone();
        // Your logic using the timeZone information
        return ResponseEntity.ok("Endpoint executed with timezone: " + timeZone.getID());
    }
}
```

### 3. Spring Boot Auto-Configuration (Optional)

Samay automatically registers the interceptor in Spring Boot applications using Spring Boot's autoconfiguration feature.
You don't need to explicitly configure the interceptor.

### 4. Interceptor Removal

The library automatically cleans up the `ThreadLocal` storage after the request is processed. You don't need to worry about manual cleanup.

## License

Samay (Formerly TimeZoneInterceptor) is distributed under the GPL-3 License.
See [LICENSE](https://github.com/DineshSolanki/TimeZoneInterceptor/blob/master/LICENSE) for more information.

## Contributions

🤝 Contributions are welcome! If you encounter any issues, have suggestions, or want to contribute, please feel free to open an issue or submit a pull request.

---

### For Library Developers

To build the library from source, clone the repository and run:

```shell
mvn clean install
```

This will build and install the library into your local Maven repository.

```shell
git clone https://github.com/DineshSolanki/Samay.git
cd Samay
mvn clean install
```
---
Samay
---
Discover the world of effortless timezone retrieval with Samay (Formerly TimeZoneInterceptor)!
🌏 Simplify your Spring Boot applications and say goodbye to timezone-related headaches.
Let Samay take care of the heavy lifting, so you can focus on creating amazing applications.
Give it a try and see the difference today!
🚀

For any queries, support, or discussions, don't hesitate to join the community.!
🎉 We look forward to having you on board!
Happy coding!
👨‍💻👩‍💻
