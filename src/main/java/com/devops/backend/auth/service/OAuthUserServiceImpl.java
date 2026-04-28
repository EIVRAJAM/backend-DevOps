package com.devops.backend.auth.service;

import com.devops.backend.acceso.entity.Acceso;
import com.devops.backend.acceso.repository.AccesoRepository;
import com.devops.backend.rol.entity.Rol;
import com.devops.backend.rol.repository.RolRepository;
import com.devops.backend.usuario.entity.Usuario;
import com.devops.backend.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio para procesar autenticación OAuth2
 * Gestiona búsqueda, creación o reutilización de usuarios OAuth
 */
@Service
@RequiredArgsConstructor
public class OAuthUserServiceImpl {

    private final UsuarioRepository usuarioRepository;
    private final AccesoRepository accesoRepository;
    private final RolRepository rolRepository;

    /**
     * Procesa usuario desde OAuth2 (Google)
     * Busca usuario por correo, si existe lo reutiliza (auto-migrate),
     * si no existe crea uno nuevo
     *
     * @param email    Email del usuario de Google
     * @param nombre   Nombre del usuario
     * @param apellido Apellido del usuario
     * @return Usuario autenticado
     */
    @Transactional
    public Usuario processOAuthUser(String email, String nombre, String apellido) {
        // Paso 1: Buscar usuario por correo (reutilizar automáticamente)
        Optional<Acceso> existingAcceso = accesoRepository.findByCorreoAcceso(email);

        if (existingAcceso.isPresent()) {
            Acceso acceso = existingAcceso.get();
            Usuario usuario = acceso.getUsuario();

            // Validar que la cuenta no esté bloqueada
            if ("BLOQUEADO".equals(acceso.getEstadoCuenta()) || "BLOQUEADO".equals(usuario.getEstado())) {
                throw new IllegalStateException("Cuenta bloqueada. Contacte con administración.");
            }

            // Auto-migrate: usuario existente puede ahora usar OAuth (reutilizar)
            return usuario;
        }

        // Paso 2: Crear nuevo usuario y credencial
        // 2a. Obtener rol por defecto: ROLE_USER
        Rol rolUser = rolRepository.findByNombreRol("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Rol ROLE_USER no encontrado en base de datos"));

        // 2b. Generar username único a partir del email
        String baseUsername = sanitizeUsernameFromEmail(email);
        String uniqueUsername = generateUsernameUnique(baseUsername);

        // 2c. Crear usuario nuevo
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setRol(rolUser);
        nuevoUsuario.setNombres(nombre != null ? nombre : email.split("@")[0]);
        nuevoUsuario.setApellidos(apellido != null ? apellido : "");
        nuevoUsuario.setDocumento("OAUTH_" + email); // documento temporal
        nuevoUsuario.setEstado("ACTIVO");
        nuevoUsuario = usuarioRepository.save(nuevoUsuario);

        // 2d. Crear acceso sin contraseña (NULL para OAuth)
        Acceso nuevoAcceso = new Acceso();
        nuevoAcceso.setUsuario(nuevoUsuario); // @MapsId asignará automáticamente idUsuario
        nuevoAcceso.setUsername(uniqueUsername);
        nuevoAcceso.setCorreoAcceso(email);
        nuevoAcceso.setClaveAcceso(null); // NULL para OAuth, sin contraseña local
        nuevoAcceso.setEstadoCuenta("ACTIVO");
        nuevoAcceso.setUuidAcceso(UUID.randomUUID());
        nuevoAcceso.setUltimoLogin(LocalDateTime.now());
        nuevoAcceso.setIntentosFallidos(0);
        accesoRepository.save(nuevoAcceso);

        // Actualizar relación bidireccional
        nuevoUsuario.setAcceso(nuevoAcceso);
        usuarioRepository.save(nuevoUsuario);

        return nuevoUsuario;
    }

    /**
     * Extrae la parte antes del @ del email y la sanitiza
     * Ejemplo: "juan.perez@gmail.com" → "juan.perez"
     */
    private String sanitizeUsernameFromEmail(String email) {
        String basePart = email.split("@")[0];

        // Permitir solo alphanumeric, _, .
        String sanitized = basePart.replaceAll("[^a-zA-Z0-9_.]", "");

        // Limitar a 50 caracteres (máximo en BD)
        if (sanitized.length() > 50) {
            sanitized = sanitized.substring(0, 50);
        }

        return sanitized.isEmpty() ? "user_" + System.currentTimeMillis() : sanitized;
    }

    /**
     * Genera un username único verificando disponibilidad en BD
     * Si baseUsername está disponible, lo retorna
     * Si no, agrega sufijo: baseUsername_XXXX hasta encontrar disponible
     */
    private String generateUsernameUnique(String baseUsername) {
        // Verificar si el username base está disponible
        if (!accesoRepository.existsByUsername(baseUsername)) {
            return baseUsername;
        }

        // Si no está disponible, agregar sufijo aleatorio
        for (int i = 0; i < 100; i++) {
            String suffixedUsername = baseUsername + "_" + (1000 + (int) (Math.random() * 9000));
            if (!accesoRepository.existsByUsername(suffixedUsername)) {
                return suffixedUsername;
            }
        }

        // Fallback: usar email completo con timestamp
        throw new IllegalStateException("No se pudo generar username único para: " + baseUsername);
    }
}
