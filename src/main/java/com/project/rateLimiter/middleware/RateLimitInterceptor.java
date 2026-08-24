package com.project.rateLimiter.middleware;

import com.project.rateLimiter.algorithm.FixedWindowAlgorithm;
import com.project.rateLimiter.exception.RateLimitExceededException;
import com.project.rateLimiter.model.RateLimiterResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
     private FixedWindowAlgorithm fixedWindowAlgorithm;
     private static  final long LIMIT = 5;
     private static  final long WINDDOW_SECOND = 60;

     public RateLimitInterceptor(FixedWindowAlgorithm fixedWindowAlgorithm) {
         this.fixedWindowAlgorithm = fixedWindowAlgorithm;
     }
     @Override
    public boolean preHandle(HttpServletRequest request , HttpServletResponse response  ,Object handler){
         String clientId = request.getRemoteAddr();
         RateLimiterResult result = fixedWindowAlgorithm.isAllowed(clientId , LIMIT , WINDDOW_SECOND);
         response.setHeader("X-Rate-Limiter-limit", String.valueOf(result.getLimit()));
         response.setHeader("X-Rate-Limiter-Remaining", String.valueOf(result.getRemaining()));
         response.setHeader("X-Rate-Limiter-Reset",String.valueOf(result.getResetTimeSecond()));
         if (!result.isAllowed()){
             Long currentTime = Instant.now().getEpochSecond();
             Long retryAfter = result.getResetTimeSecond() - currentTime;
             throw new RateLimitExceededException(retryAfter);
         }
         return true;
     }
}
