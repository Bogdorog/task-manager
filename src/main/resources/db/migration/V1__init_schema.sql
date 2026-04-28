-- =========================
-- Таблица пользователей
-- =========================
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    login           VARCHAR(255)                            NOT NULL UNIQUE,
    full_name       VARCHAR(255)                            NOT NULL,
    role_id         BIGINT                                  NOT NULL,
    avatar_media_id UUID,
    address         VARCHAR(255)                            NOT NULL,
    email           VARCHAR(255)                            NOT NULL UNIQUE,
    phone           VARCHAR(15)                             NOT NULL UNIQUE,
    password_hash   VARCHAR(255)                            NOT NULL,
    active          BOOLEAN                                 NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- =========================
-- Таблица компаний
-- =========================
CREATE TABLE companies (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(255) NOT NULL,
                           description TEXT,
                           email VARCHAR(255),
                           phone VARCHAR(15),
                           address TEXT,
                           created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                           updated_at TIMESTAMP
);

-- =========================
-- Роли внутри компании
-- =========================
CREATE TABLE company_roles (
                               id BIGSERIAL PRIMARY KEY,
                               company_id BIGINT NOT NULL,
                               name VARCHAR(100) NOT NULL,
                               description TEXT,
                               created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                               CONSTRAINT fk_company_roles_company
                                   FOREIGN KEY (company_id)
                                       REFERENCES companies(id)
                                       ON DELETE CASCADE,
                               CONSTRAINT unique_role_per_company UNIQUE (company_id, name)
);

-- =========================
-- Возможные разрешения для ролей внутри компании
-- =========================
CREATE TABLE permissions (
                             id BIGSERIAL PRIMARY KEY,
                             name VARCHAR(100) NOT NULL UNIQUE
);

-- =========================
-- Связь между ролью и разрешениями (M:N)
-- =========================
CREATE TABLE role_permissions (
                                  role_id BIGINT NOT NULL,
                                  permission_id BIGINT NOT NULL,
                                  PRIMARY KEY (role_id, permission_id),
                                  CONSTRAINT fk_role_permissions_role
                                      FOREIGN KEY (role_id)
                                          REFERENCES company_roles(id)
                                          ON DELETE CASCADE,
                                  CONSTRAINT fk_role_permissions_permission
                                      FOREIGN KEY (permission_id)
                                          REFERENCES permissions(id)
                                          ON DELETE CASCADE
);

-- =========================
-- Связь между пользователем и компанией
-- =========================
CREATE TABLE company_memberships (
                                     id BIGSERIAL PRIMARY KEY,
                                     user_id BIGINT NOT NULL,
                                     company_id BIGINT NOT NULL,
                                     role_id BIGINT NOT NULL,
                                     joined_at TIMESTAMP NOT NULL DEFAULT NOW(),

                                     CONSTRAINT fk_membership_user
                                         FOREIGN KEY (user_id)
                                         REFERENCES users(id)
                                         ON DELETE CASCADE,

                                     CONSTRAINT fk_membership_company
                                         FOREIGN KEY (company_id)
                                         REFERENCES companies(id)
                                         ON DELETE CASCADE,

                                     CONSTRAINT fk_membership_role
                                         FOREIGN KEY (role_id)
                                         REFERENCES company_roles(id)
                                         ON DELETE CASCADE,

                                     CONSTRAINT unique_user_company UNIQUE (user_id, company_id)

);

-- =========================
-- Задачи
-- =========================
CREATE TABLE tasks (
                       id BIGSERIAL PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       description TEXT,
                       status VARCHAR(50) NOT NULL,
                       priority VARCHAR(50) NOT NULL,
                       assigned_to BIGINT,
                       created_by BIGINT NOT NULL,
                       company_id BIGINT NOT NULL,
                       due_date TIMESTAMP,
                       created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                       updated_at TIMESTAMP,

                       CONSTRAINT fk_task_assigned_to
                           FOREIGN KEY (assigned_to)
                           REFERENCES users(id)
                           ON DELETE SET NULL,

                       CONSTRAINT fk_task_created_by
                           FOREIGN KEY (created_by)
                           REFERENCES users(id)
                           ON DELETE CASCADE,

                       CONSTRAINT fk_task_company
                           FOREIGN KEY (company_id)
                           REFERENCES companies(id)
                           ON DELETE CASCADE

);

