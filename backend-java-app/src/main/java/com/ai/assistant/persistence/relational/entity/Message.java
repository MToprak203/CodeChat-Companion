package com.ai.assistant.persistence.relational.entity;

import com.ai.assistant.enums.MessageType;
import com.ai.assistant.enums.RecipientType;
import com.ai.assistant.persistence.relational.entity.Auditable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table("messages")
public class Message extends Auditable {

    @Id
    private Long id;

    @Column("message_id")
    private UUID messageId;

    private String content;

    @Column("sender_id")
    private Long senderId;

    @Column("conversation_id")
    private Long conversationId;

    @Column("send_date")
    private LocalDateTime sendDate;

    private MessageType type;

    @Column("recipient")
    private RecipientType recipient;

    @Column("reply_to_message_id")
    private String replyToMessageId;

    // auditing fields are inherited from {@link Auditable}
}
