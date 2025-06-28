package com.ai.assistant.persistence.relational.converter;

import com.ai.assistant.enums.ConversationType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class ConversationTypeWriteConverter implements Converter<ConversationType, String> {
    @Override
    public String convert(ConversationType source) {
        return source.toString();
    }
}