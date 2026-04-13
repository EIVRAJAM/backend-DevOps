package com.devops.backend.usuario.dto;

import java.time.LocalDate;

public record UsuarioDTO(
    String documento,
    String nombres,
    String apellidos,
    Short genero,
    LocalDate fechaNacimiento,
    String telefono,
    Long idRol
) {}