-- =========================
-- Комментарии к задачам
-- =========================
CREATE TABLE task_comments (
                               id BIGSERIAL PRIMARY KEY,
                               task_id BIGINT NOT NULL,
                               user_id BIGINT NOT NULL,
                               comment_text TEXT NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT NOW(),

                               CONSTRAINT fk_comment_task
                                   FOREIGN KEY (task_id)
                                   REFERENCES tasks(id)
                                   ON DELETE CASCADE,

                               CONSTRAINT fk_comment_user
                                   FOREIGN KEY (user_id)
                                   REFERENCES users(id)
                                   ON DELETE CASCADE

);

-- =========================
-- Вложения
-- =========================
CREATE TABLE attachments (
                             id BIGSERIAL PRIMARY KEY,
                             task_id BIGINT NOT NULL,
                             file_url TEXT NOT NULL,
                             uploaded_by BIGINT NOT NULL,
                             uploaded_at TIMESTAMP NOT NULL DEFAULT NOW(),

                             CONSTRAINT fk_attachment_task
                                 FOREIGN KEY (task_id)
                                 REFERENCES tasks(id)
                                 ON DELETE CASCADE,

                             CONSTRAINT fk_attachment_user
                                 FOREIGN KEY (uploaded_by)
                                 REFERENCES users(id)
                                 ON DELETE CASCADE

);

-- =========================
-- История изменения задачи
-- =========================
CREATE TABLE task_history (
                              id BIGSERIAL PRIMARY KEY,
                              task_id BIGINT NOT NULL,
                              changed_by BIGINT NOT NULL,
                              field_name VARCHAR(100) NOT NULL,
                              old_value TEXT,
                              new_value TEXT,
                              changed_at TIMESTAMP NOT NULL DEFAULT NOW(),

                              CONSTRAINT fk_history_task
                                  FOREIGN KEY (task_id)
                                  REFERENCES tasks(id)
                                  ON DELETE CASCADE,

                              CONSTRAINT fk_history_user
                                  FOREIGN KEY (changed_by)
                                  REFERENCES users(id)
                                  ON DELETE CASCADE

);

-- =========================
-- Заметки о производительности
-- =========================
CREATE TABLE performance_records (
                                     id BIGSERIAL PRIMARY KEY,
                                     user_id BIGINT NOT NULL,
                                     tasks_completed_count INT DEFAULT 0,
                                     average_completion_time DOUBLE PRECISION,
                                     period_start TIMESTAMP NOT NULL,
                                     period_end TIMESTAMP NOT NULL,

                                     CONSTRAINT fk_performance_user
                                         FOREIGN KEY (user_id)
                                         REFERENCES users(id)
                                         ON DELETE CASCADE

);

-- =========================
-- Токены для смены пароля
-- =========================
CREATE TABLE password_reset_tokens
(
    id         BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    user_id    BIGINT                                  NOT NULL,
    token_hash VARCHAR(255)                            NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    used       BOOLEAN                                 NOT NULL DEFAULT false,
    CONSTRAINT pk_password_reset_tokens PRIMARY KEY (id)
);

-- =========================
-- Токены обновления
-- =========================
CREATE TABLE refresh_tokens
(
    id         BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    token      VARCHAR(255)                            NOT NULL,
    user_id    BIGINT                                  NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id)
);

-- =========================
-- Роли вне компании
-- =========================
CREATE TABLE roles
(
    id   BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name VARCHAR(255)                            NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id)
);

ALTER TABLE password_reset_tokens
    ADD CONSTRAINT uc_password_reset_tokens_tokenhash UNIQUE (token_hash);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT uc_refresh_tokens_token UNIQUE (token);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT uc_refresh_tokens_userid UNIQUE (user_id);

ALTER TABLE roles
    ADD CONSTRAINT uc_roles_name UNIQUE (name);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_ROLE FOREIGN KEY (role_id) REFERENCES roles (id);

CREATE INDEX idx_tasks_company ON tasks(company_id);
CREATE INDEX idx_tasks_assigned_to ON tasks(assigned_to);
CREATE INDEX idx_membership_user ON company_memberships(user_id);
CREATE INDEX idx_membership_company ON company_memberships(company_id);
CREATE INDEX idx_tasks_created_by ON tasks(created_by);
CREATE INDEX idx_comments_task ON task_comments(task_id);
CREATE INDEX idx_history_task ON task_history(task_id);
CREATE INDEX idx_role_permissions_role ON role_permissions(role_id);

INSERT INTO permissions (name) VALUES
                                   ('CREATE_TASK'),
                                   ('UPDATE_TASK'),
                                   ('DELETE_TASK'),
                                   ('ASSIGN_TASK'),
                                   ('VIEW_TASK'),
                                   ('VIEW_ALL_TASKS'),
                                   ('MANAGE_COMPANY'),
                                   ('INVITE_USER');

INSERT INTO roles VALUES (1, 'ADMIN'),
                         (2, 'USER')
    ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;