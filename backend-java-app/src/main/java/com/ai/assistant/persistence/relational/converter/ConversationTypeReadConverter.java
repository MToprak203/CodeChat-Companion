package com.ai.assistant.persistence.relational.converter;

import com.ai.assistant.enums.ConversationType;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class ConversationTypeReadConverter implements Converter<String, ConversationType> {
    @Override
    public ConversationType convert(@NonNull String source) {
        return ConversationType.valueOf(source);
    }
}