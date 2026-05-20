-- =========================================================
-- V10__allow_ticket_repurchase_after_inactive_status.sql
-- Permite nueva inscripcion cuando el ticket anterior ya no esta activo
-- =========================================================
-- Contexto:
--   La restriccion UNIQUE original sobre (id_usuario, id_evento)
--   bloqueaba cualquier nueva compra aunque el ticket anterior estuviera
--   CANCELADO o REEMBOLSADO.
--
--   La regla correcta es impedir duplicados solo mientras exista una
--   inscripcion activa: PENDIENTE, PAGADO o GRATIS.
-- =========================================================

ALTER TABLE tickets
    DROP CONSTRAINT IF EXISTS uq_tickets_usuario_evento;

CREATE UNIQUE INDEX uq_tickets_usuario_evento_activo
    ON tickets (id_usuario, id_evento)
    WHERE estado_ticket IN ('PENDIENTE', 'PAGADO', 'GRATIS');
