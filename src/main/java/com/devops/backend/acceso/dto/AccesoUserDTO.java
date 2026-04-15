package com.devops.backend.acceso.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record AccesoUserDTO(

        @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres") String username,

        @Email(message = "El correo debe ser válido") String correoAcceso) {
}