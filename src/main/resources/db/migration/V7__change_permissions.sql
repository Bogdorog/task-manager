DELETE FROM permissions
WHERE name IN ('VIEW_TASK', 'CHANGE_ALL_TASKS', 'MANAGE_BOARD_COLUMNS');

INSERT INTO permissions (name) VALUES ('VIEW_ROLES'), ('MANAGE_MEMBERS'),
                                      ('MANAGE_BOARDS')
ON CONFLICT (name) DO NOTHING;