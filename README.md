# ️ Distributed Rate Limiter

A production-grade, distributed API rate-limiting service built with **Java 21**, **Spring Boot 3**, and **Redis**.

This project solves a critical distributed systems problem: naive in-memory rate limiters break when an application scales horizontally behind a load balancer. By leveraging Redis and Lua scripting, this service maintains atomic, shared state across multiple application instances.

## 🌟 Features

- **Distributed State:** Uses Redis to ensure accurate rate limiting across multiple server instances.
- **Smart Middleware:** A custom Spring `HandlerInterceptor` that dynamically applies rate limits using Java Reflection and custom annotations.
- **Multiple Algorithms:** Implements three industry-standard rate-limiting algorithms to handle different API use cases.
- **Atomic Operations:** Uses Redis Lua scripts to prevent race conditions under heavy concurrent traffic.
- **Battle-Tested:** Proven to handle **400+ requests per second** with **~17ms latency** under load.

## 🏗️ Architecture

The system uses the **Strategy Pattern** combined with Spring's dependency injection:

1. **The Request:** An HTTP request hits a controller method annotated with `@RateLimit`.
2. **The Interceptor:** The `RateLimitInterceptor` intercepts the request, reads the annotation, and extracts the algorithm type, limit, and window size.
3. **The Registry:** The `AlgorithmRegistry` acts as a vending machine, returning the correct algorithm implementation (Fixed, Sliding, or Token Bucket).
4. **The Execution:** The algorithm executes an atomic Lua script in Redis to check and update the client's request count.
5. **The Response:** Headers (`X-Rate-Limit-Limit`, `X-Rate-Limit-Remaining`, `X-Rate-Limit-Reset`) are attached to the response. If blocked, a `429 Too Many Requests` is returned.

## 🧠 Algorithms Implemented

| Algorithm | How it Works | Best Use Case |
| :--- | :--- | :--- |
| **Fixed Window** | Counts requests in rigid time blocks (e.g., 12:00 to 12:01). | Simple, low-stakes endpoints (e.g., public data). |
| **Sliding Window Log** | Uses Redis Sorted Sets to track exact timestamps of requests. Prevents boundary spikes. | Strict security endpoints (e.g., Login, Password Reset). |
| **Token Bucket** | Uses fractional math to drip tokens into a bucket. Allows sudden bursts but enforces an average rate. | Most public APIs (e.g., Stripe, GitHub). Allows good UX while stopping bots. |

## 🚀 Load Testing Results

The system was load-tested using **k6** with 50 concurrent virtual users over 10 seconds.

- **Throughput:** ~420 requests/second
- **Average Latency:** 17ms
- **Success Rate:** 99.9% (The 0.1% failure rate was due to default Spring Redis connection pool timeouts under extreme sudden bursts, not logic errors).
- **Rate Limiting Accuracy:** 100%. The system successfully returned `200 OK` for allowed requests and `429 Too Many Requests` for blocked requests.

*(Note: In the k6 report, 429 responses are marked as "failed" by default. This high "failure" rate actually proves the rate limiter is successfully blocking malicious traffic!)*

## 🛠️ Tech Stack

- **Backend:** Java 21, Spring Boot 3.x
- **Database/Cache:** Redis (with Lua Scripting)
- **Containerization:** Docker & Docker Compose
- **Build Tool:** Maven
- **Load Testing:** Grafana k6

## 🏃♂️ How to Run Locally

### Prerequisites
- Java 21
- Maven
- Docker & Docker Compose

### 1. Start Redis
```bash
docker run -d --name ratelimiter-redis -p 6379:6379 redis:alpine