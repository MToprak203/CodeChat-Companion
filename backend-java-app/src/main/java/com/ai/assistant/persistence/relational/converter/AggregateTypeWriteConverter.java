package com.ai.assistant.persistence.relational.converter;

import com.ai.assistant.enums.AggregateType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class AggregateTypeWriteConverter implements Converter<AggregateType, String> {
    @Override
    public String convert(AggregateType source) {
        return source.toString();
    }
}