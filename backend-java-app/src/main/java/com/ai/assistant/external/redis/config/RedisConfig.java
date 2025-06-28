package com.ai.assistant.external.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public ReactiveRedisTemplate<String, Long> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        RedisSerializationContext<String, Long> context = RedisSerializationContext
                .<String, Long>newSerializationContext(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .value(RedisSerializationContext.SerializationPair.fromSerializer(new GenericToStringSerializer<>(Long.class)))
                .build();
        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate(ReactiveRedisConnectionFactory factory) {
        RedisSerializationContext<String, String> context = RedisSerializationContext
                .<String, String>newSerializationContext(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .value(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .build();
        return new ReactiveRedisTemplate<>(factory, context);
    }
}
