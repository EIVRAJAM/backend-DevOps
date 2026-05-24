-- =========================================================
-- V16__add_email_job_type_constraint.sql
-- Agrega constraint CHECK sobre email_jobs.tipo para
-- controlar los tipos validos de jobs de correo.
-- =========================================================
-- Contexto:
--   La columna tipo de email_jobs no tenia constraint formal,
--   permitiendo valores arbitrarios. Esta migracion fija el
--   conjunto de tipos validos e incluye los nuevos tipos del
--   modulo de notificaciones y check-in con QR.
-- =========================================================

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'email_jobs'
          AND constraint_name = 'chk_email_jobs_tipo'
    ) THEN
        ALTER TABLE email_jobs DROP CONSTRAINT chk_email_jobs_tipo;
    END IF;
END $$;

ALTER TABLE email_jobs
    ADD CONSTRAINT chk_email_jobs_tipo
        CHECK (tipo IN (
            'REEMBOLSO_SOLICITUD_USUARIO',
            'REEMBOLSO_SOLICITUD_ORGANIZADOR',
            'REEMBOLSO_APROBADA',
            'REEMBOLSO_RECHAZADA',
            'REEMBOLSO_REEMBOLSADA',
            'INSCRIPCION',
            'CHECKIN',
            'RECORDATORIO',
            'CAMBIO_EVENTO'
        ));
