package com.ai.assistant.persistence.relational.entity;

import com.ai.assistant.enums.AggregateType;
import com.ai.assistant.enums.OutboxEventType;
import com.ai.assistant.enums.OutboxStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table("outbox_event")
public class OutboxEvent {
    @Id
    private UUID id;

    @Column("aggregate_id")
    private UUID aggregateId;

    @Column("aggregate_type")
    private AggregateType aggregateType;

    @Column("event_type")
    private OutboxEventType eventType;

    private OutboxStatus status;

    private String payload;

    @Column("published_at")
    private Instant publishedAt;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;
}
