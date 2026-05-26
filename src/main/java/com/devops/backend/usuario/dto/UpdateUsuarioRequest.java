package com.devops.backend.usuario.dto;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Schema(description = "Datos para actualizar informacion del perfil propio. Todos los campos son opcionales: solo se actualizan los campos enviados.")
public record UpdateUsuarioRequest(

        @Size(max = 150, message = "El documento no puede exceder los 150 caracteres")
        @Schema(description = "Numero de documento de identificacion (opcional)", example = "1234567890")
        String documento,

        @Size(max = 150, message = "Los nombres no pueden exceder los 150 caracteres")
        @Schema(description = "Nombres del usuario (opcional)", example = "Juan Carlos")
        String nombres,

        @Size(max = 150, message = "Los apellidos no pueden exceder los 150 caracteres")
        @Schema(description = "Apellidos del usuario (opcional)", example = "Perez Garcia")
        String apellidos,

        @Size(max = 50, message = "El telefono no puede exceder los 50 caracteres")
        @Schema(description = "Numero de telefono de contacto (opcional)", example = "+57 3001234567")
        String telefono,

        @Pattern(regexp = "masculino|femenino", message = "El genero debe ser masculino o femenino")
        @Schema(description = "Genero del usuario (opcional)", example = "masculino")
        String genero,

        @Past(message = "La fecha de nacimiento no puede ser futura")
        @Schema(description = "Fecha de nacimiento (opcional, no puede ser futura)", example = "1990-05-15")
        Date fechaNacimiento

) {
}
