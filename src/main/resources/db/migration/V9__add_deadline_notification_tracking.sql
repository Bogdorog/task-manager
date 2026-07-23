ALTER TABLE tasks
    ADD COLUMN deadline_warning_sent BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN deadline_overdue_sent BOOLEAN NOT NULL DEFAULT false;

CREATE INDEX idx_tasks_due_date ON tasks (due_date) WHERE due_date IS NOT NULL;