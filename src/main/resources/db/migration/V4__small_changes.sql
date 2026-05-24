
ALTER TABLE task_history
    ALTER COLUMN changed_at DROP NOT NULL;

ALTER TABLE task_comments
    ALTER COLUMN created_at DROP NOT NULL;

ALTER TABLE tasks
    ALTER COLUMN created_at DROP NOT NULL;

ALTER TABLE users
    ALTER COLUMN created_at DROP NOT NULL;

ALTER TABLE performance_records
    ALTER COLUMN period_end DROP NOT NULL;

ALTER TABLE attachments
    ALTER COLUMN uploaded_at DROP NOT NULL;