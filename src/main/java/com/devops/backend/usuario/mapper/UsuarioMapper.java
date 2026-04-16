package com.devops.backend.usuario.mapper;

import com.devops.backend.auth.dto.SignUpRequest;
import com.devops.backend.rol.entity.Rol;
import com.devops.backend.usuario.dto.SignUpResponseUsuario;
import com.devops.backend.usuario.dto.UsuarioDTO;
import com.devops.backend.usuario.entity.Usuario;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class UsuarioMapper {
    private static final short GENERO_MASCULINO_CODE = 1;
    private static final short GENERO_FEMENINO_CODE = 2;

    public UsuarioDTO toDTO(SignUpRequest request, Long idRol) {
        Short genero = request.genero().equalsIgnoreCase("masculino")
                ? (short) GENERO_MASCULINO_CODE : (short) GENERO_FEMENINO_CODE;

        LocalDate fechaNacimiento = null;
        if (request.fechaNacimiento() != null) {
            fechaNacimiento = new java.sql.Date(
                    request.fechaNacimiento().getTime()).toLocalDate();
        }

        return new UsuarioDTO(
                request.documento(),
                request.nombres(),
                request.apellidos(),
                genero,
                fechaNacimiento,
                request.telefono(),
                idRol
        );
    }

    public SignUpResponseUsuario toResponse(Usuario usuario) {
        return new SignUpResponseUsuario(
                usuario.getIdUsuario(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getTelefono()
        );
    }

    public Usuario toUsuario(UsuarioDTO dto, Rol validRol){
        Usuario usuario = new Usuario();
        usuario.setDocumento(dto.documento());
        usuario.setNombres(dto.nombres());
        usuario.setApellidos(dto.apellidos());
        usuario.setGenero(dto.genero());
        usuario.setFechaNacimiento(dto.fechaNacimiento());
        usuario.setTelefono(dto.telefono());
        usuario.setRol(validRol);
        usuario.setEstado("ACTIVO");
        return usuario;
    }
}