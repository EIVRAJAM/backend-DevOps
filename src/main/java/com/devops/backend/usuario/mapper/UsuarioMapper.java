package com.devops.backend.usuario.mapper;

import com.devops.backend.usuario.dto.SignUpUserRequest;
import com.devops.backend.rol.entity.Rol;
import com.devops.backend.usuario.dto.*;
import com.devops.backend.usuario.entity.Usuario;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class UsuarioMapper {
    private static final short GENERO_MASCULINO_CODE = 1;
    private static final short GENERO_FEMENINO_CODE = 2;



    public UsuarioDTO toDTOUser(SignUpUserRequest request, Long idRol) {
        return buildUsuarioDTO(request.documento(), request.nombres(), request.apellidos(),
                request.genero(), request.fechaNacimiento(), request.telefono(), idRol);
    }

    private UsuarioDTO buildUsuarioDTO(String documento, String nombres, String apellidos,
                                       String genero, java.util.Date fechaNacimientoDate,
                                       String telefono, Long idRol) {
        Short generoCode = toCodeFromGenero(genero);

        LocalDate fechaNacimiento = null;
        if (fechaNacimientoDate != null) {
            fechaNacimiento = new java.sql.Date(fechaNacimientoDate.getTime()).toLocalDate();
        }

        return new UsuarioDTO(documento, nombres, apellidos, generoCode, fechaNacimiento, telefono, idRol);
    }

    public SignUpResponseUsuario toResponse(Usuario usuario) {
        return new SignUpResponseUsuario(
                usuario.getIdUsuario(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getTelefono(),
                usuario.getRol().getNombreRol()
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
        String genero = toGeneroFromCode(u.getGenero());
        return new UserListResponse(u.getIdUsuario(),
                                    u.getNombres(),
                                    u.getApellidos(),
                                    u.getDocumento(),
                                    genero,
                                    u.getFechaNacimiento().toString(),
                                    u.getTelefono(),
                                    u.getEstado());
    }    
    public UserResponseAdmin toUserResponseAdmin(Usuario u){
        String genero = toGeneroFromCode(u.getGenero());
        return new UserResponseAdmin(
                u.getIdUsuario(),
                u.getNombres(),
                u.getApellidos(),
                u.getDocumento(),
                genero,
                u.getFechaNacimiento() != null ? u.getFechaNacimiento().toString() : null,
                u.getTelefono(),
                u.getEstado(),
                u.getRol().getNombreRol(),
                u.getCreadoEn() != null ? u.getCreadoEn().toString() : null,
                u.getActualizadoEn() != null ? u.getActualizadoEn().toString() : null
        );
    }


    public UserUpdateAdminResponse toUpdateAdminResponse(Usuario u) {
        String genero = toGeneroFromCode(u.getGenero());

        return new UserUpdateAdminResponse(
                u.getIdUsuario(),
                u.getDocumento(),
                u.getNombres(),
                u.getApellidos(),
                genero,
                u.getFechaNacimiento(),
                u.getTelefono(),
                u.getEstado(),
                u.getRol().getNombreRol(),
                u.getCreadoEn(),
                u.getActualizadoEn()
        );
    }

    public void applyUpdate(Usuario usuario, UpdateUsuarioRequest dto) {
        Short genero = toCodeFromGenero(dto.genero());

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
    }

    public void applyUpdateAdmin(Usuario usuario, UserUpdateAdminDto dto, Rol rol) {
        Short genero = toCodeFromGenero(dto.genero());

        usuario.setDocumento(dto.documento());
        usuario.setNombres(dto.nombres());
        usuario.setApellidos(dto.apellidos());
        usuario.setGenero(genero);
        usuario.setFechaNacimiento(dto.fechaNacimiento());
        usuario.setTelefono(dto.telefono());
        usuario.setRol(rol);
        usuario.setEstado(dto.estado());
    }
    private String toGeneroFromCode(Short genero) {
        if (genero == null) {
            return null;
        }
        return genero == GENERO_MASCULINO_CODE ? "masculino" : "femenino";
    }
    private short toCodeFromGenero(String genero) {
        return genero.equalsIgnoreCase("masculino")
                ? (short) GENERO_MASCULINO_CODE : (short) GENERO_FEMENINO_CODE;
    }

}