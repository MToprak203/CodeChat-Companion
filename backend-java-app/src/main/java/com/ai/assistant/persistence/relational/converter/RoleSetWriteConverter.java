package com.ai.assistant.persistence.relational.converter;

import com.ai.assistant.enums.Role;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.util.Set;
import java.util.stream.Collectors;

@WritingConverter
public class RoleSetWriteConverter implements Converter<Set<Role>, String> {
    @Override
    public String convert(@NonNull Set<Role> source) {
        return source.isEmpty()
                ? ""
                : source.stream()
                .map(Role::name)
                .collect(Collectors.joining(","));
    }
}
