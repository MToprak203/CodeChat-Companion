package com.ai.assistant.persistence.relational.converter;

import com.ai.assistant.enums.OutboxEventType;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class OutboxEventTypeReadConverter implements Converter<String, OutboxEventType> {
    @Override
    public OutboxEventType convert(@NonNull String source) {
        return OutboxEventType.valueOf(source);
    }
}