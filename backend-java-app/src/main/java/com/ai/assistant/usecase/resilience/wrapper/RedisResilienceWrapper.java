package com.ai.assistant.usecase.resilience.wrapper;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;

public class RedisResilienceWrapper extends ResilienceWrapper {
    public RedisResilienceWrapper(Retry retry, CircuitBreaker circuitBreaker, TimeLimiter timeLimiter) {
        super(retry, circuitBreaker, timeLimiter);
    }
}
