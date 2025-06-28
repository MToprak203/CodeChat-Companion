package com.ai.assistant.core.validation;

import com.ai.assistant.core.error.ApplicationException;
import com.ai.assistant.core.error.ErrorCode;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@FunctionalInterface
public interface ReactiveValidator<T> {
    Mono<T> validate(T input);

    default ReactiveValidator<T> then(ReactiveValidator<T> next) {
        return input -> this.validate(input).flatMap(next::validate);
    }

    static <T> ReactiveValidator<T> from(Predicate<T> predicate, ErrorCode code, Object... args) {
        return input -> predicate.test(input)
                ? Mono.just(input)
                : Mono.error(new ApplicationException(code, args));
    }

    static <T> ReactiveValidator<T> fail(ErrorCode code, Object... args) {
        return input -> Mono.error(new ApplicationException(code, args));
    }
}