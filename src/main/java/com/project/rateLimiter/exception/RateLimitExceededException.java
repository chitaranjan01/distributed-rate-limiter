package com.project.rateLimiter.exception;

public class RateLimitExceededException  extends RuntimeException{
    private final Long retryAfterSeconds;
    public RateLimitExceededException(Long retryAfterSeconds) {
        super("Rate Limit Exceeded . please slow down");
        this.retryAfterSeconds = retryAfterSeconds;
    }
    public Long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
