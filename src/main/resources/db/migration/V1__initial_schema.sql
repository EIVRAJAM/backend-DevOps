-- =========================================================
-- V1__initial_schema.sql
-- Migración inicial — Esquema completo del sistema
-- Flyway | Spring Boot 3.x | PostgreSQL 15+
-- =========================================================
-- Convenciones:
--   · Todos los identificadores en snake_case
--   · PKs con BIGSERIAL (equivalente a IDENTITY en estándar SQL)
--   · FKs con ON DELETE explícito en cada relación
--   · CHECKs nombrados para mensajes de error claros
--   · Índices justificados por consultas reales esperadas
--   · Sin DDL redundante (sin DROP IF EXISTS, Flyway gestiona versiones)
-- =========================================================


-- =========================================================
-- EXTENSIONES
-- =========================================================
-- uuid-ossp: genera UUID v4 en Postgres. Se usa en accesos.uuid_acceso
-- para identificar sesiones externas de forma opaca (sin exponer la PK).
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";


-- =========================================================
-- FUNCIÓN COMPARTIDA: auto-update de columna actualizado_en
-- =========================================================
-- Se reutiliza en todos los triggers de UPDATE. Centralizar la
-- lógica evita duplicar código y facilita cambios futuros.
CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.actualizado_en = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- =========================================================
-- FUNCIÓN ESPECÍFICA: auto-update condicional para ACCESOS
-- =========================================================
-- Solo actualiza actualizado_en cuando cambian campos sensibles
-- de seguridad (credenciales, estado). Evita que operaciones de
-- lectura/conteo disparen un timestamp innecesario.
CREATE OR REPLACE FUNCTION fn_set_updated_at_accesos()
RETURNS TRIGGER AS $$
BEGIN
    IF  NEW.username        IS DISTINCT FROM OLD.username
     OR NEW.correo_acceso   IS DISTINCT FROM OLD.correo_acceso
     OR NEW.password_hash   IS DISTINCT FROM OLD.password_hash
     OR NEW.estado_cuenta   IS DISTINCT FROM OLD.estado_cuenta
    THEN
        NEW.actualizado_en = CURRENT_TIMESTAMP;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- =========================================================
-- 1. ROLES
-- =========================================================
-- Tabla catálogo: pocos registros, cambios poco frecuentes.
-- nombre_rol UNIQUE garantiza que no existan roles duplicados.
CREATE TABLE roles (
    id_rol         BIGSERIAL    PRIMARY KEY,
    nombre_rol     VARCHAR(100)  NOT NULL,
    estado         VARCHAR(10)  NOT NULL DEFAULT 'ACTIVO'
        CONSTRAINT chk_roles_estado CHECK (estado IN ('ACTIVO','INACTIVO')),
    creado_en      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_roles_nombre UNIQUE (nombre_rol)
);

-- Índice explícito sobre nombre_rol para búsquedas por nombre
-- (Spring Security suele buscar roles por nombre, no por ID).
CREATE INDEX idx_roles_nombre ON roles (nombre_rol);

CREATE TRIGGER trg_roles_updated_at
BEFORE UPDATE ON roles
FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();


-- =========================================================
-- 2. USUARIOS
-- =========================================================
-- · documento_usuario: reducido a VARCHAR(20). Los documentos
--   reales (CC, pasaporte, NIT) no superan 15-20 caracteres.
--   VARCHAR(150) era exagerado y podría ocultar datos sucios.
-- · telefono_usuario: VARCHAR(20) cubre cualquier formato E.164.
-- · ON DELETE RESTRICT en FK a roles: no se puede eliminar un
--   rol que tenga usuarios asignados (integridad referencial fuerte).
CREATE TABLE usuarios (
    id_usuario               BIGSERIAL    PRIMARY KEY,
    id_rol                   BIGINT       NOT NULL,
    documento_usuario        VARCHAR(150)  NOT NULL,
    nombres_usuario          VARCHAR(150) NOT NULL,
    apellidos_usuario        VARCHAR(150) NOT NULL,
    -- 1=Masculino 2=Femenino 3=No binario 4=Otro 5=Prefiero no decir
    genero_usuario           INT2 CHECK (genero_usuario IN (1,2,3,4,5)),
    fecha_nacimiento_usuario DATE,
    telefono_usuario         VARCHAR(50),
    estado                   VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO'
        CONSTRAINT chk_usuarios_estado
            CHECK (estado IN ('ACTIVO','INACTIVO','BLOQUEADO')),
    creado_en                TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuarios_documento UNIQUE (documento_usuario),
    CONSTRAINT chk_usuarios_genero
        CHECK (genero_usuario IS NULL OR genero_usuario BETWEEN 1 AND 5),
    CONSTRAINT fk_usuarios_rol
        FOREIGN KEY (id_rol) REFERENCES roles (id_rol)
        ON DELETE RESTRICT   -- Nunca eliminar un rol con usuarios activos
        ON UPDATE CASCADE    -- Si cambia el id_rol (raro con BIGSERIAL), se propaga
);

