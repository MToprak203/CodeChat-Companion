package com.ai.assistant.persistence.relational.converter;

import com.ai.assistant.enums.Scope;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.util.Set;
import java.util.stream.Collectors;

@WritingConverter
public class ScopeSetWriteConverter implements Converter<Set<Scope>, String> {
    @Override
    public String convert(Set<Scope> source) {
        return source.isEmpty()
                ? ""
                :source.stream()
                .map(Scope::name)
                .collect(Collectors.joining(","));
    }
}
