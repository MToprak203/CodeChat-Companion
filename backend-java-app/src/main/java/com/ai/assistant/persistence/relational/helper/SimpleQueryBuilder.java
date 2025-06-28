package com.ai.assistant.persistence.relational.helper;

import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Criteria;

public record SimpleQueryBuilder(Criteria criteria, Sort sort) implements QueryBuilder {

    public static SimpleQueryBuilder of(Criteria criteria) {
        return new SimpleQueryBuilder(criteria, Sort.unsorted());
    }

    public static SimpleQueryBuilder of(Criteria criteria, Sort sort) {
        return new SimpleQueryBuilder(criteria, sort);
    }

    @Override public Criteria getCriteria() { return criteria; }
    @Override public Sort getSort() { return sort; }
}