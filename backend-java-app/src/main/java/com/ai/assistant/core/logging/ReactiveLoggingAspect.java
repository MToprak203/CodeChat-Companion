package com.ai.assistant.core.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Aspect
@Component
@Profile("!prod")
@Slf4j
public class ReactiveLoggingAspect {

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object logReactive(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        String name = ms.getDeclaringType().getSimpleName() + "." + ms.getName();
        Object[] args = pjp.getArgs();
        long start = System.currentTimeMillis();

        // proceed to get the returned value
        Object result = pjp.proceed();

        if (result instanceof Mono<?> mono) {
            return mono
                    .doOnSubscribe(s -> log.debug(">>> Entering {} with args={}", name, args))
                    .doOnSuccess(v -> {
                        long elapsed = System.currentTimeMillis() - start;
                        log.debug("<<< Exiting {}; returned={} took={}ms", name, v, elapsed);
                    })
                    .doOnError(e -> log.error("XXX Exception in {}: {}", name, e.getMessage(), e));
        }

        if (result instanceof Flux<?> flux) {
            return flux
                    .doOnSubscribe(s -> log.debug(">>> Entering {} with args={}", name, args))
                    .doOnComplete(() -> {
                        long elapsed = System.currentTimeMillis() - start;
                        log.debug("<<< Exiting {}; completed stream in {}ms", name, elapsed);
                    })
                    .doOnError(e -> log.error("XXX Exception in {}: {}", name, e.getMessage(), e));
        }

        // fall back to the original aspect for non-reactive returns
        log.debug(">>> Entering {} with args={}", name, args);
        Object ret = result;
        long duration = System.currentTimeMillis() - start;
        log.debug("<<< Exiting {}; returned={} took={}ms", name, ret, duration);
        return ret;
    }
}

