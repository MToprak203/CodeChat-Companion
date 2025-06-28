package com.ai.assistant.mapper;

import com.ai.assistant.event.MessageEvent;
import com.ai.assistant.persistence.relational.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.time.LocalDateTime;
@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(target = "text", source = "content")
    @Mapping(target = "occurredAt", source = "sendDate")
    MessageEvent toDto(Message message);

    @Mapping(target = "content", source = "text")
    @Mapping(target = "sendDate", source = "occurredAt")
    Message toEntity(MessageEvent messageEvent);
}
