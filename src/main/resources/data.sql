-- Insertar roles básicos si no existen
INSERT INTO roles (role_name)
VALUES ('ROLE_ADMIN')
    ON CONFLICT (role_name) DO NOTHING;

INSERT INTO roles (role_name)
VALUES ('ROLE_USER')
    ON CONFLICT (role_name) DO NOTHING;

INSERT INTO roles (role_name)
VALUES ('ROLE_STAFF')
    ON CONFLICT (role_name) DO NOTHING;

-- Insertar un usuario admin con contraseña encriptada (la contraseña es 'admin')
-- En un entorno real, nunca se guardarían contraseñas en texto plano en data.sql
-- Este es sólo para fines de desarrollo
INSERT INTO users (user_name, user_last_name, user_username, user_password, user_phone, user_enabled)
SELECT 'Admin', 'System', 'admin', '$2a$10$3lFPXyIIf3vCm2zKvXkWWOPjNLZj3cQKZkZJSXSCL6XeQ5wf.3P1S', '1234567890', TRUE
    WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE user_username = 'admin'
);

-- Asignar rol de admin al usuario admin
INSERT INTO users_roles (user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u, roles r
WHERE u.user_username = 'admin' AND r.role_name = 'ROLE_ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM users_roles ur
                      JOIN users u2 ON ur.user_id = u2.user_id
                      JOIN roles r2 ON ur.role_id = r2.role_id
    WHERE u2.user_username = 'admin' AND r2.role_name = 'ROLE_ADMIN'
);

-- Insertar usuario normal (la contraseña es 'user')
INSERT INTO users (user_name, user_last_name, user_username, user_password, user_phone, user_enabled)
SELECT 'Usuario', 'Normal', 'user', '$2a$10$xLiQwBQQtfJoxqDu.WfuSeNYP/oI0AgZMhvjtLq7Gqly6u8WC4sMO', '0987654321', TRUE
    WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE user_username = 'user'
);

-- Asignar rol de usuario al usuario normal
INSERT INTO users_roles (user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u, roles r
WHERE u.user_username = 'user' AND r.role_name = 'ROLE_USER'
  AND NOT EXISTS (
    SELECT 1 FROM users_roles ur
                      JOIN users u2 ON ur.user_id = u2.user_id
                      JOIN roles r2 ON ur.role_id = r2.role_id
    WHERE u2.user_username = 'user' AND r2.role_name = 'ROLE_USER'
);

-- Insertar usuario staff (la contraseña es 'staff')
INSERT INTO users (user_name, user_last_name, user_username, user_password, user_phone, user_enabled)
SELECT 'Staff', 'Restaurante', 'staff', '$2a$10$xLiQwBQQtfJoxqDu.WfuSeNYP/oI0AgZMhvjtLq7Gqly6u8WC4sMO', '1122334455', TRUE
    WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE user_username = 'staff'
);

-- Asignar rol de staff al usuario staff
INSERT INTO users_roles (user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u, roles r
WHERE u.user_username = 'staff' AND r.role_name = 'ROLE_STAFF'
  AND NOT EXISTS (
    SELECT 1 FROM users_roles ur
                      JOIN users u2 ON ur.user_id = u2.user_id
                      JOIN roles r2 ON ur.role_id = r2.role_id
    WHERE u2.user_username = 'staff' AND r2.role_name = 'ROLE_STAFF'
);

-- Insertar 10 mesas de restaurante
DO $$
BEGIN
FOR i IN 1..10 LOOP
        INSERT INTO restaurant_table (table_status)
SELECT 'AVAILABLE'
    WHERE NOT EXISTS (
            SELECT 1 FROM restaurant_table WHERE table_id = i
        );
END LOOP;
END$$;

-- username: jperez
--password: password123
--role: user

-- username: david
--password: david123
--role: user

-- username: anav
--password: ana123
--role: admin

-- username: bero
--password: bero123
--role: staff

select * from roles;
select * from users;
select * from users_roles;

