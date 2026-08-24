package com.project.rateLimiter.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RateLimiterResult {

    private  boolean allowed;
    private long limit;
    private long remaining;
    private long resetTimeSecond;

    public  RateLimiterResult(boolean allowed, long limit, long remaining, long resetTimeSecond) {
  this.allowed = allowed;
  this.limit = limit;
  this.remaining = remaining;
  this.resetTimeSecond = resetTimeSecond;
    }
}
