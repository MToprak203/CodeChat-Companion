package com.ai.assistant.usecase.resilience.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.ai.assistant.core.Constants.Resilience.AI_SERVICE_NAME;
import static com.ai.assistant.core.Constants.Resilience.CircuitBreaker.AI_SERVICE_CIRCUIT_BREAKER;
import static com.ai.assistant.core.Constants.Resilience.CircuitBreaker.DB_CIRCUIT_BREAKER;
import static com.ai.assistant.core.Constants.Resilience.CircuitBreaker.REDIS_CIRCUIT_BREAKER;
import static com.ai.assistant.core.Constants.Resilience.CircuitBreaker.WS_CIRCUIT_BREAKER;
import static com.ai.assistant.core.Constants.Resilience.CircuitBreaker.WEB_CLIENT_CIRCUIT_BREAKER;
import static com.ai.assistant.core.Constants.Resilience.DB_NAME;
import static com.ai.assistant.core.Constants.Resilience.KAFKA_NAME;
import static com.ai.assistant.core.Constants.Resilience.REDIS_NAME;
import static com.ai.assistant.core.Constants.Resilience.Retry.DB_RETRY;
import static com.ai.assistant.core.Constants.Resilience.Retry.KAFKA_RETRY;
import static com.ai.assistant.core.Constants.Resilience.Retry.REDIS_RETRY;
import static com.ai.assistant.core.Constants.Resilience.Retry.WEB_CLIENT_RETRY;
import static com.ai.assistant.core.Constants.Resilience.Retry.WS_RETRY;
import static com.ai.assistant.core.Constants.Resilience.TimeLimiter.*;
import static com.ai.assistant.core.Constants.Resilience.WEB_CLIENT_NAME;
import static com.ai.assistant.core.Constants.Resilience.WS_NAME;

@Configuration
public class ResilienceConfig {

    // ###### KAFKA ######
    @Bean(KAFKA_RETRY)
    public Retry kafkaEventRetry(RetryRegistry retryRegistry) {
        return retryRegistry.retry(KAFKA_NAME);
    }

    // ###### Redis ######
    @Bean(REDIS_RETRY)
    public Retry redisEventRetry(RetryRegistry retryRegistry) {
        return retryRegistry.retry(REDIS_NAME);
    }

    @Bean(REDIS_CIRCUIT_BREAKER)
    public CircuitBreaker redisCircuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry) {
        return circuitBreakerRegistry.circuitBreaker(REDIS_NAME);
    }

    @Bean(REDIS_TIME_LIMITER)
    public TimeLimiter redisTimeLimiter(TimeLimiterRegistry timeLimiterRegistry) {
        return timeLimiterRegistry.timeLimiter(REDIS_NAME);
    }

    // ###### Ai Service ######
    @Bean(AI_SERVICE_CIRCUIT_BREAKER)
    public CircuitBreaker aiServiceCircuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry) {
        return circuitBreakerRegistry.circuitBreaker(AI_SERVICE_NAME);
    }

    @Bean(AI_SERVICE_TIME_LIMITER)
    public TimeLimiter aiServiceTimeLimiter(TimeLimiterRegistry timeLimiterRegistry) {
        return timeLimiterRegistry.timeLimiter(AI_SERVICE_NAME);
    }

    // ###### Web Client ######
    @Bean(WEB_CLIENT_RETRY)
    public Retry webClientCallRetry(RetryRegistry retryRegistry) {
        return retryRegistry.retry(WEB_CLIENT_NAME);
    }

    @Bean(WEB_CLIENT_CIRCUIT_BREAKER)
    public CircuitBreaker webClientCircuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry) {
        return circuitBreakerRegistry.circuitBreaker(WEB_CLIENT_NAME);
    }

    @Bean(WEB_CLIENT_TIME_LIMITER)
    public TimeLimiter webClientTimeLimiter(TimeLimiterRegistry timeLimiterRegistry) {
        return timeLimiterRegistry.timeLimiter(WEB_CLIENT_NAME);
    }

    // ###### DB ######
    @Bean(DB_RETRY)
    public Retry dbCallRetry(RetryRegistry retryRegistry) {
        return retryRegistry.retry(DB_NAME);
    }

    @Bean(DB_CIRCUIT_BREAKER)
    public CircuitBreaker dbCircuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry) {
        return circuitBreakerRegistry.circuitBreaker(DB_NAME);
    }

    @Bean(DB_TIME_LIMITER)
    public TimeLimiter dbTimeLimiter(TimeLimiterRegistry timeLimiterRegistry) {
        return timeLimiterRegistry.timeLimiter(DB_NAME);
    }

    // ###### WS ######
    @Bean(WS_RETRY)
    public Retry wsRetry(RetryRegistry retryRegistry) {
        return retryRegistry.retry(WS_NAME);
    }

    @Bean(WS_CIRCUIT_BREAKER)
    public CircuitBreaker wsCircuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry) {
        return circuitBreakerRegistry.circuitBreaker(WS_NAME);
    }

    @Bean(WS_TIME_LIMITER)
    public TimeLimiter wsTimeLimiter(TimeLimiterRegistry timeLimiterRegistry) {
        return timeLimiterRegistry.timeLimiter(WS_NAME);
    }
}
