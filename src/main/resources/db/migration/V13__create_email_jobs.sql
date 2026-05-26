CREATE TABLE email_jobs (
    id_email_job BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(80) NOT NULL,
    destinatario VARCHAR(200) NOT NULL,
    copia VARCHAR(500),
    asunto VARCHAR(250) NOT NULL,
    template VARCHAR(150) NOT NULL,
    payload JSONB NOT NULL,
    estado VARCHAR(30) NOT NULL,
    intentos INT NOT NULL DEFAULT 0,
    max_intentos INT NOT NULL DEFAULT 3,
    proximo_intento_en TIMESTAMP NOT NULL,
    enviado_en TIMESTAMP,
    ultimo_error TEXT,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_jobs_pendientes ON email_jobs(estado, proximo_intento_en)
    WHERE estado = 'PENDIENTE';

CREATE INDEX idx_email_jobs_creado ON email_jobs(creado_en);

CREATE TABLE email_job_attachments (
    id_attachment BIGSERIAL PRIMARY KEY,
    id_email_job BIGINT NOT NULL,
    nombre_original VARCHAR(255) NOT NULL,
    nombre_almacenado VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    storage_path TEXT NOT NULL,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_attachment_email_job FOREIGN KEY (id_email_job)
        REFERENCES email_jobs(id_email_job) ON DELETE CASCADE
);

CREATE INDEX idx_attachment_email_job ON email_job_attachments(id_email_job);
