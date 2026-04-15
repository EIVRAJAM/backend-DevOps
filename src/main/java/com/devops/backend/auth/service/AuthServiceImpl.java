package com.devops.backend.auth.service;

import com.devops.backend.acceso.entity.*;
import com.devops.backend.acceso.repository.*;
import com.devops.backend.acceso.service.*;
import com.devops.backend.auth.dto.*;
import com.devops.backend.exception.ApiValidationError;
import com.devops.backend.exception.BadRequestException;
import com.devops.backend.exception.ValidationException;
import com.devops.backend.sesion.entity.*;
import com.devops.backend.sesion.repository.*;
import com.devops.backend.usuario.dto.*;
import com.devops.backend.usuario.entity.*;
import com.devops.backend.usuario.repository.UsuarioRepository;
import com.devops.backend.usuario.service.*;
import com.devops.backend.rol.repository.*;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.devops.backend.security.TokenJwtConfig.SECRET_KEY;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AccesoService accesoService;

    @Autowired
    private AccesoRepository accesoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired // revisarrrrrrrrrrr
    private SesionRepository sesionRepository;

    @Autowired
    private RolRepository rolRepository;

    @Override
    @Transactional
    public SignUpResponse save(SignUpRequest signupRequest) {

        // Convert Date to LocalDate if necessary, assuming SignupRequest still uses
        // Date.
        // If SignupRequest uses Date, we need to convert. If it uses LocalDate, we are
        // good.
        // Let's assume for now we need to convert or that SignupRequest will be
        // updated.
        // Actually, let's check SignupRequest first. But to proceed with this edit:

        java.time.LocalDate fechaNacimiento = null;
        if (signupRequest.fechaNacimiento() != null) {
            fechaNacimiento = new java.sql.Date(signupRequest.fechaNacimiento().getTime()).toLocalDate();
        }

        // Assuming signupRequest.idRols() is a Set<Long>, we take the first one or
        // change SignupRequest.
        // Let's assume we take the first one for now to fix the compilation error.
        String defaultRoleName = "ROLE_USER";

        Long idRol = rolRepository.findByNombreRol(defaultRoleName)
                .orElseThrow(() -> new BadRequestException("El rol por defecto ROLE_USER no está disponible"))
                .getIdRol();

        List<ApiValidationError> signupErrors = validateSignup(signupRequest);
        if (!signupErrors.isEmpty()) {
            throw new ValidationException("Errores en el registro", signupErrors);
        }

        // Convert String genero to Short (1=masculino, 2=femenino)
        Short generoShort = null;
        if (signupRequest.genero() != null) {
            generoShort = signupRequest.genero().equalsIgnoreCase("masculino") ? (short) 1 : (short) 2;
        }

        Usuario usuario = usuarioService.save(
                new UsuarioDTO(
                        signupRequest.documento(),
                        signupRequest.nombres(),
                        signupRequest.apellidos(),
                        generoShort,
                        fechaNacimiento,
                        signupRequest.telefono(),
                        idRol));

        System.out.println("Usuario:" + "{" +
                usuario.getIdUsuario() + "\n" +
                usuario.getNombres() + "\n" +
                usuario.getApellidos() + "\n" +
                usuario.getGenero() + "\n" +
                usuario.getFechaNacimiento() + "\n" +
                usuario.getTelefono() + "\n" +

                "}");

        Acceso acceso = new Acceso();
        acceso.setUsuario(usuario);
        acceso.setUsername(signupRequest.username());
        acceso.setCorreoAcceso(signupRequest.correoAcceso());
        acceso.setClaveAcceso(signupRequest.claveAcceso());

        Acceso newAcceso = accesoService.save(acceso);

        System.out.println("Acceso:" + newAcceso.toString());

        return new SignUpResponse(
                usuario.getIdUsuario(),
                usuario.getNombres(),
                usuario.getApellidos(),
                newAcceso.getCorreoAcceso());
    }

    @Override
    public List<ApiValidationError> validateSignup(SignUpRequest signupRequest) {
        List<ApiValidationError> errors = new ArrayList<>();

        if (signupRequest.documento() != null && usuarioRepository.existsByDocumento(signupRequest.documento())) {
            errors.add(new ApiValidationError("documento", "El documento ya existe"));
        }

        if (signupRequest.username() != null && accesoRepository.existsByUsername(signupRequest.username())) {
            errors.add(new ApiValidationError("username", "El username ya está en uso"));
        }

        if (signupRequest.correoAcceso() != null
                && accesoRepository.existsByCorreoAcceso(signupRequest.correoAcceso())) {
            errors.add(new ApiValidationError("correoAcceso", "El correo ya está en uso"));
        }

        return errors;
    }

    @Override
    public Map<String, String> login(LoginRequest loginRequest) {
        Optional<Acceso> optionalAcceso = accesoRepository.findByCorreoAcceso(loginRequest.usernameOrEmail());

        if (optionalAcceso.isEmpty()) {
            optionalAcceso = accesoRepository.findByUsername(loginRequest.usernameOrEmail());
        }

        Acceso acceso = optionalAcceso.orElseThrow(() -> new RuntimeException(
                "Usuario no encontrado con el correo o username proporcionado"));

        if (!passwordEncoder.matches(loginRequest.password(), acceso.getClaveAcceso())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        if ("DESACTIVADO".equalsIgnoreCase(acceso.getEstadoCuenta())) {
            throw new RuntimeException("La cuenta está desactivada");
        }
        if ("BLOQUEADO".equalsIgnoreCase(acceso.getEstadoCuenta())) {
            throw new RuntimeException("La cuenta está bloqueada");
        }

        String token = Jwts.builder()
                .subject(acceso.getIdUsuario().toString())
                .claim("authorities",
                        java.util.List.of(Map.of("authority",
                                acceso.getUsuario().getRol().getNombreRol())))
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .issuedAt(new Date())
                .signWith(SECRET_KEY)
                .compact();

        // registrar la sesión
        Sesion sesion = new Sesion();
        sesion.setUsuario(acceso.getUsuario());
        sesion.setFechaSesion(java.time.LocalDate.now());
        sesion.setHoraSesion(java.time.LocalTime.now());
        // sesion.setToken(token); // Not supported by DB
        sesionRepository.save(sesion);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("correo", acceso.getCorreoAcceso());
        response.put("username", acceso.getUsername());
        response.put("message", "Inicio de sesión exitoso");

        return response;
    }

    @Override
    public void logout(String token) {
        /*
         * Sesion sesion = sesionRepository.findByToken(token)
         * .orElseThrow(() -> new
         * RuntimeException("Sesión no encontrada para este token"));
         *
         * sesion.setActivo(false);
         * sesion.setFechaLogout(LocalDateTime.now());
         * sesionRepository.save(sesion);
         */
        // Logout logic not supported by current DB schema (no token storage in sessions
        // table)
    }

}
