-- =========================================================
-- V9__create_evento_staff_and_checkin.sql
-- Crea tabla para delegación de staff por evento y columnas
-- necesarias para el proceso de check-in de tickets.
-- =========================================================

-- ---------------------------------------------------------
-- 1. MODIFICAR TABLA tickets (CHECK-IN)
-- ---------------------------------------------------------
ALTER TABLE tickets
ADD COLUMN checkin_realizado BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN fecha_checkin TIMESTAMP,
ADD COLUMN id_usuario_checkin BIGINT;

-- Constraint para asegurar consistencia del check-in
ALTER TABLE tickets
ADD CONSTRAINT chk_tickets_checkin_consistency
CHECK (
    (checkin_realizado = FALSE AND fecha_checkin IS NULL AND id_usuario_checkin IS NULL)
    OR
    (checkin_realizado = TRUE AND fecha_checkin IS NOT NULL AND id_usuario_checkin IS NOT NULL)
);

ALTER TABLE tickets
ADD CONSTRAINT fk_tickets_usuario_checkin
FOREIGN KEY (id_usuario_checkin) REFERENCES usuarios (id_usuario)
ON DELETE RESTRICT;

-- Índices para búsqueda de QR y métricas de check-in
CREATE INDEX idx_tickets_codigo_qr ON tickets (codigo_qr);
CREATE INDEX idx_tickets_evento_checkin ON tickets (id_evento, checkin_realizado);

-- ---------------------------------------------------------
-- 2. NUEVA TABLA evento_staff (ASIGNACIÓN)
-- ---------------------------------------------------------
CREATE TABLE evento_staff (
    id_evento_staff BIGSERIAL PRIMARY KEY,
    id_evento       BIGINT    NOT NULL,
    id_usuario      BIGINT    NOT NULL,
    asignado_por    BIGINT    NOT NULL,
    estado          VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
        CONSTRAINT chk_evento_staff_estado
            CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    creado_en       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_evento_staff_usuario UNIQUE (id_evento, id_usuario),
    
    CONSTRAINT fk_evento_staff_evento
        FOREIGN KEY (id_evento) REFERENCES eventos (id_evento)
        ON DELETE RESTRICT,
        
    CONSTRAINT fk_evento_staff_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario)
        ON DELETE RESTRICT,
        
    CONSTRAINT fk_evento_staff_asignador
        FOREIGN KEY (asignado_por) REFERENCES usuarios (id_usuario)
        ON DELETE RESTRICT
);

CREATE INDEX idx_evento_staff_evento ON evento_staff (id_evento);
CREATE INDEX idx_evento_staff_usuario ON evento_staff (id_usuario);
CREATE INDEX idx_evento_staff_evento_estado ON evento_staff (id_evento, estado);
CREATE INDEX idx_evento_staff_usuario_estado ON evento_staff (id_usuario, estado);

CREATE TRIGGER trg_evento_staff_updated_at
BEFORE UPDATE ON evento_staff
FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
