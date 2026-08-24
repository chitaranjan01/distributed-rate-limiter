package com.project.rateLimiter.algorithm;

import com.project.rateLimiter.model.RateLimiterResult;

public interface RateLimiterAlgorithm {
    RateLimiterResult isAllowed(String clientId, long limit, long windowSizeSecond);
}
