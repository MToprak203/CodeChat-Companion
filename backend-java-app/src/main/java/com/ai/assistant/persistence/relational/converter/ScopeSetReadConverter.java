package com.ai.assistant.persistence.relational.converter;

import com.ai.assistant.enums.Scope;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@ReadingConverter
public class ScopeSetReadConverter implements Converter<String, Set<Scope>> {
    @Override
    public Set<Scope> convert(String source) {
        if (source.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(source.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(Scope::valueOf)
                .collect(Collectors.toSet());
    }
}
