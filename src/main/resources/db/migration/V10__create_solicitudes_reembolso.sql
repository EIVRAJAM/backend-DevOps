-- Migración para la tabla de solicitudes de reembolso

CREATE TABLE solicitudes_reembolso (
    id_solicitud_reembolso BIGSERIAL PRIMARY KEY,
    id_ticket BIGINT NOT NULL,
    id_usuario_solicitante BIGINT NOT NULL,
    id_usuario_responsable BIGINT,
    estado_solicitud VARCHAR(50) NOT NULL,
    motivo_solicitud TEXT NOT NULL,
    respuesta_organizador TEXT,
    monto_solicitado DECIMAL(10,2) NOT NULL,
    monto_aprobado DECIMAL(10,2),
    fecha_solicitud TIMESTAMP NOT NULL,
    fecha_revision TIMESTAMP,
    fecha_procesamiento TIMESTAMP,
    creado_en TIMESTAMP NOT NULL,
    actualizado_en TIMESTAMP,
    
    CONSTRAINT fk_solicitud_ticket FOREIGN KEY (id_ticket) REFERENCES tickets(id_ticket) ON DELETE RESTRICT,
    CONSTRAINT fk_solicitud_usuario_solicitante FOREIGN KEY (id_usuario_solicitante) REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    CONSTRAINT fk_solicitud_usuario_responsable FOREIGN KEY (id_usuario_responsable) REFERENCES usuarios(id_usuario) ON DELETE SET NULL,
    CONSTRAINT chk_monto_solicitado CHECK (monto_solicitado > 0)
);

CREATE INDEX idx_solicitud_ticket ON solicitudes_reembolso(id_ticket);
CREATE INDEX idx_solicitud_usuario_solicitante ON solicitudes_reembolso(id_usuario_solicitante);
CREATE INDEX idx_solicitud_estado ON solicitudes_reembolso(estado_solicitud);
