-- Crear tabla de roles
CREATE TABLE IF NOT EXISTS roles (
                                     role_id SERIAL PRIMARY KEY,
                                     role_name VARCHAR(50) NOT NULL UNIQUE
    );

-- Crear tabla de usuarios
CREATE TABLE IF NOT EXISTS users (
                                     user_id SERIAL PRIMARY KEY,
                                     user_name VARCHAR(100) NOT NULL,
    user_last_name VARCHAR(100) NOT NULL,
    user_username VARCHAR(50) NOT NULL UNIQUE,
    user_password VARCHAR(255) NOT NULL,
    user_phone VARCHAR(20),
    user_enabled BOOLEAN DEFAULT TRUE,
    user_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Crear tabla de roles de usuarios
CREATE TABLE IF NOT EXISTS users_roles (
                                           user_id INTEGER NOT NULL,
                                           role_id INTEGER NOT NULL,
                                           PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE
    );

-- Crear tabla de mesas del restaurante
CREATE TABLE IF NOT EXISTS restaurant_table (
                                                table_id SERIAL PRIMARY KEY,
                                                table_status VARCHAR(20) NOT NULL CHECK (table_status IN ('AVAILABLE', 'OCCUPIED', 'RESERVED'))
    );

-- Crear tabla de reservas
CREATE TABLE IF NOT EXISTS reservations (
                                            res_id SERIAL PRIMARY KEY,
                                            user_id INTEGER NOT NULL,
                                            table_id INTEGER NOT NULL,
                                            res_date TIMESTAMP NOT NULL,
                                            res_guests INTEGER NOT NULL CHECK (res_guests > 0),
    res_estado VARCHAR(20) NOT NULL CHECK (res_estado IN ('ACTIVE', 'CANCELLED', 'COMPLETED')),
    res_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (table_id) REFERENCES restaurant_table(table_id) ON DELETE RESTRICT
    );

-- Crear tabla de historial de reservas
CREATE TABLE IF NOT EXISTS historial_res (
                                             his_id SERIAL PRIMARY KEY,
                                             user_id INTEGER NOT NULL,
                                             res_id INTEGER NOT NULL,
                                             his_action VARCHAR(50) NOT NULL CHECK (his_action IN ('CREATE', 'UPDATE', 'DELETE')),
    his_old_values JSONB,
    his_new_values JSONB,
    his_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (res_id) REFERENCES reservations(res_id) ON DELETE CASCADE
    );

-- Crear función para el trigger de historial
CREATE OR REPLACE FUNCTION log_reservation_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO historial_res (user_id, res_id, his_action, his_old_values, his_new_values)
        VALUES (NEW.user_id, NEW.res_id, 'CREATE', NULL, row_to_json(NEW)::jsonb);
RETURN NEW;
ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO historial_res (user_id, res_id, his_action, his_old_values, his_new_values)
        VALUES (NEW.user_id, NEW.res_id, 'UPDATE', row_to_json(OLD)::jsonb, row_to_json(NEW)::jsonb);
RETURN NEW;
ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO historial_res (user_id, res_id, his_action, his_old_values, his_new_values)
        VALUES (OLD.user_id, OLD.res_id, 'DELETE', row_to_json(OLD)::jsonb, NULL);
RETURN OLD;
END IF;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Verificar y eliminar el trigger si ya existe
DROP TRIGGER IF EXISTS reservation_history_trigger ON reservations;

-- Crear trigger para historial de reservas
CREATE TRIGGER reservation_history_trigger
    AFTER INSERT OR UPDATE OR DELETE ON reservations
    FOR EACH ROW
    EXECUTE FUNCTION log_reservation_changes();

-- Crear índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_users_username ON users(user_username);
CREATE INDEX IF NOT EXISTS idx_reservations_date ON reservations(res_date);
CREATE INDEX IF NOT EXISTS idx_reservations_table ON reservations(table_id);
CREATE INDEX IF NOT EXISTS idx_historial_res_datetime ON historial_res(his_created_at);