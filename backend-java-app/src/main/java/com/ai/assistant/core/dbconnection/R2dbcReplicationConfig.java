package com.ai.assistant.core.dbconnection;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

import java.util.List;

import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
public class R2dbcReplicationConfig {

    @Bean
    @Primary
    public ConnectionFactory routingConnectionFactory(DataSourceProperties props) {
        // Build primary CF
        DataSourceProperties.Dsn p = props.getPrimary();
        ConnectionFactory primaryCf = ConnectionFactories.get(
                ConnectionFactoryOptions.parse(p.getUrl())
                        .mutate()
                        .option(USER, p.getUsername())
                        .option(PASSWORD, p.getPassword())
                        .build()
        );

        // Build replicas CFs
        List<ConnectionFactory> replicaCfs = props.getReplicas().stream()
                .map(d -> ConnectionFactories.get(
                        ConnectionFactoryOptions.parse(d.getUrl())
                                .mutate()
                                .option(USER, d.getUsername())
                                .option(PASSWORD, d.getPassword())
                                .build()
                ))
                .toList();

        return new ReplicationRoutingConnectionFactory(primaryCf, replicaCfs);
    }

    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory routingConnectionFactory) {
        return new R2dbcTransactionManager(routingConnectionFactory);
    }

    @Bean
    public R2dbcEntityTemplate r2dbcEntityTemplate(ConnectionFactory routingConnectionFactory) {
        return new R2dbcEntityTemplate(routingConnectionFactory);
    }
}
