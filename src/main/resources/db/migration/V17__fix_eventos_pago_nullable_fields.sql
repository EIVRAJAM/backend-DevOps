-- =========================================================
-- V17__fix_eventos_pago_nullable_fields.sql
-- Permite NULL en moneda y refuerza consistencia es_de_pago
-- =========================================================
-- Contexto:
--   El backend ya normaliza los campos dependientes cuando
--   es_de_pago cambia (mapper limpia precio y moneda a NULL).
--   La BD debe alinearse permitiendo NULL en moneda y agregando
--   una constraint bidireccional que cubra ambos casos.
-- =========================================================

-- 1. Eliminar constraints viejas que no cubren el caso es_de_pago = FALSE
ALTER TABLE eventos DROP CONSTRAINT IF EXISTS chk_eventos_pago_precio;
ALTER TABLE eventos DROP CONSTRAINT IF EXISTS chk_eventos_precio;

-- 2. Permitir NULL en moneda (eventos gratuitos no tienen moneda)
ALTER TABLE eventos ALTER COLUMN moneda DROP NOT NULL;

-- 3. Nueva constraint bidireccional: cubre ambos casos
--    es_de_pago = TRUE  -> precio > 0 y moneda NOT NULL
--    es_de_pago = FALSE -> precio y moneda deben ser NULL
ALTER TABLE eventos ADD CONSTRAINT chk_eventos_pago_consistencia
    CHECK (
        (
            es_de_pago = TRUE
            AND precio IS NOT NULL
            AND precio > 0
            AND moneda IS NOT NULL
        )
        OR
        (
            es_de_pago = FALSE
            AND precio IS NULL
            AND moneda IS NULL
        )
    );
