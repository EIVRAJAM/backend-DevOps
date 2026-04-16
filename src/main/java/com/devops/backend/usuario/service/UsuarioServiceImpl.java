package com.devops.backend.usuario.service;

import com.devops.backend.acceso.repository.AccesoRepository;
import com.devops.backend.auth.dto.SignUpRequest;
import com.devops.backend.exception.ApiValidationError;
import com.devops.backend.exception.BadRequestException;
import com.devops.backend.exception.ConflictException;
import com.devops.backend.rol.entity.Rol;
import com.devops.backend.rol.repository.RolRepository;
import com.devops.backend.usuario.dto.SignUpResponseUsuario;
import com.devops.backend.usuario.dto.UserListResponse;
import com.devops.backend.usuario.dto.UsuarioDTO;
import com.devops.backend.usuario.dto.UsuarioFilterRequest;
import com.devops.backend.usuario.entity.Usuario;
import com.devops.backend.usuario.mapper.UsuarioMapper;
import com.devops.backend.usuario.repository.UsuarioRepository;
import com.devops.backend.usuario.specification.UsuarioSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;


@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final AccesoRepository accesoRepository;
    private final UsuarioMapper usuarioMapper;
    private static final String DEFAULT_ROLE = "ROLE_USER";


    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              RolRepository rolRepository, AccesoRepository accesoRepository, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.accesoRepository = accesoRepository;
        this.usuarioMapper = usuarioMapper;
    }

    @Override
    public SignUpResponseUsuario saveUser(SignUpRequest signupRequest) {

        Long idRol = obtenerRolPorDefecto();
        validaciones(signupRequest);
        Usuario usuario = save(usuarioMapper.toDTO(signupRequest,idRol));

        return  usuarioMapper.toResponse(usuario);
    }

    private void validaciones(SignUpRequest signupRequest) {
        List<ApiValidationError> errors = new ArrayList<>();
        if (accesoRepository.existsByUsername(signupRequest.username())) {
            errors.add(new ApiValidationError("username", "El username ya está en uso"));
        }
        if (accesoRepository.existsByCorreoAcceso(signupRequest.correoAcceso())) {
            errors.add(new ApiValidationError("correoAcceso", "El correo ya está registrado"));
        }
        if (!errors.isEmpty()) {
            throw new ConflictException("Campos duplicados en el registro", errors);
        }
    }

    private Long obtenerRolPorDefecto() {
        return rolRepository.findByNombreRol(DEFAULT_ROLE)
                .orElseThrow(() -> new BadRequestException(
                        "El rol por defecto " + DEFAULT_ROLE + " no está disponible"))
                .getIdRol();
    }

    @Override
    public Usuario save(UsuarioDTO dto) {
        validacionesDto(dto);

        // Obtener el rol válido desde la BD
        Rol validRol = rolRepository.findById(dto.idRol())
                .orElseThrow(() -> new BadRequestException("El rol con ID " + dto.idRol() + " no existe"));

        return usuarioRepository.save(usuarioMapper.toUsuario(dto,validRol));
    }

    @Override
    public List<UserListResponse> getAllUsers() {

        return StreamSupport.stream(usuarioRepository.findAll().spliterator(),false).
                //    Nota: Aclarar con el equipo si lo dejamos la lista modificable o sin modifical .collect(Collectors.toList()
                map(usuarioMapper::toListResponse).toList();
    }

    @Override
    public Page<UserListResponse> getAllUsers(UsuarioFilterRequest f) {

        Specification<Usuario> spec = Specification
                .where(UsuarioSpecification.porDocumento(f.documento()))
                .and(UsuarioSpecification.porNombre(f.nombre()))
                .and(UsuarioSpecification.porApellido(f.apellido()))
                .and(UsuarioSpecification.porRol(f.nombreRol()));

        PageRequest pageable = PageRequest.of(f.page(), f.size());

        return usuarioRepository.findAll(spec, pageable)
                .map(usuarioMapper::toListResponse); // sin .toList() para que no dañe los meta datos
    }

    private void validacionesDto(UsuarioDTO dto) {

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

    }
}
