package com.devops.backend.usuario.service;

import com.devops.backend.acceso.repository.AccesoRepository;
import com.devops.backend.auth.dto.SignUpRequest;
import com.devops.backend.auth.dto.SignUpResponse;
import com.devops.backend.exception.ApiValidationError;
import com.devops.backend.exception.BadRequestException;
import com.devops.backend.exception.ConflictException;
import com.devops.backend.rol.entity.Rol;
import com.devops.backend.rol.repository.RolRepository;
import com.devops.backend.usuario.dto.UsuarioDTO;
import com.devops.backend.usuario.entity.Usuario;
import com.devops.backend.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final AccesoRepository accesoRepository;


    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              RolRepository rolRepository, AccesoRepository accesoRepository) {

        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.accesoRepository = accesoRepository;
    }

    @Override
    public Usuario saveUser(SignUpRequest signupRequest) {

        String defaultRoleName = "ROLE_USER";

        Long idRol = rolRepository.findByNombreRol(defaultRoleName)
                .orElseThrow(() -> new BadRequestException("El rol por defecto ROLE_USER no está disponible"))
                .getIdRol();

        Short generoShort = signupRequest.genero().equalsIgnoreCase("masculino") ? (short) 1 : (short) 2;

        java.time.LocalDate fechaNacimiento = null;
        if (signupRequest.fechaNacimiento() != null) {
            fechaNacimiento = new java.sql.Date(signupRequest.fechaNacimiento().getTime()).toLocalDate();
        }
        // Aqui Construyo el UsuarioDTO y delego a save()

        UsuarioDTO dto = new UsuarioDTO(
                signupRequest.documento(),
                signupRequest.nombres(),
                signupRequest.apellidos(),
                generoShort,
                fechaNacimiento,
                signupRequest.telefono(),
                idRol
        );

        return save(dto);
    }

    @Override
    public Usuario save(UsuarioDTO dto) {
        List<ApiValidationError> errors = new ArrayList<>();

        // Validar que el documento no esté repetido
        if (usuarioRepository.existsByDocumento(dto.documento())) {
            System.out.println("DEBUG: Documento repetido");
            errors.add(new ApiValidationError("documento", "El documento ya existe"));
        }

        // Validar que haya un rol
        if (dto.idRol() == null) {
            System.out.println("DEBUG: Rol nulo");
            errors.add(new ApiValidationError("idRol", "El usuario debe tener un rol"));
        }

        if (!errors.isEmpty()) {
            throw new ConflictException("Campos duplicados o inválidos en el registro", errors);
        }

        // Obtener el rol válido desde la BD
        Rol validRol = rolRepository.findById(dto.idRol())
                .orElseThrow(() -> new BadRequestException("El rol con ID " + dto.idRol() + " no existe"));

        // Mapear el DTO a entidad
        Usuario usuario = new Usuario();
        usuario.setDocumento(dto.documento());
        usuario.setNombres(dto.nombres());
        usuario.setApellidos(dto.apellidos());
        usuario.setGenero(dto.genero());
        usuario.setFechaNacimiento(dto.fechaNacimiento());
        usuario.setTelefono(dto.telefono());
        usuario.setRol(validRol);
        usuario.setEstado("ACTIVO");

        return usuarioRepository.save(usuario);
    }

}
