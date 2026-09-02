package com.project.rateLimiter.algorithm;

import com.project.rateLimiter.model.RateLimiterResult;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
@Component
public class TokenBucketAlgorithm  implements RateLimiterAlgorithm {

		private final StringRedisTemplate stringRedisTemplate;

		private final String LUA_SCRIPT = "local key = KEYS[1]\n" +
				"local capacity = tonumber(ARGV[1])\n" +
				"local refill_rate = tonumber(ARGV[2])\n" +
				"local current_time = tonumber(ARGV[3])\n" +
				"local bucket = redis.call('HMGET', key, 'tokens', 'last_time')\n" +
				"local tokens = tonumber(bucket[1]) or capacity \n" +
				"local last_time = tonumber(bucket[2]) or current_time \n " +
				"local time_passed = current_time - last_time\n" +
				"local new_tokens = time_passed * refill_rate\n" +
				"tokens = math.min(capacity, tokens + new_tokens)\n" +
				"local allowed = 0\n" +
				"if tokens >= 1 then\n" +
				"    tokens = tokens - 1           -- Take a token\n" +
				"    last_time = current_time      -- Update the time ONLY if allowed\n" +
				"    allowed = 1\n" +
				"end\n" +
				"redis.call('HMSET', key, 'tokens', tokens, 'last_time', last_time)\n" +
				"local expire_time = math.ceil(capacity / refill_rate) * 2\n" +
				"redis.call('EXPIRE', key, expire_time)\n" +
				"return {allowed, math.floor(tokens)}" ;
		public TokenBucketAlgorithm(StringRedisTemplate stringRedisTemplate) {
			this.stringRedisTemplate = stringRedisTemplate;
		}

		@Override
		public AlgorithmType getType(){

			return AlgorithmType.TOKEN_BUCKET;
		}


		@Override
		public RateLimiterResult isAllowed(String clientId , long limit , long windowSize) {
			double refillRate = (double) limit / windowSize;
			double currentTime = System.currentTimeMillis() / 1000.0;
			String redisKey = "RateLimit:Bucket:" + clientId;

			List<String> keys = Arrays.asList(redisKey);
			Object[] args = new Object[]{
					String.valueOf(limit),
					String.valueOf(refillRate),
					String.valueOf(currentTime)
			};
			DefaultRedisScript<List> redisScript = new DefaultRedisScript<>(LUA_SCRIPT, List.class);

			@SuppressWarnings("unchecked")
			List<Long> result = (List<Long>) stringRedisTemplate.execute(redisScript, keys, args);

			long allowed = result.get(0);
			long remainingTokens = result.get(1);

			long resetTimeSecond = (long) currentTime + windowSize;

			return new RateLimiterResult(allowed ==1 , limit, remainingTokens, resetTimeSecond);


		}
	}

