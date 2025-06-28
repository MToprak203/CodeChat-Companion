package com.ai.assistant.core.dbconnection;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryMetadata;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ai.assistant.core.Constants.DBReplication.ROUTE_KEY;
import static com.ai.assistant.core.Constants.DBReplication.ROUTE_PRIMARY;
import static com.ai.assistant.core.Constants.DBReplication.ROUTE_REPLICA;

@RequiredArgsConstructor
public class ReplicationRoutingConnectionFactory implements ConnectionFactory {
    private final ConnectionFactory primary;
    private final List<ConnectionFactory> replicas;
    private final AtomicInteger replicaIdx = new AtomicInteger();

    @Override
    public Publisher<? extends Connection> create() {
        return Mono.deferContextual(ctx -> {
            String route = ctx.getOrDefault(ROUTE_KEY, ROUTE_PRIMARY);
            ConnectionFactory target = ROUTE_REPLICA.equals(route) && !replicas.isEmpty()
                    ? replicas.get(replicaIdx.getAndIncrement() % replicas.size())
                    : primary;
            return Mono.from(target.create());
        });
    }

    @Override
    public ConnectionFactoryMetadata getMetadata() {
        return primary.getMetadata();
    }
}
