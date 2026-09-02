package com.project.rateLimiter.controller;

import com.project.rateLimiter.algorithm.AlgorithmRegistry;
import com.project.rateLimiter.algorithm.AlgorithmType;
import com.project.rateLimiter.algorithm.FixedWindowAlgorithm;
import com.project.rateLimiter.algorithm.SlidingWindowAlgorithm;
import com.project.rateLimiter.middleware.RateLimit;
import com.project.rateLimiter.model.RateLimiterResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {
    private final FixedWindowAlgorithm fixedWindowAlgorithm;
    private final SlidingWindowAlgorithm slidingWindowAlgorithm;
    private final AlgorithmRegistry algorithmRegistry;

    public TestController(FixedWindowAlgorithm fixedWindowAlgorithm, SlidingWindowAlgorithm slidingWindowAlgorithm, AlgorithmRegistry algorithmRegistry ) {
        this.fixedWindowAlgorithm = fixedWindowAlgorithm;
        this.slidingWindowAlgorithm = slidingWindowAlgorithm;
        this.algorithmRegistry = algorithmRegistry;
    }

    @GetMapping("/hello")
    public String hello() {
        return "testing  run perfect   !! ";
    }

    @GetMapping("/ratelimitresult")
    public RateLimiterResult ratelimitresult(@RequestParam(defaultValue = "192.168.0.100") String clientId,
                                             @RequestParam(defaultValue = "5") Long limit,
                                             @RequestParam(defaultValue = "60") Long windowSecond) {

        return fixedWindowAlgorithm.isAllowed(clientId, limit, windowSecond);

    }

    @GetMapping("/Slidingwindow")
    public RateLimiterResult rateLimiterResult(
            @RequestParam(defaultValue = "testuser") String clientId,
            @RequestParam(defaultValue = "5") long limit,
            @RequestParam(defaultValue = "60") long windowSecond) {
        return slidingWindowAlgorithm.isAllowed(clientId, limit, windowSecond);
    }


    @RateLimit(algorithmType = AlgorithmType.SLIDING_WINDOW, limit = 3, windowSize = 60)
    @GetMapping("/slidingwindow")
    public String slidingwindow() {
        return "testing  run perfect   for slidingwindow  !! ";
    }

    @RateLimit(algorithmType = AlgorithmType.FIXED_WINDOW, limit = 10, windowSize = 60)
    @GetMapping("/fixedwindow")
    public String fixedwindow()
    {
        return "testing  run perfect   for fixedwindow  !! ";
    }

    @RateLimit(algorithmType = AlgorithmType.TOKEN_BUCKET , limit = 10 ,windowSize = 60)
    @GetMapping("/tokenbased")
    public String tokenbased (){
        return "testing  run perfect  for tokenbased   !! ";
    }
}