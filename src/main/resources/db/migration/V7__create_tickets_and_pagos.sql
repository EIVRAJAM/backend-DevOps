-- =========================================================
-- V7__create_tickets_and_pagos.sql
-- Crea las tablas tickets y pagos para el flujo de compra
-- =========================================================
-- Contexto:
--   Se introducen dos tablas nuevas para gestionar el ciclo
--   completo de una compra en Stripe:
--
--   · tickets: representa la inscripción de un usuario a un evento.
--     Existe tanto para eventos gratuitos (estado GRATIS) como de
--     pago (estados PENDIENTE → PAGADO / CANCELADO / REEMBOLSADO).
--     El UNIQUE (id_usuario, id_evento) impide doble registro por
--     race condition o doble clic, delegando la restricción a la BD.
--
--   · pagos: registro inmutable de cada transacción procesada por
--     Stripe. Incluye stripe_raw_event (JSON del webhook) para
--     facilitar debugging y auditoría sin depender de Stripe Dashboard.
--     Un ticket puede tener múltiples filas en pagos (cobro + reembolso).
--
--   Ambas tablas usan ON DELETE RESTRICT en sus FKs para preservar
--   el historial financiero incluso si el evento o usuario se elimina.
-- =========================================================


-- ---------------------------------------------------------
-- TICKETS
-- ---------------------------------------------------------
CREATE TABLE tickets (
    id_ticket                BIGSERIAL     PRIMARY KEY,
    id_evento                BIGINT        NOT NULL,
    id_usuario               BIGINT        NOT NULL,
    stripe_payment_intent_id VARCHAR(100),
    estado_ticket            VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE'
        CONSTRAINT chk_tickets_estado
            CHECK (estado_ticket IN ('PENDIENTE','PAGADO','CANCELADO','REEMBOLSADO','GRATIS')),
    monto_pagado             NUMERIC(10,2),
    moneda                   VARCHAR(3),
    codigo_qr                VARCHAR(255),
    fecha_compra             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_en                TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Previene doble registro del mismo usuario en el mismo evento
    CONSTRAINT uq_tickets_usuario_evento
        UNIQUE (id_usuario, id_evento),
    CONSTRAINT fk_tickets_evento
        FOREIGN KEY (id_evento) REFERENCES eventos (id_evento)
        ON DELETE RESTRICT,
    CONSTRAINT fk_tickets_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario)
        ON DELETE RESTRICT
);

CREATE INDEX idx_tickets_evento  ON tickets (id_evento);
CREATE INDEX idx_tickets_usuario ON tickets (id_usuario);
CREATE INDEX idx_tickets_estado  ON tickets (estado_ticket);
-- Índice parcial: solo tickets con intent asociado son buscados
-- durante la validación de webhooks de Stripe
CREATE INDEX idx_tickets_intent  ON tickets (stripe_payment_intent_id)
    WHERE stripe_payment_intent_id IS NOT NULL;

CREATE TRIGGER trg_tickets_updated_at
BEFORE UPDATE ON tickets
FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();


-- ---------------------------------------------------------
-- PAGOS
-- ---------------------------------------------------------
CREATE TABLE pagos (
    id_pago          BIGSERIAL     PRIMARY KEY,
    id_ticket        BIGINT        NOT NULL,
    stripe_charge_id VARCHAR(100),
    stripe_refund_id VARCHAR(100),
    monto            NUMERIC(10,2) NOT NULL
        CONSTRAINT chk_pagos_monto CHECK (monto > 0),
    moneda           VARCHAR(3)    NOT NULL,
    tipo_pago        VARCHAR(20)   NOT NULL
        CONSTRAINT chk_pagos_tipo
            CHECK (tipo_pago IN ('COBRO','REEMBOLSO')),
    estado_pago      VARCHAR(20)   NOT NULL
        CONSTRAINT chk_pagos_estado
            CHECK (estado_pago IN ('EXITOSO','FALLIDO','PENDIENTE','REEMBOLSADO')),
    -- JSON crudo del evento Stripe; invaluable para auditoría y debugging
    stripe_raw_event TEXT,
    creado_en        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pagos_ticket
        FOREIGN KEY (id_ticket) REFERENCES tickets (id_ticket)
        ON DELETE RESTRICT
);

CREATE INDEX idx_pagos_ticket ON pagos (id_ticket);
CREATE INDEX idx_pagos_charge ON pagos (stripe_charge_id)
    WHERE stripe_charge_id IS NOT NULL;