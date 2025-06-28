package com.ai.assistant.usecase.resilience.wrapper;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;

public class WebClientResilienceWrapper extends ResilienceWrapper {
    public WebClientResilienceWrapper(Retry retry, CircuitBreaker circuitBreaker, TimeLimiter timeLimiter) {
        super(retry, circuitBreaker, timeLimiter);
    }
}
