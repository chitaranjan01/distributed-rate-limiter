package com.project.rateLimiter.middleware;

import com.project.rateLimiter.algorithm.AlgorithmType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RateLimit {
	AlgorithmType algorithmType () default  AlgorithmType.FIXED_WINDOW;
	long limit () default 100;
	long windowSize () default 60;
 }
