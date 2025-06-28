package com.ai.assistant.persistence.relational.converter;

import com.ai.assistant.enums.OutboxEventType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class OutboxEventTypeWriteConverter implements Converter<OutboxEventType, String> {
    @Override
    public String convert(OutboxEventType source) {
        return source.toString();
    }
}