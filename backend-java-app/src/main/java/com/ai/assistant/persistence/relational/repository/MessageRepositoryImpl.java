package com.ai.assistant.persistence.relational.repository;

import com.ai.assistant.persistence.relational.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository
@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepositoryCustom {
    private final R2dbcEntityTemplate template;

    @Override
    public Flux<Message> findByConversationId(Long conversationId, int offset, int limit) {
        Query query = Query.query(
                Criteria.where("conversation_id").is(conversationId)
                        .and("deleted_at").isNull()
        )
                .sort(Sort.by(Sort.Order.desc("send_date")))
                .limit(limit)
                .offset(offset);

        return template.select(query, Message.class);
    }

    @Override
    public Mono<Long> countByConversationId(Long conversationId) {
        Query query = Query.query(
                Criteria.where("conversation_id").is(conversationId)
                        .and("deleted_at").isNull()
        );
        return template.count(query, Message.class);
    }

    @Override
    public Flux<Message> findUnreadMessages(Long conversationId, java.time.LocalDateTime since) {
        Criteria criteria = Criteria.where("conversation_id").is(conversationId)
                .and("deleted_at").isNull();
        if (since != null) {
            criteria = criteria.and("send_date").greaterThan(since);
        }
        Query query = Query.query(criteria)
                .sort(Sort.by(Sort.Order.desc("send_date")));
        return template.select(query, Message.class);
    }
}
