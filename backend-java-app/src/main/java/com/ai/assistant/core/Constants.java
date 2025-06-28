package com.ai.assistant.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {
    public static class Correlation {
        public static final String HEADER_CORRELATION_PARAM = "X-Correlation-Id";
        public static final String CONTEXT_CORRELATION_KEY = "correlationId";
    }

    public static class JWT {
        public static final String CLAIM_USER_ID = "user_id";
        public static final String CLAIM_ROLES = "roles";
        public static final String CLAIM_SCOPES = "scopes";
    }

    public static class DBReplication {
        public static final String ROUTE_KEY = "DB_ROUTE";
        public static final String ROUTE_PRIMARY = "PRIMARY";
        public static final String ROUTE_REPLICA = "REPLICA";
    }

    public static class AI {
        public static final Long AI_USER_ID = -1L;
        public static final String AI_FALLBACK_MESSAGE = "AI is currently unavailable. Please try again later.";
    }

    public static class WebSocket {
        public static final String WS_PATH_CONVERSATION_MESSAGES =
                "/ws/{userId}/conversations/{conversationId}/messages";

        public static final String WS_PATH_CONVERSATION_AI_TOKENS =
                "/ws/{userId}/conversations/{conversationId}/tokens";

        public static final String WS_PATH_CONVERSATION_USER_NOTIFICATIONS =
                "/ws/{userId}/conversations/{conversationId}/notify";

        public static final String WS_PATH_CONVERSATION_GROUP_NOTIFICATIONS =
                "/ws/{userId}/conversations/{conversationId}/group-notify";

        public static final String WS_PATH_SYSTEM_NOTIFICATIONS =
                "/ws/{userId}/notifications";

        public static final String WS_PATH_PROJECT_SELECTED_FILES =
                "/ws/{userId}/projects/{projectId}/selected-files";
    }

    public static class Outbox {
        public static final int OUTBOX_BATCH_SIZE = 100;
    }

    public static class Resilience {
        public static final String AI_SERVICE_NAME = "ai-service";
        public static final String REDIS_NAME = "redis";
        public static final String KAFKA_NAME = "kafka";
        public static final String WEB_CLIENT_NAME = "web-client";
        public static final String DB_NAME = "db";
        public static final String WS_NAME = "ws";

        public static class Retry {
            public static final String AI_SERVICE_RETRY = "ai-service-retry";
            public static final String REDIS_RETRY = "redis-retry";
            public static final String KAFKA_RETRY = "kafka-retry";
            public static final String WEB_CLIENT_RETRY = "web-client-retry";
            public static final String DB_RETRY = "db-retry";
            public static final String WS_RETRY = "ws-retry";
        }

        public static class CircuitBreaker {
            public static final String AI_SERVICE_CIRCUIT_BREAKER = "ai-service-circuit-breaker";
            public static final String REDIS_CIRCUIT_BREAKER = "redis-circuit-breaker";
            public static final String KAFKA_CIRCUIT_BREAKER = "kafka-circuit-breaker";
            public static final String WEB_CLIENT_CIRCUIT_BREAKER = "web-client-circuit-breaker";
            public static final String DB_CIRCUIT_BREAKER = "db-circuit-breaker";
            public static final String WS_CIRCUIT_BREAKER = "ws-circuit-breaker";
        }

        public static class TimeLimiter {
            public static final String AI_SERVICE_TIME_LIMITER = "ai-service-time-limiter";
            public static final String REDIS_TIME_LIMITER = "redis-time-limiter";
            public static final String KAFKA_TIME_LIMITER = "kafka-time-limiter";
            public static final String WEB_CLIENT_TIME_LIMITER = "web-client-time-limiter";
            public static final String DB_TIME_LIMITER = "db-time-limiter";
            public static final String WS_TIME_LIMITER = "ws-time-limiter";
        }

        public static class Wrapper {
            public static final String AI_SERVICE_RESILIENCE_WRAPPER = "ai-service-wrapper";
            public static final String REDIS_RESILIENCE_WRAPPER = "redis-wrapper";
            public static final String KAFKA_RESILIENCE_WRAPPER = "kafka-wrapper";
            public static final String WEB_CLIENT_RESILIENCE_WRAPPER = "web-client-wrapper";
            public static final String DB_RESILIENCE_WRAPPER = "db-wrapper";
            public static final String WS_RESILIENCE_WRAPPER = "ws-wrapper";
        }
    }

    public static class Path {
        public static final String API_BASE = "/api";

        public static class Version {
            public static final String V1 = "/v1";
        }

        public static class Auth {
            public static final String BASE = "/auth";
            public static final String AUTH_V1 = API_BASE + Version.V1 + BASE;

            public static class Method {
                public static final String LOGIN = "/login";
                public static final String REGISTER = "/register";
                public static final String LOGOUT = "/logout";
            }
        }

        public static class Conversation {
            public static final String BASE = "/conversations";
            public static final String CONVERSATION_V1 = API_BASE + Version.V1 + BASE;

        public static class Method {
            public static final String AI = "/ai";
            public static final String DIRECT = "/direct";
            public static final String GROUP = "/group";
            public static final String ACTIVE = "/active";
            public static final String NO_PROJECT = "/no-project";
        }

            public static class Message {
                public static final String BASE = "/{conversationId}/messages";
                public static final String UNREAD = "/unread";
            }

            public static class Notification {
                public static final String BASE = "/{conversationId}/notifications";
            }

            public static class Participant {
                public static final String BASE = "/{conversationId}/participants";
                public static final String ACTIVE = BASE + "/active";
                public static final String USER = BASE + "/{userId}";
            }

            public static class Stop {
                public static final String BASE = "/{conversationId}/stop";
            }
        }

        public static class Project {
            public static final String BASE = "/projects";
            public static final String PROJECT_V1 = API_BASE + Version.V1 + BASE;

            public static class Method {
                public static final String TREE = "/{projectId}/tree";
                public static final String SYNC = "/{projectId}/sync";
                public static final String CONVERSATION = "/{projectId}/conversation";
                public static final String SELECTED_FILES = "/{projectId}/selected-files";
                public static final String FILE = "/{projectId}/file";
            }
        }

    }
}
