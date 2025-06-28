package com.ai.assistant.persistence.relational.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Auditable {

    @CreatedBy
    @Column("created_by")
    private Long createdById;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    @LastModifiedBy
    @Column("updated_by")
    private Long updatedById;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;

    @Column("deleted_by")
    private Long deletedById;

    @Column("deleted_at")
    private Instant deletedAt;

    /**
     * Optimistic-locking version field.
     * R2DBC will check this on updates to prevent lost updates.
     */
    @Version
    private Long version;
}
