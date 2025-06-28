package com.ai.assistant.core.dbconnection;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.ai.assistant.core.Constants.DBReplication.ROUTE_KEY;
import static com.ai.assistant.core.Constants.DBReplication.ROUTE_REPLICA;

@Aspect
@Component
public class ReadOnlyAspect {

    @Around("@annotation(com.ai.assistant.core.dbconnection.ReadOnly)")
    public Object routeToReplica(ProceedingJoinPoint pjp) throws Throwable {
        Object ret = pjp.proceed();

        if (ret instanceof Mono<?> mono) {
            return mono.contextWrite(ctx -> ctx.put(ROUTE_KEY, ROUTE_REPLICA));
        }
        if (ret instanceof Flux<?> flux) {
            return flux.contextWrite(ctx -> ctx.put(ROUTE_KEY, ROUTE_REPLICA));
        }
        return ret;
    }
}
