package com.project.rateLimiter.middleware;

import com.project.rateLimiter.algorithm.AlgorithmRegistry;
import com.project.rateLimiter.algorithm.AlgorithmType;
import com.project.rateLimiter.algorithm.RateLimiterAlgorithm;
import com.project.rateLimiter.exception.RateLimitExceededException;
import com.project.rateLimiter.model.RateLimiterResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;

public class PreHandler implements HandlerInterceptor {

	private final AlgorithmRegistry algorithmRegistry;

	public PreHandler(AlgorithmRegistry algorithmRegistry) {
		this.algorithmRegistry = algorithmRegistry;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (!(handler instanceof HandlerMethod)) {
			return true;
		}
		HandlerMethod handlerMethod = (HandlerMethod) handler;
		RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
		if (rateLimit == null) {
			return true;
		}

		AlgorithmType algorithmType = rateLimit.algorithmType();
		long limit = rateLimit.limit();
		long windowSizeSecond = rateLimit.windowSize();
		String clientId = request.getRemoteAddr();

		RateLimiterAlgorithm algorithm = algorithmRegistry.getAlgorithm(algorithmType);
		RateLimiterResult result = algorithm.isAllowed(clientId, limit, windowSizeSecond);
		response.setHeader("X-Rate-Limit-Limit", String.valueOf(result.getLimit()));
		response.setHeader("X-Rate-Limit-Remaining", String.valueOf(result.getRemaining()));
		response.setHeader("reset time", String.valueOf(result.getResetTimeSecond()));
		if (! result.isAllowed()){
			long retryAfter =result.getResetTimeSecond() -  Instant.now().getEpochSecond();
			throw new RateLimitExceededException(retryAfter);
		}
		return  true;


	}
}