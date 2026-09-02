package com.project.rateLimiter.algorithm;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Component
public class AlgorithmRegistry {
	private  final Map<AlgorithmType , RateLimiterAlgorithm> algorithms;

	public AlgorithmRegistry(List<RateLimiterAlgorithm> algorithmList ) {
		this.algorithms = new HashMap<>();

		for (RateLimiterAlgorithm algorithm1 : algorithmList) {
			AlgorithmType type = algorithm1.getType();
			algorithms.put(type, algorithm1);
		}
	}

	public RateLimiterAlgorithm getAlgorithm(AlgorithmType type) {
		RateLimiterAlgorithm algorithm = algorithms.get(type);
		if (algorithm == null) {
		throw new IllegalArgumentException("Algorithm Not Found" + type);
		}
		return algorithm;
	}

	public List<AlgorithmType> getAllRegisteredTypes(){
		return new ArrayList<>(algorithms.keySet());
	}
}
