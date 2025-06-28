package com.ai.assistant.persistence.relational.converter;

import com.ai.assistant.enums.OutboxStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class OutboxStatusWriteConverter implements Converter<OutboxStatus, String> {
    @Override
    public String convert(OutboxStatus source) {
        return source.toString();
    }
}
