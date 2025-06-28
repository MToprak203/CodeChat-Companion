package com.ai.assistant.mapper;

import com.ai.assistant.dto.response.conversation.ConversationResponseDTO;
import com.ai.assistant.persistence.relational.entity.Conversation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    ConversationResponseDTO toResponseDTO(Conversation conversation);
}