CREATE INDEX idx_usuarios_rol      ON usuarios (id_rol);
CREATE INDEX idx_usuarios_estado   ON usuarios (estado);
-- Índice parcial: la mayoría de consultas operacionales solo
-- interesan usuarios ACTIVOS; este índice es más pequeño y rápido.
CREATE INDEX idx_usuarios_activos  ON usuarios (id_rol) WHERE estado = 'ACTIVO';

CREATE TRIGGER trg_usuarios_updated_at
BEFORE UPDATE ON usuarios
FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();


-- =========================================================
-- 3. ACCESOS
-- =========================================================
-- Relación 1:1 con usuarios (id_usuario es PK y FK a la vez).
-- Separar credenciales de datos personales es una práctica de
-- seguridad: permite auditar accesos sin exponer datos sensibles.
--
-- · ON DELETE CASCADE: si se elimina el usuario, sus credenciales
--   desaparecen automáticamente. Evita registros huérfanos.
-- · uuid_acceso: no se usa como PK para evitar índices de UUID en
--   hot-paths. Se usa como token público opaco (ej. en emails de
--   verificación), por eso se indexa por separado.
-- · intentos_fallidos: tope lógico gestionado por la aplicación;
--   el CHECK previene valores negativos por bugs.
CREATE TABLE accesos (
    id_usuario        BIGINT       PRIMARY KEY,
    username          VARCHAR(50)  NOT NULL,
    correo_acceso     VARCHAR(200) NOT NULL,
    password_hash      TEXT NOT NULL,
    intentos_fallidos   INT NOT NULL DEFAULT 0
        CONSTRAINT chk_accesos_intentos CHECK (intentos_fallidos >= 0),
    estado_cuenta     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO'
        CONSTRAINT chk_accesos_estado
            CHECK (estado_cuenta IN ('ACTIVO','INACTIVO','BLOQUEADO')),
    uuid_acceso       UUID         NOT NULL DEFAULT uuid_generate_v4(),
    ultimo_login      TIMESTAMP,
    creado_en         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_accesos_username      UNIQUE (username),
    CONSTRAINT uq_accesos_correo        UNIQUE (correo_acceso),
    CONSTRAINT uq_accesos_uuid          UNIQUE (uuid_acceso),
    CONSTRAINT fk_accesos_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario)
        ON DELETE CASCADE    -- Borrar usuario → borrar credenciales
        ON UPDATE CASCADE
);

-- Índice para búsquedas de login por username o correo (hot-path).
CREATE INDEX idx_accesos_username  ON accesos (username);
CREATE INDEX idx_accesos_correo    ON accesos (correo_acceso);
-- uuid_acceso se indexa para lookups de verificación/reset por email.
CREATE INDEX idx_accesos_uuid      ON accesos (uuid_acceso);

CREATE TRIGGER trg_accesos_updated_at
BEFORE UPDATE ON accesos
FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at_accesos();


