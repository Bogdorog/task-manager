CREATE TABLE account_delete_tokens
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT                                  NOT NULL,
    token      VARCHAR(255)                            NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE             NOT NULL
);

ALTER TABLE account_delete_tokens
    ADD CONSTRAINT uc_account_delete_tokens_token UNIQUE (token);

ALTER TABLE users
    ALTER COLUMN created_at DROP NOT NULL;