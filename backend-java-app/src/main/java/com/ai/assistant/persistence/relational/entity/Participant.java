package com.ai.assistant.persistence.relational.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table("conversation_participants")
public class Participant extends Auditable {
    @Id
    private Long id;

    @Column("conversation_id")
    private Long conversationId;

    @Column("user_id")
    private Long userId;


    @Column("joined_at")
    private Instant joinedAt;

    @Column("left_at")
    private Instant leftAt;

    @Column("last_read_at")
    private Instant lastReadAt;
}
