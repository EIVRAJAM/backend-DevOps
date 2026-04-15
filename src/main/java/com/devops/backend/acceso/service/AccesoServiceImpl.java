package com.devops.backend.acceso.service;

import com.devops.backend.acceso.entity.*;
import com.devops.backend.acceso.repository.*;
import com.devops.backend.exception.ApiValidationError;
import com.devops.backend.exception.BadRequestException;
import com.devops.backend.exception.ConflictException;
import com.devops.backend.usuario.entity.*;
import com.devops.backend.usuario.repository.*;
import com.devops.backend.rol.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.devops.backend.acceso.util.PasswordGeneratorUtil;

@Service
public class AccesoServiceImpl implements AccesoService {

    @Autowired
    private AccesoRepository accesoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<Acceso> findAll() {
        return (List<Acceso>) accesoRepository.findAll();
    }

    @Override
    @Transactional
    public Acceso save(Acceso acceso) {
        // Validar existencia de usuario
        Long idUsuario = acceso.getUsuario().getIdUsuario();
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new BadRequestException("No existe un usuario con ID: " + idUsuario));

        // Validar unicidad de username y correo
        List<ApiValidationError> errors = new java.util.ArrayList<>();
        if (accesoRepository.existsByUsername(acceso.getUsername())) {
            errors.add(new ApiValidationError("username", "El username ya está en uso"));
        }

        if (accesoRepository.existsByCorreoAcceso(acceso.getCorreoAcceso())) {
            errors.add(new ApiValidationError("correoAcceso", "El correo ya está en uso"));
        }

        if (!errors.isEmpty()) {
            throw new ConflictException("Campos duplicados en el registro", errors);
        }

        // Enlazar el usuario
        acceso.setUsuario(usuario);

        // Encriptar clave
        acceso.setClaveAcceso(passwordEncoder.encode(acceso.getClaveAcceso()));
        System.out.println(acceso.getClaveAcceso());
        return accesoRepository.save(acceso);
    }

    @Override
    @Transactional
    public Acceso saveWithDefaultPassword(Long idUsuario, String username, String correoAcceso) {
        // Validar existencia de usuario
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new BadRequestException("No existe un usuario con ID: " + idUsuario));

        // Validar unicidad de username y correo
        List<ApiValidationError> errors = new java.util.ArrayList<>();
        if (accesoRepository.existsByUsername(username)) {
            errors.add(new ApiValidationError("username", "El username ya está en uso"));
        }

        if (accesoRepository.existsByCorreoAcceso(correoAcceso)) {
            errors.add(new ApiValidationError("correoAcceso", "El correo ya está en uso"));
        }

        if (!errors.isEmpty()) {
            throw new ConflictException("Campos duplicados en el registro", errors);
        }

        // Crear entidad Acceso con contraseña aleatoria generada
        Acceso acceso = new Acceso();
        acceso.setUsuario(usuario);
        acceso.setUsername(username);
        acceso.setCorreoAcceso(correoAcceso);
        acceso.setClaveAcceso(passwordEncoder.encode(PasswordGeneratorUtil.generateSecurePassword()));
        acceso.setEstadoCuenta("ACTIVO");
        acceso.setIntentosFallidos(0);

        return accesoRepository.save(acceso);
    }

}
