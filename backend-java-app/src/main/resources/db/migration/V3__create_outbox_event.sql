CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE outbox_event
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id   UUID         NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    status         VARCHAR(100) NOT NULL,
    payload        TEXT         NOT NULL,
    published_at   TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);