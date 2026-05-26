-- =========================================================
-- V3__allow_nullable_password.sql
-- Permite password_hash NULL para usuarios OAuth
-- =========================================================

ALTER TABLE accesos
ALTER COLUMN password_hash DROP NOT NULL;

-- Documentación:
-- - Usuarios OAuth no tienen contraseña local, por lo que password_hash será NULL
-- - Usuarios con login tradicional continuarán con password_hash NOT NULL
-- - La validación de login comprobará: si password_hash IS NULL → solo OAuth permitido