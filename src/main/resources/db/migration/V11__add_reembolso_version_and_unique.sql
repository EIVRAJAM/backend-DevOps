-- V11: Versionado optimista y constraint anti-duplicados para solicitudes de reembolso

ALTER TABLE solicitudes_reembolso
    ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

CREATE UNIQUE INDEX IF NOT EXISTS idx_one_active_solicitud_per_ticket
    ON solicitudes_reembolso (id_ticket)
    WHERE estado_solicitud IN ('SOLICITADA', 'EN_REVISION', 'APROBADA');
