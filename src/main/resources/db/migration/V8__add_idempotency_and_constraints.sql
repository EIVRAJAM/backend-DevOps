-- =========================================================
-- V8__add_idempotency_and_constraints.sql
-- Refuerza idempotencia en pagos y webhooks (Stripe)
-- =========================================================

-- ---------------------------------------------------------
-- TICKETS: asegurar 1 PaymentIntent por ticket(Corrección de diseño)
-- ---------------------------------------------------------
CREATE UNIQUE INDEX uq_tickets_payment_intent
ON tickets (stripe_payment_intent_id)
WHERE stripe_payment_intent_id IS NOT NULL;


-- ---------------------------------------------------------
-- PAGOS: idempotencia fuerte sobre transacciones Stripe
-- ---------------------------------------------------------

-- Evita duplicar cargos (charge)
CREATE UNIQUE INDEX uq_pagos_charge
ON pagos (stripe_charge_id)
WHERE stripe_charge_id IS NOT NULL;

-- Evita duplicar reembolsos (refund)
CREATE UNIQUE INDEX uq_pagos_refund
ON pagos (stripe_refund_id)
WHERE stripe_refund_id IS NOT NULL;


-- ---------------------------------------------------------
-- WEBHOOKS: evitar reprocesamiento de eventos Stripe
-- ---------------------------------------------------------

-- 1. Agregar columna para identificar evento único de Stripe
ALTER TABLE pagos
ADD COLUMN stripe_event_id VARCHAR(100);

-- 2. Índice único → clave de idempotencia del webhook
CREATE UNIQUE INDEX uq_pagos_event
ON pagos (stripe_event_id)
WHERE stripe_event_id IS NOT NULL;


-- ---------------------------------------------------------
-- Idempotencia propia del backend
-- ---------------------------------------------------------

-- Controla reintentos desde la API (ej: clientes con mala conexión)
ALTER TABLE pagos
ADD COLUMN idempotency_key VARCHAR(100);

CREATE UNIQUE INDEX uq_pagos_idempotency
ON pagos (idempotency_key)
WHERE idempotency_key IS NOT NULL;