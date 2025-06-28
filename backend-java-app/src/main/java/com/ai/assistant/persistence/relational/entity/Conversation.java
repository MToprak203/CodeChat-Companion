package com.ai.assistant.persistence.relational.entity;

import com.ai.assistant.enums.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table("conversations")
public class Conversation extends Auditable {
    @Id
    private Long id;
    private ConversationType type;
    private String title;
    @Column("project_id")
    private Long projectId;
}
