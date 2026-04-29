package com.devops.backend.usuario.mapper;

import com.devops.backend.auth.dto.SignUpRequest;
import com.devops.backend.rol.entity.Rol;
import com.devops.backend.usuario.dto.*;
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

    public UserListResponse toListResponse(Usuario u){
        return new UserListResponse(u.getIdUsuario(),u.getNombres(),
                                    u.getApellidos(), u.getDocumento(),
                                    u.getRol().getNombreRol());
    }

    public UserUpdateAdminResponse toUpdateAdminResponse(Usuario u) {
        return new UserUpdateAdminResponse(
                u.getIdUsuario(),
                u.getDocumento(),
                u.getNombres(),
                u.getApellidos(),
                u.getGenero(),
                u.getFechaNacimiento(),
                u.getTelefono(),
                u.getEstado(),
                u.getRol().getNombreRol(),
                u.getCreadoEn(),
                u.getActualizadoEn()
        );
    }

    public void applyUpdate(Usuario usuario, UpdateUsuarioRequest dto, Rol rol) {
        Short genero = dto.genero().equalsIgnoreCase("masculino")
                ? (short) GENERO_MASCULINO_CODE : (short) GENERO_FEMENINO_CODE;

        LocalDate fechaNacimiento = null;
        if (dto.fechaNacimiento() != null) {
            fechaNacimiento = new java.sql.Date(dto.fechaNacimiento().getTime()).toLocalDate();
        }
        usuario.setDocumento(dto.documento());
        usuario.setNombres(dto.nombres());
        usuario.setApellidos(dto.apellidos());
        usuario.setGenero(genero);
        usuario.setFechaNacimiento(fechaNacimiento);
        usuario.setTelefono(dto.telefono());
        usuario.setRol(rol);
    }

    public void applyUpdateAdmin(Usuario usuario, UserUpdateAdminDto dto, Rol rol) {
        Short genero = dto.genero().equalsIgnoreCase("masculino")
                ? (short) GENERO_MASCULINO_CODE : (short) GENERO_FEMENINO_CODE;

        usuario.setDocumento(dto.documento());
        usuario.setNombres(dto.nombres());
        usuario.setApellidos(dto.apellidos());
        usuario.setGenero(genero);
        usuario.setFechaNacimiento(dto.fechaNacimiento());
        usuario.setTelefono(dto.telefono());
        usuario.setRol(rol);
        usuario.setEstado(dto.estado());
    }


}