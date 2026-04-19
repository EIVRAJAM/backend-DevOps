-- =========================================================
-- V2__seed_roles.sql
-- Datos semilla — Roles del sistema por defecto
-- =========================================================
-- ¿Por qué una migración separada de V1?
--
-- 1. Separación de responsabilidades:
--    V1 define estructura (DDL), V2 carga datos (DML).
--    Esto sigue el principio de Single Responsibility en migraciones.
--
-- 2. Repetibilidad y entornos:
--    En algunos proyectos, el seed varía por entorno (dev vs prod).
--    Separar DDL de DML permite aplicar V1 en todos los entornos
--    y decidir si aplicar V2 según contexto.
--
-- 3. Historial limpio:
--    Si en el futuro se necesita agregar un nuevo rol, se crea V3__add_role_x.sql
--    sin tocar V1 ni V2. Flyway garantiza que solo se ejecuta lo nuevo.
--
-- 4. Mantenibilidad:
--    Es más fácil revisar en auditoría "¿cuándo se insertaron los roles
--    base?" buscando el archivo de seed, no mezclado con 800 líneas de DDL.
-- =========================================================

INSERT INTO roles (nombre_rol, estado)
VALUES
    ('ROLE_USER',  'ACTIVO'),
    ('ROLE_ADMIN', 'ACTIVO')
ON CONFLICT (nombre_rol) DO NOTHING;
-- ON CONFLICT DO NOTHING: hace la migración idempotente.
-- Si por algún motivo se ejecuta en una BD que ya tiene los roles, no falla.