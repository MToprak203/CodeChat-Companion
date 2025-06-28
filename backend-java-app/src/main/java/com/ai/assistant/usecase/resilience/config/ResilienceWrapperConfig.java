package com.ai.assistant.usecase.resilience.config;

import com.ai.assistant.usecase.resilience.wrapper.AiServiceResilienceWrapper;
import com.ai.assistant.usecase.resilience.wrapper.DbResilienceWrapper;
import com.ai.assistant.usecase.resilience.wrapper.KafkaResilienceWrapper;
import com.ai.assistant.usecase.resilience.wrapper.RedisResilienceWrapper;
import com.ai.assistant.usecase.resilience.wrapper.ResilienceWrapper;
import com.ai.assistant.usecase.resilience.wrapper.WebClientResilienceWrapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.ai.assistant.core.Constants.Resilience.CircuitBreaker.AI_SERVICE_CIRCUIT_BREAKER;
import static com.ai.assistant.core.Constants.Resilience.CircuitBreaker.DB_CIRCUIT_BREAKER;
import static com.ai.assistant.core.Constants.Resilience.CircuitBreaker.REDIS_CIRCUIT_BREAKER;
import static com.ai.assistant.core.Constants.Resilience.CircuitBreaker.WS_CIRCUIT_BREAKER;
import static com.ai.assistant.core.Constants.Resilience.CircuitBreaker.WEB_CLIENT_CIRCUIT_BREAKER;
import static com.ai.assistant.core.Constants.Resilience.Retry.DB_RETRY;
import static com.ai.assistant.core.Constants.Resilience.Retry.KAFKA_RETRY;
import static com.ai.assistant.core.Constants.Resilience.Retry.REDIS_RETRY;
import static com.ai.assistant.core.Constants.Resilience.Retry.WS_RETRY;
import static com.ai.assistant.core.Constants.Resilience.Retry.WEB_CLIENT_RETRY;
import static com.ai.assistant.core.Constants.Resilience.TimeLimiter.*;
import static com.ai.assistant.core.Constants.Resilience.Wrapper.AI_SERVICE_RESILIENCE_WRAPPER;
import static com.ai.assistant.core.Constants.Resilience.Wrapper.DB_RESILIENCE_WRAPPER;
import static com.ai.assistant.core.Constants.Resilience.Wrapper.KAFKA_RESILIENCE_WRAPPER;
import static com.ai.assistant.core.Constants.Resilience.Wrapper.REDIS_RESILIENCE_WRAPPER;
import static com.ai.assistant.core.Constants.Resilience.Wrapper.WS_RESILIENCE_WRAPPER;
import static com.ai.assistant.core.Constants.Resilience.Wrapper.WEB_CLIENT_RESILIENCE_WRAPPER;

@Configuration
public class ResilienceWrapperConfig {

    @Bean(KAFKA_RESILIENCE_WRAPPER)
    public ResilienceWrapper kafkaResilienceWrapper(@Qualifier(KAFKA_RETRY) Retry retry) {
        return new KafkaResilienceWrapper(retry, null, null);
    }

    @Bean(REDIS_RESILIENCE_WRAPPER)
    public ResilienceWrapper redisResilienceWrapper(@Qualifier(REDIS_RETRY) Retry retry,
                                                    @Qualifier(REDIS_CIRCUIT_BREAKER) CircuitBreaker circuitBreaker,
                                                    @Qualifier(REDIS_TIME_LIMITER) TimeLimiter timeLimiter) {
        return new RedisResilienceWrapper(retry, circuitBreaker, timeLimiter);
    }

    @Bean(AI_SERVICE_RESILIENCE_WRAPPER)
    public ResilienceWrapper aiServiceResilienceWrapper(@Qualifier(AI_SERVICE_CIRCUIT_BREAKER) CircuitBreaker circuitBreaker,
                                                        @Qualifier(AI_SERVICE_TIME_LIMITER) TimeLimiter timeLimiter) {
        return new AiServiceResilienceWrapper(null, circuitBreaker, timeLimiter);
    }

    @Bean(DB_RESILIENCE_WRAPPER)
    public ResilienceWrapper dbResilienceWrapper(@Qualifier(DB_RETRY) Retry retry,
                                                 @Qualifier(DB_CIRCUIT_BREAKER) CircuitBreaker circuitBreaker,
                                                 @Qualifier(DB_TIME_LIMITER) TimeLimiter timeLimiter) {
        return new DbResilienceWrapper(retry, circuitBreaker, timeLimiter);
    }

    @Bean(WS_RESILIENCE_WRAPPER)
    public ResilienceWrapper wsResilienceWrapper(@Qualifier(WS_RETRY) Retry retry,
                                                 @Qualifier(WS_CIRCUIT_BREAKER) CircuitBreaker circuitBreaker,
                                                 @Qualifier(WS_TIME_LIMITER) TimeLimiter timeLimiter) {
        return new DbResilienceWrapper(retry, circuitBreaker, timeLimiter);
    }

    @Bean(WEB_CLIENT_RESILIENCE_WRAPPER)
    public ResilienceWrapper webClientResilienceWrapper(@Qualifier(WEB_CLIENT_RETRY) Retry retry,
                                                        @Qualifier(WEB_CLIENT_CIRCUIT_BREAKER) CircuitBreaker circuitBreaker,
                                                        @Qualifier(WEB_CLIENT_TIME_LIMITER) TimeLimiter timeLimiter) {
        return new WebClientResilienceWrapper(retry, circuitBreaker, timeLimiter);
    }
}
