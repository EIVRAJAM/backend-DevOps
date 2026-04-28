-- =========================================================
-- V4__add_session_authentication_type.sql
-- Agrega tipo_login a sesiones para auditoría por método de autenticación
-- =========================================================

-- Agregar columna tipo_login a la tabla sesiones
ALTER TABLE sesiones
ADD COLUMN tipo_login VARCHAR(20) NOT NULL DEFAULT 'LOCAL';

-- Constraint: validar valores permitidos
ALTER TABLE sesiones
ADD CONSTRAINT chk_sesiones_tipo_login 
    CHECK (tipo_login IN ('LOCAL', 'GOOGLE', 'MICROSOFT'));

-- Índice para consultas rápidas por tipo de login
CREATE INDEX idx_sesiones_tipo_login 
    ON sesiones (tipo_login, id_usuario, fecha_inicio DESC);

-- Documentación:
-- - tipo_login registra el método de autenticación de cada sesión específica
-- - Valores: LOCAL (email+password) | GOOGLE (OAuth Google) | MICROSOFT (futuro)
-- - DEFAULT 'LOCAL' para compatibilidad con sesiones existentes
-- - Permite auditoría y análisis de métodos de autenticación usado por usuario