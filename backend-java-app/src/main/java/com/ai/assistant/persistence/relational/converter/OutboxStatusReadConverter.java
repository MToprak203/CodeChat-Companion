package com.ai.assistant.persistence.relational.converter;

import com.ai.assistant.enums.OutboxStatus;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class OutboxStatusReadConverter implements Converter<String, OutboxStatus> {
    @Override
    public OutboxStatus convert(@NonNull String source) {
        return OutboxStatus.valueOf(source);
    }
}
