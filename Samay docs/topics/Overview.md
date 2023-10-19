# Overview

Samay is a Java library that provides automatic timezone handling for Spring Boot applications. It allows easy extraction and usage of timezone information from HTTP requests.

## What is Samay?

Samay is designed to simplify timezone handling in Spring Boot web applications. It intercepts incoming HTTP requests, extracts the timezone from a custom header, stores it in a ThreadLocal variable, and makes it available throughout the request lifecycle.

This saves developers from having to manually extract and pass around timezone information in their application code. Samay handles this automatically via request interception and context propagation.

Some alternatives to Samay include:

- Manually extracting timezone on every request
- Passing timezone as a method parameter
- Using session/cookie to store timezone

Samay provides a cleaner and more automated way to handle time zones.

## Glossary

Request Interception
: Process of intercepting an HTTP request before it reaches the target controller. Useful for cross-cutting concerns like logging, security, etc.

ThreadLocal
: A variable that provides thread-level isolation. Stores different values per thread. Used to propagate context across layers.

Context Propagation
: Passing request-specific data like user ID, timezone, etc across application layers to avoid plumbing everywhere.
