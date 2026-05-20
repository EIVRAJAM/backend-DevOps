CREATE TABLE datos_reembolso_solicitud (
    id_datos_reembolso BIGSERIAL PRIMARY KEY,
    id_solicitud_reembolso BIGINT NOT NULL,
    medio_reembolso VARCHAR(30) NOT NULL,
    titular_cuenta VARCHAR(150) NOT NULL,
    documento_titular VARCHAR(20) NOT NULL,
    entidad_financiera VARCHAR(100),
    tipo_cuenta VARCHAR(20),
    numero_cuenta_enmascarado VARCHAR(30),
    correo_contacto VARCHAR(150) NOT NULL,
    telefono_contacto VARCHAR(30) NOT NULL,
    observaciones TEXT,
    certificado_enviado BOOLEAN NOT NULL DEFAULT FALSE,
    documento_adicional_enviado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_envio_datos TIMESTAMP NOT NULL,
    creado_en TIMESTAMP NOT NULL,
    actualizado_en TIMESTAMP,

    CONSTRAINT fk_datos_solicitud FOREIGN KEY (id_solicitud_reembolso)
        REFERENCES solicitudes_reembolso(id_solicitud_reembolso) ON DELETE CASCADE,
    CONSTRAINT uq_datos_solicitud UNIQUE (id_solicitud_reembolso),
    CONSTRAINT chk_medio_reembolso CHECK (medio_reembolso IN ('CUENTA_BANCARIA', 'NEQUI', 'DAVIPLATA', 'OTRO')),
    CONSTRAINT chk_tipo_cuenta CHECK (tipo_cuenta IS NULL OR tipo_cuenta IN ('AHORROS', 'CORRIENTE'))
);

CREATE INDEX idx_datos_solicitud ON datos_reembolso_solicitud(id_solicitud_reembolso);
