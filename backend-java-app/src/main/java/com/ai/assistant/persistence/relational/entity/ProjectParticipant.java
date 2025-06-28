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
@Table("project_participants")
public class ProjectParticipant extends Auditable {
    @Id
    private Long id;

    @Column("project_id")
    private Long projectId;

    @Column("user_id")
    private Long userId;

    @Column("joined_at")
    private Instant joinedAt;

    @Column("left_at")
    private Instant leftAt;
}
