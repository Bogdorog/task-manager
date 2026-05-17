INSERT INTO permissions (name) VALUES
                                   ('MANAGE_ROLES'),
                                   ('VIEW_MEMBERS'),
                                   ('VIEW_ANALYTICS'),
                                   ('CHANGE_ALL_TASKS'),
                                   ('MANAGE_BOARD_COLUMNS');

CREATE TABLE IF NOT EXISTS boards (
                        id BIGSERIAL PRIMARY KEY,
                        company_id BIGINT NOT NULL,
                        name VARCHAR(255) NOT NULL,
                        description TEXT,
                        created_by BIGINT NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT NOW(),

                        CONSTRAINT fk_board_company
                            FOREIGN KEY (company_id)
                                REFERENCES companies(id)
                                ON DELETE CASCADE,

                        CONSTRAINT fk_board_creator
                            FOREIGN KEY (created_by)
                                REFERENCES users(id)
                                ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS board_columns (
                               id BIGSERIAL PRIMARY KEY,
                               board_id BIGINT NOT NULL,
                               name VARCHAR(100) NOT NULL,
                               position INT NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT NOW(),

                               CONSTRAINT fk_column_board
                                   FOREIGN KEY (board_id)
                                       REFERENCES boards(id)
                                       ON DELETE CASCADE
);

ALTER TABLE tasks DROP CONSTRAINT fk_task_company;
ALTER TABLE tasks DROP COLUMN company_id;

-- временно NULL для безопасного заполнения существующих строк
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS board_id BIGINT;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS column_id BIGINT;
-- Устанавливаем NOT NULL
ALTER TABLE tasks ALTER COLUMN board_id SET NOT NULL;
ALTER TABLE tasks ALTER COLUMN column_id SET NOT NULL;

ALTER TABLE tasks ADD CONSTRAINT fk_task_board
    FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE;

ALTER TABLE tasks ADD CONSTRAINT fk_task_column
    FOREIGN KEY (column_id) REFERENCES board_columns(id) ON DELETE CASCADE;

CREATE INDEX idx_tasks_column
    ON tasks(column_id);
CREATE INDEX idx_columns_board
    ON board_columns(board_id);