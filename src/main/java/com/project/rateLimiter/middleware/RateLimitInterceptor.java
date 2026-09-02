package com.project.rateLimiter.middleware;

import com.project.rateLimiter.algorithm.AlgorithmRegistry;
import com.project.rateLimiter.algorithm.AlgorithmType;
import com.project.rateLimiter.algorithm.RateLimiterAlgorithm;
import com.project.rateLimiter.exception.RateLimitExceededException;
import com.project.rateLimiter.model.RateLimiterResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;


@Component
public  class RateLimitInterceptor implements HandlerInterceptor {
    private final AlgorithmRegistry algorithmRegistry;

    public RateLimitInterceptor(AlgorithmRegistry algorithmRegistry) {
        this.algorithmRegistry = algorithmRegistry;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimit = handlerMethod.getMethod().getAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return true;
        }
        AlgorithmType algorithmType = rateLimit.algorithmType();
        long limit = rateLimit.limit();
        long windowSize = rateLimit.windowSize();
        String clientId = request.getRemoteAddr();

        RateLimiterAlgorithm algorithm = algorithmRegistry.getAlgorithm(algorithmType);

        RateLimiterResult result = algorithm.isAllowed(clientId, limit, windowSize);
        response.setHeader("x-rate-limiter-limit", String.valueOf(result.getLimit()));
        response.setHeader("X-Rate-limiter-Remaining", String.valueOf(result.getRemaining()));
        response.setHeader("X-Rate-limiter-Reset", String.valueOf(result.getResetTimeSecond()));
        if (!result.isAllowed()) {
            Long retryAfter = result.getResetTimeSecond() - Instant.now().getEpochSecond();
            throw new RateLimitExceededException(retryAfter);
        }
        return true;
    }
}

