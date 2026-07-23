CREATE TABLE notification_settings
(
    user_id        BIGSERIAL PRIMARY KEY,
    retention_days INTEGER NOT NULL,
    CONSTRAINT fk_notification_settings_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE
);

CREATE TABLE notifications
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT                                  NOT NULL,
    type       VARCHAR(255)                            NOT NULL,
    payload    TEXT                                    NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    read_at    TIMESTAMP WITHOUT TIME ZONE,
    expires_at TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE
);

CREATE INDEX idx_notification_expires_at ON notifications (expires_at);
CREATE INDEX idx_notification_user ON notifications (user_id);