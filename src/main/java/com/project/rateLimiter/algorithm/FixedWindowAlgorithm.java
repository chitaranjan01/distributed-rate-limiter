package com.project.rateLimiter.algorithm;

import com.project.rateLimiter.model.RateLimiterResult;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class FixedWindowAlgorithm implements RateLimiterAlgorithm {
private  final StringRedisTemplate  stringRedisTemplate;
public FixedWindowAlgorithm(StringRedisTemplate stringRedisTemplate ) {
    this.stringRedisTemplate = stringRedisTemplate;
}
@Override
    public RateLimiterResult isAllowed(String clientId, long limit, long windowSizeSecond) {
    long currentEpochSecond = Instant.now().getEpochSecond();
    long currentWindow= currentEpochSecond / windowSizeSecond;
    String redisKey = String.format("ratelimit:%s:%d", clientId, currentWindow);
    Long currentCount = stringRedisTemplate.opsForValue().increment(redisKey);
    if (currentCount != null && currentCount == 1) {
        stringRedisTemplate.expire(redisKey , Duration.ofSeconds(windowSizeSecond));
    }
    long resetWindow = currentWindow +1;
    long resetTimeSeconds = resetWindow * windowSizeSecond;

    boolean isAllowed = currentCount!= null && currentCount<=limit;
    long remaining = isAllowed?(limit - currentCount): 0 ;
    /* return RateLimiterResult.builder()
             .allowed(isAllowed)
             .remaining(Math.max(remaining ,0))
             .resetTimeSecond(resetTimeSeconds)
             .build();*/
    RateLimiterResult result = new RateLimiterResult(isAllowed , limit , remaining , resetTimeSeconds);
    result.setAllowed(isAllowed);
    result.setLimit(limit);
    result.setRemaining(remaining);
    result.setResetTimeSecond(resetTimeSeconds);
    return result;


}
@Override
    public AlgorithmType getType() {
     return AlgorithmType.FIXED_WINDOW;
}
}
