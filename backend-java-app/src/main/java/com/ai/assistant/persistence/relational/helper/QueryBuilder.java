package com.ai.assistant.persistence.relational.helper;

import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Criteria;

public interface QueryBuilder {
    Criteria getCriteria();
    Sort getSort();
}