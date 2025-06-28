CREATE SEQUENCE users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;

CREATE TABLE users
(
    id         BIGINT      NOT NULL DEFAULT nextval('users_id_seq') PRIMARY KEY,
    username   TEXT        NOT NULL,
    password   TEXT        NOT NULL,
    email      TEXT        NOT NULL,
    roles      TEXT[],
    scopes     TEXT[],
    enabled    BOOLEAN     NOT NULL DEFAULT TRUE,

    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_by BIGINT,
    deleted_at TIMESTAMPTZ,

    version    BIGINT      NOT NULL DEFAULT 0
);

ALTER SEQUENCE users_id_seq OWNED BY users.id;

CREATE UNIQUE INDEX unique_username_not_deleted
    ON users (username)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX unique_email_not_deleted
    ON users (email)
    WHERE deleted_at IS NULL;