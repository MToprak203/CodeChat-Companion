package com.ai.assistant.persistence.relational.converter;

import com.ai.assistant.enums.Role;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@ReadingConverter
public class RoleSetReadConverter implements Converter<String, Set<Role>> {
    @Override
    public Set<Role> convert(String source) {
        if (source.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(source.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }
}
