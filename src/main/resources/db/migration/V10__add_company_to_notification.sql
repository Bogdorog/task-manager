ALTER TABLE notifications
    ADD COLUMN company_id BIGINT NULL;

CREATE INDEX idx_notification_company ON notifications (user_id, company_id);