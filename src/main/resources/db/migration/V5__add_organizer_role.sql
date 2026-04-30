-- =========================================================
-- V5__add_organizer_role.sql
-- Agrega el rol ORGANIZER al catálogo de roles del sistema
-- =========================================================
-- Contexto:
--   El sistema inicialmente solo contemplaba ROLE_USER y ROLE_ADMIN.
--   Se introduce ROLE_ORGANIZER para usuarios que crean y gestionan
--   eventos, diferenciándolos de simples asistentes (ROLE_USER) y
--   del administrador del sistema (ROLE_ADMIN).
-- =========================================================

INSERT INTO roles (nombre_rol, estado)
VALUES ('ROLE_ORGANIZER', 'ACTIVO')
ON CONFLICT (nombre_rol) DO NOTHING;