-- =========================================================
-- 4. SESIONES
-- =========================================================
-- Almacena JWTs activos para soporte de revocación (token blacklist
-- o whitelist). token_jti es el JWT ID (claim "jti"), que identifica
-- de forma única cada token emitido.
--
-- · token_jti VARCHAR(255): los JWTs con jti UUID son 36 chars,
--   pero se da margen para implementaciones distintas.
-- · UNIQUE en token_jti: el sistema no debe emitir dos JWTs con
--   el mismo jti; este constraint lo hace imposible en BD.
-- · ON DELETE CASCADE: eliminar usuario revoca todas sus sesiones.
-- · fecha_fin NULL: sesión aún activa (no se ha cerrado/expirado).
CREATE TABLE sesiones (
    id_sesion      BIGSERIAL PRIMARY KEY,
    id_usuario     BIGINT NOT NULL,
    fecha_inicio   TIMESTAMP NOT NULL,
    fecha_fin      TIMESTAMP NULL,
    activa         BOOLEAN DEFAULT TRUE,
    token_jti      VARCHAR(255) NOT NULL,
    CONSTRAINT uq_sesiones_jti UNIQUE (token_jti),
    CONSTRAINT chk_sesiones_fechas
        CHECK (fecha_fin IS NULL OR fecha_fin > fecha_inicio),
    CONSTRAINT fk_sesiones_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Consultas frecuentes: sesiones activas de un usuario,
-- y búsqueda por jti para validación de JWT en cada request.
CREATE INDEX idx_sesiones_usuario  ON sesiones (id_usuario);
CREATE INDEX idx_sesiones_jti      ON sesiones (token_jti);
CREATE INDEX idx_sesiones_activas  ON sesiones (id_usuario) WHERE activa = TRUE;


-- =========================================================
-- 5. FUNCIONALIDADES
-- =========================================================
-- Árbol de menú / permisos con auto-referencia.
-- · ON DELETE RESTRICT en FK padre: no se puede eliminar un nodo
--   que tenga hijos. La app debe eliminar hijos primero.
-- · ON DELETE SET NULL en FK padre sería alternativa si se quieren
--   "huérfanos" al borrar el padre; RESTRICT es más seguro.
CREATE TABLE funcionalidades (
    id_funcionalidad       BIGSERIAL    PRIMARY KEY,
    id_padre_funcionalidad BIGINT,
    nombre_funcionalidad   VARCHAR(150) NOT NULL,
    url_funcionalidad      VARCHAR(250),
    estado                 VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO'
        CONSTRAINT chk_func_estado CHECK (estado IN ('ACTIVO','INACTIVO')),
    creado_en              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_funcionalidades_padre
        FOREIGN KEY (id_padre_funcionalidad) REFERENCES funcionalidades (id_funcionalidad)
        ON DELETE RESTRICT   -- No borrar padre con hijos existentes
        ON UPDATE CASCADE
);

CREATE INDEX idx_funcionalidades_padre ON funcionalidades (id_padre_funcionalidad);
CREATE INDEX idx_funcionalidades_url   ON funcionalidades (url_funcionalidad)
    WHERE url_funcionalidad IS NOT NULL;

CREATE TRIGGER trg_funcionalidades_updated_at
BEFORE UPDATE ON funcionalidades
FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();


-- =========================================================
-- 6. REL_ROL_FUNCIONALIDAD  (Permisos por rol)
-- =========================================================
-- Tabla de unión entre roles y funcionalidades.
-- · BIGSERIAL PK: facilita la integración con JPA/Hibernate
--   (evita EmbeddedId o IdClass) y permite referenciar filas
--   individualmente si fuera necesario en el futuro.
-- · UNIQUE (id_rol, id_funcionalidad): garantiza que no existan
--   permisos duplicados para la misma combinación.
-- · ON DELETE CASCADE en ambas FKs: si se elimina el rol o la
--   funcionalidad, el permiso asociado desaparece automáticamente.
CREATE TABLE rel_rol_funcionalidad (
    id_rol_funcionalidad BIGSERIAL PRIMARY KEY,
    id_rol               BIGINT    NOT NULL,
    id_funcionalidad     BIGINT    NOT NULL,
    CONSTRAINT uq_rrf_rol_func UNIQUE (id_rol, id_funcionalidad),
    CONSTRAINT fk_rrf_rol
        FOREIGN KEY (id_rol) REFERENCES roles (id_rol)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_rrf_funcionalidad
        FOREIGN KEY (id_funcionalidad) REFERENCES funcionalidades (id_funcionalidad)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- El índice sobre id_funcionalidad acelera la búsqueda inversa:
-- "¿qué roles tienen esta funcionalidad?"
CREATE INDEX idx_rrf_rol ON rel_rol_funcionalidad(id_rol);
CREATE INDEX idx_rrf_funcionalidad ON rel_rol_funcionalidad (id_funcionalidad);


-- =========================================================
-- 7. REL_USUARIO_FUNCIONALIDAD  (Permisos adicionales por usuario)
-- =========================================================
-- Permite excepciones/sobreescrituras al permiso de rol.
-- Mismo patrón: BIGSERIAL PK + UNIQUE compuesto.
CREATE TABLE rel_usuario_funcionalidad (
    id_usuario_funcionalidad BIGSERIAL PRIMARY KEY,
    id_usuario               BIGINT    NOT NULL,
    id_funcionalidad         BIGINT    NOT NULL,
    favorito                 BOOLEAN   NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_ruf_usuario_func UNIQUE (id_usuario, id_funcionalidad),
    CONSTRAINT fk_ruf_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ruf_funcionalidad
        FOREIGN KEY (id_funcionalidad) REFERENCES funcionalidades (id_funcionalidad)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX idx_ruf_funcionalidad ON rel_usuario_funcionalidad (id_funcionalidad);
-- Índice parcial para consultar solo favoritos de un usuario.
CREATE INDEX idx_ruf_favoritos     ON rel_usuario_funcionalidad (id_usuario)
    WHERE favorito = TRUE;


-- =========================================================
-- 8. VERIFICATION_CODES
-- =========================================================
-- Códigos de un solo uso para reset de contraseña y desbloqueo.
-- · ON DELETE CASCADE: eliminar usuario borra sus códigos pendientes.
-- · intentos: la app lo incrementa en cada intento fallido;
--   CHECK >= 0 previene corrupción por bugs.
-- · No hay actualizado_en: los códigos son inmutables tras creación.
--   Solo cambia "usado" y "intentos", lo cual está bien sin trigger.
CREATE TABLE verification_codes (
    id_codigo        BIGSERIAL   PRIMARY KEY,
    id_usuario       BIGINT      NOT NULL,
    codigo           VARCHAR(10) NOT NULL,
    tipo_codigo      VARCHAR(30) NOT NULL
        CONSTRAINT chk_vc_tipo
            CHECK (tipo_codigo IN ('RESET_PASSWORD','UNLOCK_ACCOUNT')),
    fecha_expiracion TIMESTAMP   NOT NULL,
    usado            BOOLEAN     NOT NULL DEFAULT FALSE,
    intentos         int    NOT NULL DEFAULT 0
        CONSTRAINT chk_vc_intentos CHECK (intentos >= 0),
    creado_en        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_vc_expiracion
        CHECK (fecha_expiracion > creado_en),
    CONSTRAINT fk_vc_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE INDEX idx_vc_usuario  ON verification_codes (id_usuario);
-- Índice compuesto para la consulta típica: buscar código vigente
-- de un usuario de un tipo específico.
CREATE INDEX idx_vc_lookup   ON verification_codes (id_usuario, tipo_codigo)
    WHERE usado = FALSE;


-- =========================================================
-- 9. EVENTOS
-- =========================================================
-- · ON DELETE RESTRICT en FK a usuarios: no se puede eliminar
--   un usuario que haya creado eventos. Protege la integridad
--   de la auditoría y del historial de eventos.
-- · cupos_parqueadero: el CHECK original era correcto pero
--   verboso. Se simplifica y se separa en dos constraints
--   nombradas para mensajes de error más claros.
-- · Se agrega columna imagen_url para posible uso futuro
--   (común en sistemas de eventos).
CREATE TABLE eventos (
    id_evento            BIGSERIAL    PRIMARY KEY,
    id_usuario_creador   BIGINT       NOT NULL,
    nombre_evento        VARCHAR(150) NOT NULL,
    descripcion_evento   TEXT,
    fecha_evento         DATE         NOT NULL,
    hora_evento          TIME         NOT NULL,
    lugar_evento         VARCHAR(200) NOT NULL,
    referencia_ubicacion VARCHAR(255),
    imagen_url           VARCHAR(500),
    estado_evento        VARCHAR(20)  NOT NULL DEFAULT 'BORRADOR'
        CONSTRAINT chk_eventos_estado_evento
            CHECK (estado_evento IN ('BORRADOR','PUBLICADO','CERRADO','CANCELADO')),
    capacidad_maxima     INTEGER      NOT NULL
        CONSTRAINT chk_eventos_capacidad CHECK (capacidad_maxima >= 0),
    tiene_parqueadero    BOOLEAN      NOT NULL DEFAULT FALSE,
    cupos_parqueadero    INTEGER      DEFAULT 0
        CONSTRAINT chk_eventos_cupos_parqueadero CHECK (cupos_parqueadero >= 0),
    -- Regla: si no hay parqueadero, cupos debe ser 0 o NULL
    CONSTRAINT chk_eventos_parqueadero_consistencia
        CHECK (
            (tiene_parqueadero = FALSE AND (cupos_parqueadero IS NULL OR cupos_parqueadero = 0))
            OR
            (tiene_parqueadero = TRUE  AND cupos_parqueadero >= 0)
        ),
    estado               VARCHAR(10)  NOT NULL DEFAULT 'ACTIVO'
        CONSTRAINT chk_eventos_estado CHECK (estado IN ('ACTIVO','INACTIVO')),
    creado_en            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_eventos_usuario_creador
        FOREIGN KEY (id_usuario_creador) REFERENCES usuarios (id_usuario)
        ON DELETE RESTRICT   -- No eliminar usuario con eventos registrados
        ON UPDATE CASCADE
);

CREATE INDEX idx_eventos_usuario       ON eventos (id_usuario_creador);
CREATE INDEX idx_eventos_fecha         ON eventos (fecha_evento);
CREATE INDEX idx_eventos_estado_evento ON eventos (estado_evento);
-- Índice parcial para eventos publicados (los más consultados públicamente).
CREATE INDEX idx_eventos_publicados    ON eventos (fecha_evento)
    WHERE estado_evento = 'PUBLICADO' AND estado = 'ACTIVO';

CREATE TRIGGER trg_eventos_updated_at
BEFORE UPDATE ON eventos
FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();


-- =========================================================
-- 10. HISTORIAL_EVENTOS
-- =========================================================
-- Auditoría de cambios de estado. Inmutable por diseño:
-- nunca se actualiza, solo se inserta.
-- · ON DELETE CASCADE en FK a eventos: si se borra el evento,
--   su historial desaparece también.
-- · ON DELETE SET NULL en FK a usuario responsable: si se
--   elimina el usuario que hizo el cambio, el historial se
--   conserva pero sin referencia al usuario (preserva auditoría).
-- · CHECK en estado_anterior: puede ser NULL (estado inicial)
--   o uno de los estados válidos.
CREATE TABLE historial_eventos (
    id_historial_evento    BIGSERIAL   PRIMARY KEY,
    id_evento              BIGINT      NOT NULL,
    estado_anterior        VARCHAR(10)
        CONSTRAINT chk_hist_estado_ant
            CHECK (estado_anterior IS NULL
                OR estado_anterior IN ('BORRADOR','PUBLICADO','CERRADO','CANCELADO')),
    estado_nuevo           VARCHAR(10) NOT NULL
        CONSTRAINT chk_hist_estado_nuevo
            CHECK (estado_nuevo IN ('BORRADOR','PUBLICADO','CERRADO','CANCELADO')),
    comentario             VARCHAR(500),
    id_usuario_responsable BIGINT,
    fecha_cambio           TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hist_evento
        FOREIGN KEY (id_evento) REFERENCES eventos (id_evento)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_hist_usuario
        FOREIGN KEY (id_usuario_responsable) REFERENCES usuarios (id_usuario)
        ON DELETE SET NULL   -- Preservar historial aunque el usuario sea eliminado
        ON UPDATE CASCADE
);

CREATE INDEX idx_hist_evento ON historial_eventos (id_evento);
CREATE INDEX idx_hist_fecha  ON historial_eventos (fecha_cambio);