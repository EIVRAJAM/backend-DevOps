package com.devops.backend.auth.service;

import com.devops.backend.acceso.entity.Acceso;
import com.devops.backend.acceso.repository.AccesoRepository;
import com.devops.backend.auth.dto.ForgotPasswordRequest;
import com.devops.backend.auth.dto.ResetPasswordRequest;
import com.devops.backend.auth.entity.PasswordResetCode;
import com.devops.backend.auth.repository.PasswordResetCodeRepository;
import com.devops.backend.exception.BadRequestException;
import com.devops.backend.usuario.entity.Usuario;
import com.devops.backend.usuario.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    @Autowired
    private PasswordResetCodeRepository passwordResetCodeRepository;

    @Autowired
    private AccesoRepository accesoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${password-reset.code-expiration-minutes:10}")
    private int codeExpirationMinutes;

    @Value("${password-reset.max-attempts:5}")
    private int maxAttempts;

    private static final Random random = new Random();

    @Override
    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        try {
            // 1. Buscar usuario por email (en la tabla accesos)
            Optional<Acceso> accesoOpt = accesoRepository.findByCorreoAcceso(request.email());

            if (accesoOpt.isEmpty()) {
                // Por seguridad, no revelar que el email no existe
                log.info("Solicitud de reseteo para email no registrado: {}", request.email());
                return;
            }

            Acceso acceso = accesoOpt.get();
            Usuario usuario = acceso.getUsuario();

            // 2. Verificar si la cuenta está bloqueada
            if ("BLOQUEADO".equalsIgnoreCase(acceso.getEstadoCuenta())) {
                throw new BadRequestException(
                        "La cuenta está bloqueada. Debe desbloquearla antes de continuar con la recuperación de contraseña");
            }

            // 3. Invalidar códigos anteriores no usados
            passwordResetCodeRepository.invalidateAllCodesForUser(usuario);

            // 4. Generar código de 6 dígitos
            String codigo = generateResetCode();

            // 5. Guardar en BD con expiración
            PasswordResetCode resetCode = new PasswordResetCode();
            resetCode.setUsuario(usuario);
            resetCode.setCodigo(codigo);
            resetCode.setFechaExpiracion(LocalDateTime.now().plusMinutes(codeExpirationMinutes));
            resetCode.setUsado(false);
            resetCode.setIntentos(0);

            passwordResetCodeRepository.save(resetCode);

            // 6. Enviar correo con el código
            try {
                emailService.sendPasswordResetEmail(request.email(), codigo, codeExpirationMinutes);
                log.info("Código de recuperación enviado a: {}", request.email());
            } catch (Exception e) {
                log.error("Error al enviar correo a: {}", request.email(), e);
                // No lanzamos excepción para no revelar que fallo el envío
            }

        } catch (BadRequestException e) {
            // Re-lanzar validaciones de negocio sin envolver
            throw e;
        } catch (Exception e) {
            log.error("Error en requestPasswordReset", e);
            throw new RuntimeException("Error al procesar la solicitud de recuperación", e);
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // 1. Validar que las contraseñas coincidan
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException("Las contraseñas no coinciden");
        }

        // 2. Buscar usuario por email
        Optional<Acceso> accesoOpt = accesoRepository.findByCorreoAcceso(request.email());
        if (accesoOpt.isEmpty()) {
            throw new BadRequestException("El usuario no existe");
        }

        Acceso acceso = accesoOpt.get();
        //Usuario usuario = acceso.getUsuario();

        // 2.5 Verificar si la cuenta está bloqueada
        if ("BLOQUEADO".equalsIgnoreCase(acceso.getEstadoCuenta())) {
            throw new BadRequestException(
                    "La cuenta está bloqueada. Debe desbloquearla antes de continuar con la recuperación de contraseña");
        }

        // 3. Buscar y validar el código
        PasswordResetCode resetCode = validateResetCode(request.email(), request.code());

        // 4. Si el código es válido:
        // - Actualizar contraseña
        acceso.setClaveAcceso(passwordEncoder.encode(request.newPassword()));
        acceso.setIntentosFallidos(0); // Resetear intentos fallidos
        accesoRepository.save(acceso);

        // - Marcar código como usado
        resetCode.markAsUsed();
        passwordResetCodeRepository.save(resetCode);

        log.info("Contraseña resetada exitosamente para usuario: {}", request.email());
    }

    @Override
    public String generateResetCode() {
        // Generar código de 6 dígitos
        return String.format("%06d", random.nextInt(1000000));
    }

    @Override
    public PasswordResetCode validateResetCode(String email, String codigo) {
        // 1. Buscar usuario por email
        Optional<Acceso> accesoOpt = accesoRepository.findByCorreoAcceso(email);
        if (accesoOpt.isEmpty()) {
            throw new BadRequestException("El usuario no existe");
        }

        Usuario usuario = accesoOpt.get().getUsuario();

        // 2. Buscar código válido
        Optional<PasswordResetCode> resetCodeOpt = passwordResetCodeRepository.findValidCode(usuario, codigo);
        if (resetCodeOpt.isEmpty()) {
            // Intentar encontrar el código aunque sea inválido para aumentar intentos
            Optional<PasswordResetCode> anyCodeOpt = passwordResetCodeRepository.findByUsuarioAndCodigo(usuario,
                    codigo);

            if (anyCodeOpt.isPresent()) {
                PasswordResetCode resetCode = anyCodeOpt.get();

                // Validar cada condición para dar mensajes específicos
                if (resetCode.getUsado()) {
                    throw new BadRequestException("El código ya fue utilizado");
                }

                if (resetCode.isExpired()) {
                    throw new BadRequestException("El código ha expirado");
                }

                if (resetCode.getIntentos() >= maxAttempts) {
                    throw new BadRequestException("Máximo número de intentos excedido");
                }

                // Si no es ninguna de las anteriores, aumentar intentos
                resetCode.incrementAttempts();
                passwordResetCodeRepository.save(resetCode);
                throw new BadRequestException("El código es inválido");
            }

            throw new BadRequestException("El código no existe");
        }

        return resetCodeOpt.get();
    }
}
