package com.devops.backend.auth.dto;

import jakarta.validation.constraints.*;
import java.util.Date;

public record SignUpRequest(
                @NotBlank String documento,
                @NotBlank String nombres,
                @NotBlank String apellidos,
                @Pattern(regexp = "masculino|femenino", message = "El género debe ser masculino o femenino") String genero,
                @Past Date fechaNacimiento,
                @NotBlank String telefono,
                @NotBlank String username,
                @Email String correoAcceso,
                @NotBlank @Size(min = 8, message = "La clave debe tener al menos 8 caracteres") String claveAcceso) {
}
