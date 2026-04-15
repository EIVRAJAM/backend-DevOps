package com.devops.backend.acceso.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccesoAdminDTO(
        Long idUsuario,
        String username,
        String correoAcceso,
        Integer intentosFallidos,
        String estadoCuenta,
        UUID uuidAcceso,
        LocalDateTime ultimoLogin,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn
) {}