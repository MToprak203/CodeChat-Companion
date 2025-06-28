package com.ai.assistant.usecase.resilience.wrapper;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
public abstract class ResilienceWrapper {
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;
    private final TimeLimiter timeLimiter;

    public <T> Mono<T> wrap(Mono<T> source) {
        logResilienceConfig("mono");

        Mono<T> result = source;
        if (retry != null) result = result.transformDeferred(RetryOperator.of(retry));
        if (circuitBreaker != null) result = result.transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
        if (timeLimiter != null) result = result.transformDeferred(TimeLimiterOperator.of(timeLimiter));

        return result.doOnError(e ->
                log.warn("[resilience:error] Exception during resilient operation: {}", e.toString()));
    }

    public <T> Flux<T> wrap(Flux<T> source) {
        logResilienceConfig("flux");

        Flux<T> result = source;
        if (retry != null) result = result.transformDeferred(RetryOperator.of(retry));
        if (circuitBreaker != null) result = result.transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
        if (timeLimiter != null) result = result.transformDeferred(TimeLimiterOperator.of(timeLimiter));

        return result.doOnError(e ->
                log.warn("[resilience:error] Exception during resilient operation: {}", e.toString()));
    }

    private void logResilienceConfig(String type) {
        String caller = resolveCaller();
        log.debug("[resilience:wrap:{}] Enabled -> retry={}, circuitBreaker={}, timeLimiter={}, calledBy={}",
                type,
                retry != null, circuitBreaker != null, timeLimiter != null,
                caller);
    }

    private String resolveCaller() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        for (StackTraceElement element : stack) {
            String className = element.getClassName();
            if (className.startsWith("com.ai.assistant") && !className.equals("com.ai.assistant.usecase.resilience.wrapper.ResilienceWrapper")) {
                return className + "#" + element.getMethodName();
            }
        }
        return "unknown";
    }
}
