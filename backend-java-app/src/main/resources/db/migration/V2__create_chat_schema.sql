CREATE TABLE conversations
(
    id         BIGSERIAL PRIMARY KEY,
    type       TEXT        NOT NULL DEFAULT 'PRIVATE',
    title      TEXT        NOT NULL DEFAULT ('Conversation_' || to_char(now(), 'YYYYMMDD_HH24MI')),
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_by BIGINT,
    deleted_at TIMESTAMPTZ,
    version    BIGINT      NOT NULL DEFAULT 0
);

CREATE TABLE messages
(
    id                  BIGSERIAL PRIMARY KEY,
    message_id          UUID        NOT NULL UNIQUE,
    content             TEXT        NOT NULL,
    sender_id           BIGINT      NOT NULL,
    conversation_id     BIGINT      NOT NULL,
    send_date           TIMESTAMP   NOT NULL,
    type                VARCHAR(30) NOT NULL,
    reply_to_message_id UUID,
    created_by          BIGINT,
    created_at          TIMESTAMP DEFAULT now(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP DEFAULT now(),
    deleted_by          BIGINT,
    deleted_at          TIMESTAMP,
    version             BIGINT   NOT NULL DEFAULT 0
);

CREATE INDEX idx_messages_conversation_id ON messages (conversation_id);
CREATE INDEX idx_messages_message_id ON messages (message_id);
CREATE INDEX idx_messages_sender_id ON messages (sender_id);

CREATE TABLE conversation_participants
(
    id              BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT      NOT NULL REFERENCES conversations (id),
    user_id         BIGINT      NOT NULL,
    is_ai           BOOLEAN     NOT NULL DEFAULT false,
    joined_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    left_at         TIMESTAMPTZ,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_by      BIGINT,
    deleted_at      TIMESTAMPTZ,
    version         BIGINT      NOT NULL DEFAULT 0
);

CREATE INDEX idx_messages_conv
    ON messages (conversation_id);


CREATE INDEX idx_conversation_participants_conv
    ON conversation_participants (conversation_id);
