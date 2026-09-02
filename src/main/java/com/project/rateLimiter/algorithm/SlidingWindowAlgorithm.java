package com.project.rateLimiter.algorithm;

import com.project.rateLimiter.model.RateLimiterResult;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class SlidingWindowAlgorithm implements RateLimiterAlgorithm {
	private final StringRedisTemplate stringRedisTemplate;

	private static  final  String LUA_SCRIPT = "-- Step 1: Calculate when the window started (current time - window size)\n" +
			"local windowStart = tonumber(ARGV[1]) - tonumber(ARGV[2])\n" +
			"\n" +
			"-- Step 2: Remove all entries older than windowStart\n" +
			"redis.call('ZREMRANGEBYSCORE', KEYS[1], '-inf', windowStart)\n" +
			"\n" +
			"-- Step 3: Count how many requests are in the current window\n" +
			"local currentCount = redis.call('ZCARD', KEYS[1])\n" +
			"\n" +
			"-- Step 4: Check if we're under the limit\n" +
			"local limit = tonumber(ARGV[3])\n" +
			"local allowed = 0\n" +
			"local remaining = 0\n" +
			"\n" +
			"if currentCount < limit then\n" +
			"    -- ALLOWED: Add the new request to the Sorted Set\n" +
			"    redis.call('ZADD', KEYS[1], ARGV[1], ARGV[4])\n" +
			"    allowed = 1\n" +
			"    remaining = limit - currentCount - 1\n" +
			"    \n" +
			"    -- Set TTL to windowSize * 2 (auto-delete if client goes quiet)\n" +
			"    redis.call('PEXPIRE', KEYS[1], tonumber(ARGV[2]) * 2)\n" +
			"else\n" +
			"    -- BLOCKED: Don't add the request\n" +
			"    remaining = 0\n" +
			"end\n" +
			"\n" +
			"-- Step 5: Calculate window end and return results\n" +
			"local windowEnd = windowStart + tonumber(ARGV[2])\n" +
			"return {allowed, limit, remaining, windowStart, windowEnd}";

	public SlidingWindowAlgorithm(StringRedisTemplate stringRedisTemplate) {
		this.stringRedisTemplate = stringRedisTemplate;
	}
	@Override
	public RateLimiterResult isAllowed(String clientId , long limit , long windowSizeSecond){
		long currentTimeMillis = Instant.now().toEpochMilli();
		long windowSizeMillis = windowSizeSecond * 1000;
		String memberId = currentTimeMillis +":" + UUID.randomUUID();
		String redisKey ="ratelimit:log:" + clientId;
		List<String> keys = Arrays.asList(redisKey);
		Object[] args =new Object[]{
				String.valueOf(currentTimeMillis),
				String.valueOf(windowSizeMillis),
				String.valueOf(limit),
				memberId
		};
		DefaultRedisScript<List> script = new DefaultRedisScript<>(LUA_SCRIPT, List.class);
		@SuppressWarnings("unchecked")
				List<Long> result =(List<Long>)  stringRedisTemplate.execute(script, keys, args);
		long allowed = result.get(0);
		long returnedLimit = result.get(1);
		long remaining = result.get(2);
		long windowStart = result.get(3);
		long windowEnd = result.get(4);
		long resetTimeSecond = windowEnd/1000;

		RateLimiterResult result1 = new RateLimiterResult(allowed ==1 , limit ,remaining, resetTimeSecond);
		result1.setAllowed(allowed ==1 );
		result1.setLimit(returnedLimit);
		result1.setRemaining(remaining);
		result1.setResetTimeSecond(resetTimeSecond);
		return result1;
	}
	 @Override
	public AlgorithmType getType() {
		return AlgorithmType.SLIDING_WINDOW;
	 }
}
