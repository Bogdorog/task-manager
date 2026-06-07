-- Добавляем ссылку, на случай если сотрудник работает в нескольких компаниях
ALTER TABLE performance_records
    ADD COLUMN company_id BIGINT,
    ADD CONSTRAINT fk_perf_company FOREIGN KEY (company_id)
        REFERENCES companies(id) ON DELETE CASCADE;
-- У каждого столбца должна быть уникальная позиция в рамках доски
ALTER TABLE board_columns
    ADD CONSTRAINT uq_column_position UNIQUE (board_id, position);
CREATE INDEX idx_tasks_board ON tasks(board_id);
-- Убираем лишние ограничения
ALTER TABLE board_columns
    ALTER COLUMN created_at DROP NOT NULL;
ALTER TABLE boards
    ALTER COLUMN created_at DROP NOT NULL;
-- Запрещаем удаление при наличии носителей роли
ALTER TABLE company_memberships
    DROP CONSTRAINT fk_membership_role;
ALTER TABLE public.company_memberships
    ADD CONSTRAINT fk_membership_role
        FOREIGN KEY (role_id)
            REFERENCES public.company_roles (id)
            ON DELETE RESTRICT;
-- Центральная таблица медиафайлов
CREATE TABLE media_assets (
                              id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              original_name   VARCHAR(255)  NOT NULL,
                              stored_name     VARCHAR(255)  NOT NULL,
                              mime_type       VARCHAR(100)  NOT NULL,  -- image/jpeg, application/pdf ...
                              file_size       BIGINT        NOT NULL,  -- байты
                              uploaded_by     BIGINT        REFERENCES users(id) ON DELETE SET NULL,
                              created_at      TIMESTAMP     DEFAULT NOW()
);

-- Вложения к задачам — теперь ссылаются на media_assets
ALTER TABLE attachments DROP COLUMN file_url;
ALTER TABLE attachments
    ADD COLUMN media_asset_id UUID NOT NULL
        REFERENCES media_assets(id) ON DELETE CASCADE;

-- Аватар пользователя
ALTER TABLE users
    ADD CONSTRAINT fk_user_avatar
        FOREIGN KEY (avatar_media_id)
            REFERENCES media_assets(id)
            ON DELETE SET NULL;

-- Вложения в комментариях
CREATE TABLE comment_attachments (
                                     comment_id      BIGINT  NOT NULL REFERENCES task_comments(id) ON DELETE CASCADE,
                                     media_asset_id  UUID    NOT NULL REFERENCES media_assets(id)  ON DELETE CASCADE,
                                     PRIMARY KEY (comment_id, media_asset_id)
);