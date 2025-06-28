package com.ai.assistant.persistence.relational.converter;

import com.ai.assistant.enums.AggregateType;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class AggregateTypeReadConverter implements Converter<String, AggregateType> {
    @Override
    public AggregateType convert(@NonNull String source) {
        return AggregateType.valueOf(source);
    }
}