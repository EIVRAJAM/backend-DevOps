package com.devops.backend.usuario.service;

import com.devops.backend.acceso.repository.AccesoRepository;
import com.devops.backend.exception.ApiValidationError;
import com.devops.backend.exception.BadRequestException;
import com.devops.backend.exception.ConflictException;
import com.devops.backend.exception.ResourceNotFoundException;
import com.devops.backend.rol.entity.Rol;
import com.devops.backend.rol.repository.RolRepository;
import com.devops.backend.usuario.dto.*;
import com.devops.backend.usuario.entity.Usuario;
import com.devops.backend.usuario.mapper.UsuarioMapper;
import com.devops.backend.usuario.repository.UsuarioRepository;
import com.devops.backend.usuario.specification.UsuarioSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    public SignUpResponseUsuario saveUser(SignUpUserRequest signupRequest) {

        Long idRol = obtenerRolPorDefecto();
        Usuario usuario = save(usuarioMapper.toDTOUser(signupRequest,idRol));

        return  usuarioMapper.toResponse(usuario);
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
    public UserListResponse findById(Long id) {
        return usuarioRepository.findByIdUsuario(id)
                .map(usuarioMapper::toListResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),"Usuario con ID " + id + " no encontrado"));
    }

    @Override
    public UserListResponse findByDocumento(String documento) {
        return usuarioRepository.findByDocumento(documento)
                .map(usuarioMapper::toListResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),"Usuario con documento " + documento + " no encontrado"));
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

    @Override
    @Transactional
    public UserListResponse updateUser(Long id, UpdateUsuarioRequest request) {

        Usuario usuario = usuarioRepository.findByIdUsuario(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatusCode.valueOf(404), "Usuario con ID " + id + " no encontrado"));

        List<ApiValidationError> errors = new ArrayList<>();

        if (usuarioRepository.existsByDocumentoAndIdUsuarioNot(request.documento(), id)) {
            errors.add(new ApiValidationError("documento", "El documento ya está registrado por otro usuario"));
        }

        if (usuarioRepository.existsByTelefonoAndIdUsuarioNot(request.telefono(),id)){
            errors.add(new ApiValidationError("telefono", "El telefono ya está registrado por otro usuario"));
        }

        Rol rol = rolRepository.findById(request.idRol())
                .orElseGet(() -> {
                    errors.add(new ApiValidationError("idRol", "El rol con ID " + request.idRol() + " no existe"));
                    return null;
       });


        if (!errors.isEmpty()) {
            throw new ConflictException("Campos inválidos en la actualización", errors);
        }

        // 4. Aplicar cambios y persistir
        usuarioMapper.applyUpdate(usuario, request, rol);
        Usuario updated = usuarioRepository.save(usuario);

        return usuarioMapper.toListResponse(updated);
    }

    @Override
    @Transactional
    public UserUpdateAdminResponse updateUserAdmin(Long id, UserUpdateAdminDto request) {

        Usuario usuario = usuarioRepository.findByIdUsuario(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado"));

        List<ApiValidationError> errors = new ArrayList<>();

        if (usuarioRepository.existsByDocumentoAndIdUsuarioNot(request.documento(), id)) {
            errors.add(new ApiValidationError("documento", "El documento ya está registrado por otro usuario"));
        }

        if (request.telefono() != null && usuarioRepository.existsByTelefonoAndIdUsuarioNot(request.telefono(), id)) {
            errors.add(new ApiValidationError("telefono", "El teléfono ya está registrado por otro usuario"));
        }

        Rol rol = rolRepository.findById(request.idRol())
                .orElseGet(() -> {
                    errors.add(new ApiValidationError("idRol", "El rol con ID " + request.idRol() + " no existe"));
                    return null;
        });

        if (!isEstadoValido(request.estado())) {
            errors.add(new ApiValidationError("estado", "El estado debe ser ACTIVO, INACTIVO o BLOQUEADO"));
        }

        if (!errors.isEmpty()) {
            throw new ConflictException("Campos inválidos en la actualización", errors);
        }

        usuarioMapper.applyUpdateAdmin(usuario, request, rol);
        Usuario updated = usuarioRepository.save(usuario);

        return usuarioMapper.toUpdateAdminResponse(updated);
    }

    private boolean isEstadoValido(String estado) {
        return estado != null && (estado.equals("ACTIVO") || estado.equals("INACTIVO") || estado.equals("BLOQUEADO"));
    }

    @Override
    public UserListResponse activar(Long id) {
        return updateStatus(id,"ACTIVO");
    }

    @Override
    public UserListResponse desactivar(Long id) {
        return updateStatus(id,"INACTIVO");
    }
    @Override
    public UserListResponse bloquear(Long id) {
        return updateStatus(id,"BLOQUEADO");
    }

    private UserListResponse updateStatus(Long id, String status){

        Usuario usuario = usuarioRepository.findByIdUsuario(id).
                orElseThrow(() -> new ResponseStatusException(
                        HttpStatusCode.valueOf(404), "Usuario con ID " + id + " no encontrado"));

        usuario.setEstado(status);

        return usuarioMapper.toListResponse(usuarioRepository.save(usuario));
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
