package com.ai.assistant.persistence.relational.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.stereotype.Component;

@Component
public class AuditorAwareProvider {

    private static ReactiveAuditorAware<Long> delegate;

    @Autowired
    public AuditorAwareProvider(ReactiveAuditorAware<Long> auditorAware) {
        delegate = auditorAware;
    }

    public static ReactiveAuditorAware<Long> getAuditorAware() {
        return delegate;
    }
}