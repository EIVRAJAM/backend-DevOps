-- =========================================================
-- V6__add_payment_columns_to_eventos.sql
-- Extiende la tabla eventos para soportar eventos de pago
-- =========================================================
-- Contexto:
--   Originalmente todos los eventos eran gratuitos. Se agregan
--   columnas para indicar si un evento es de pago, su precio,
--   moneda y cupos disponibles en tiempo real.
--
--   Decisiones de diseño:
--   · precio NULL = evento gratuito (compatibilidad con datos existentes).
--   · capacidad_disponible se decrementa atómicamente en cada compra
--     para evitar race conditions (más eficiente que COUNT de tickets).
--   · El CHECK chk_eventos_pago_precio garantiza consistencia entre
--     es_de_pago y precio a nivel de BD, como última línea de defensa.
-- =========================================================

ALTER TABLE eventos
    ADD COLUMN es_de_pago           BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN precio               NUMERIC(10,2)
        CONSTRAINT chk_eventos_precio
            CHECK (precio IS NULL OR precio >= 0),
    ADD COLUMN moneda               VARCHAR(3)   NOT NULL DEFAULT 'USD'
    CONSTRAINT chk_monedas_iso
            CHECK (moneda IN ('USD', 'COP', 'EUR', 'MXN')),,
    ADD COLUMN capacidad_disponible INTEGER
        CONSTRAINT chk_eventos_cap_disp
            CHECK (capacidad_disponible >= 0);

-- Consistencia: evento de pago debe tener precio definido y positivo
ALTER TABLE eventos
    ADD CONSTRAINT chk_eventos_pago_precio
        CHECK (
            (es_de_pago = FALSE)
            OR
            (es_de_pago = TRUE AND precio IS NOT NULL AND precio > 0)
        );

-- Índice parcial: solo eventos de pago publicados son consultados
-- frecuentemente en el contexto de checkout y listados públicos
CREATE INDEX idx_eventos_pago
    ON eventos (es_de_pago)
    WHERE es_de_pago = TRUE AND estado_evento = 'PUBLICADO';