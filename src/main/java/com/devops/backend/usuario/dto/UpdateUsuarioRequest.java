package com.devops.backend.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Date;

public record UpdateUsuarioRequest(
        @NotBlank(message = "El documento no puede estar vacío")
        @Size(max = 20)
        String documento,

        @NotBlank(message = "Los nombres no pueden estar vacíos")
        String nombres,

        @NotBlank(message = "Los apellidos no pueden estar vacíos")
        String apellidos,

        @NotBlank(message = "El Telefono no puede estar vacío")
        String telefono,

        @NotBlank(message = "Genero no puede estar vacío")
        String genero,

        @NotNull(message = "Fecha de nacimiento no puede estar vacío")
        Date fechaNacimiento,

        @NotNull(message = "Los apellidos no puede estar vacío")
        Long idRol
) { }
