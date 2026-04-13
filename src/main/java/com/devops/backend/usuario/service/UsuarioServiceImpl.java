package com.devops.backend.usuario.service;

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

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository RolRepository;

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
        Rol validRol = RolRepository.findById(dto.idRol())
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
