-- =========================================================
-- V15__add_ticket_pending_checkout_expiration.sql
-- Agrega expiracion para checkouts pendientes
-- =========================================================
-- Contexto:
--   Un ticket PENDIENTE bloquea una nueva compra mientras el usuario
--   termina el checkout. Sin expiracion, ese bloqueo puede quedar vivo
--   indefinidamente si el usuario abandona el pago.
--
--   La regla nueva es que los tickets PENDIENTE expiran y pasan a
--   EXPIRADO. Los estados EXPIRADO, CANCELADO y REEMBOLSADO permiten
--   crear un nuevo intento de compra.
-- =========================================================

ALTER TABLE tickets
    ADD COLUMN expira_en TIMESTAMP,
    ADD COLUMN stripe_client_secret VARCHAR(255);

ALTER TABLE tickets
    DROP CONSTRAINT IF EXISTS chk_tickets_estado;

ALTER TABLE tickets
    ADD CONSTRAINT chk_tickets_estado
        CHECK (estado_ticket IN ('PENDIENTE','PAGADO','CANCELADO','REEMBOLSADO','GRATIS','EXPIRADO'));

UPDATE tickets
SET expira_en = creado_en + INTERVAL '15 minutes'
WHERE estado_ticket = 'PENDIENTE'
  AND expira_en IS NULL;

UPDATE tickets
SET estado_ticket = 'EXPIRADO'
WHERE estado_ticket = 'PENDIENTE'
  AND expira_en <= CURRENT_TIMESTAMP;

ALTER TABLE tickets
    ADD CONSTRAINT chk_tickets_pendiente_expira
        CHECK (estado_ticket <> 'PENDIENTE' OR expira_en IS NOT NULL);

CREATE INDEX idx_tickets_pendientes_expira
    ON tickets (expira_en)
    WHERE estado_ticket = 'PENDIENTE';
