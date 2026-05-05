package com.devops.backend.usuario.dto;

import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;


public record UsuarioDTO(
    @Schema(description = "Número de identificación del usuario", example = "1234567890")
    String documento,
    @Schema(description = "Nombres del usuario", example = "Juan")
    String nombres,
    @Schema(description = "Apellidos del usuario", example = "Pérez")
    String apellidos,
    @Schema(description = "Género del usuario (1: Masculino, 2: Femenino)", example = "1")
    Short genero,
    @Schema(description = "Fecha de nacimiento del usuario", example = "1990-01-15")
    LocalDate fechaNacimiento,
    @Schema(description = "Número de teléfono del usuario", example = "+34612345678")
    String telefono,
    @Schema(description = "Identificador del rol asignado al usuario", example = "1")
    Long idRol
) {}