package com.ai.assistant.persistence.relational.entity;

import com.ai.assistant.enums.Role;
import com.ai.assistant.enums.Scope;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table("users")
public class User extends Auditable {
    @Id
    private Long id;
    private String username;
    private String password;
    private String email;

    private Set<Role> roles;
    private Set<Scope> scopes;

    private boolean enabled = true;
}
