package com.ai.assistant.core.dbconnection;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.datasource")
public class DataSourceProperties {

    private Dsn primary;
    private List<Dsn> replicas;

    @Getter
    @Setter
    public static class Dsn {
        private String url;
        private String username;
        private String password;
    }
}
