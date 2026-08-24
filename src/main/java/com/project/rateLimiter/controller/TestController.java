package com.project.rateLimiter.controller;

import com.project.rateLimiter.algorithm.FixedWindowAlgorithm;
import com.project.rateLimiter.model.RateLimiterResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {
    private final FixedWindowAlgorithm fixedWindowAlgorithm;
    public TestController(FixedWindowAlgorithm fixedWindowAlgorithm) {
        this.fixedWindowAlgorithm = fixedWindowAlgorithm;
    }
    @GetMapping("/hello")
    public String hello() {
         return "testing  run perfect   !! ";
    }
    @GetMapping("/ratelimitresult")
    public RateLimiterResult ratelimitresult( @RequestParam(defaultValue = "192.168.0.100") String clientId,
                                              @RequestParam(defaultValue = "5") Long limit,
                                              @RequestParam(defaultValue = "60") Long windowSecond) {

        return fixedWindowAlgorithm.isAllowed(clientId ,limit ,windowSecond);

    }
}
