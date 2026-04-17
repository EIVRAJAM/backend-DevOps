package com.devops.backend.auth.service;

import com.devops.backend.acceso.entity.Acceso;
import com.devops.backend.acceso.repository.AccesoRepository;
import com.devops.backend.auth.dto.UnlockAccountRequest;
import com.devops.backend.auth.entity.VerificationCode;
import com.devops.backend.auth.repository.VerificationCodeRepository;
import com.devops.backend.exception.BadRequestException;
import com.devops.backend.usuario.entity.Usuario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
public class AccountUnlockServiceImpl implements AccountUnlockService {

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;
    @Autowired
    private AccesoRepository accesoRepository;

    @Autowired
    private EmailService emailService;

    @Value("${account-unlock.code-expiration-minutes:10}")
    private int codeExpirationMinutes;

    @Value("${account-unlock.max-attempts:5}")
    private int maxAttempts;

    private static final Random random = new Random();

    @Override
    @Transactional
    public void requestAccountUnlock(String email) {
        try {
            // 1. Buscar usuario por email
            Optional<Acceso> accesoOpt = accesoRepository.findByCorreoAcceso(email);

            if (accesoOpt.isEmpty()) {
                // Por seguridad, no revelar que el email no existe
                log.info("Solicitud de desbloqueo para email no registrado: {}", email);
                return;
            }

            Acceso acceso = accesoOpt.get();

            // 2. Verificar si la cuenta está bloqueada
            if (!"BLOQUEADO".equalsIgnoreCase(acceso.getEstadoCuenta())) {
                throw new BadRequestException("La cuenta no está bloqueada");
            }

            Usuario usuario = acceso.getUsuario();

            // 3. Invalidar códigos anteriores no usados del tipo UNLOCK_ACCOUNT
            verificationCodeRepository.invalidateAllCodesForUserByType(usuario, "UNLOCK_ACCOUNT");

            // 4. Generar código de 6 dígitos
            String codigo = generateUnlockCode();

            // 5. Guardar en BD con expiración
            VerificationCode unlockCode = new VerificationCode();
            unlockCode.setUsuario(usuario);
            unlockCode.setCodigo(codigo);
            unlockCode.setFechaExpiracion(LocalDateTime.now().plusMinutes(codeExpirationMinutes));
            unlockCode.setUsado(false);
            unlockCode.setIntentos(0);
            unlockCode.setTipoCodigo("UNLOCK_ACCOUNT");

            verificationCodeRepository.save(unlockCode);

            // 6. Enviar correo con el código
            try {
                emailService.sendAccountUnlockEmail(email, codigo, codeExpirationMinutes);
                log.info("Código de desbloqueo enviado a: {}", email);
            } catch (Exception e) {
                log.error("Error al enviar correo de desbloqueo a: {}", email, e);
                // No lanzamos excepción para no revelar que falló el envío
            }

        } catch (BadRequestException e) {
            // Re-lanzar validaciones de negocio sin envolver
            throw e;
        } catch (Exception e) {
            log.error("Error en requestAccountUnlock", e);
            throw new RuntimeException("Error al procesar la solicitud de desbloqueo", e);
        }
    }

    @Override
    @Transactional
    public void unlockAccount(UnlockAccountRequest request) {
        // 1. Buscar usuario por email
        Optional<Acceso> accesoOpt = accesoRepository.findByCorreoAcceso(request.email());
        if (accesoOpt.isEmpty()) {
            throw new BadRequestException("El usuario no existe");
        }

        Acceso acceso = accesoOpt.get();

        // 2. Verificar si la cuenta está bloqueada
        if (!"BLOQUEADO".equalsIgnoreCase(acceso.getEstadoCuenta())) {
            throw new BadRequestException("La cuenta no está bloqueada");
        }

        // 3. Buscar y validar el código
        VerificationCode unlockCode = validateUnlockCode(request.email(), request.code());

        // 4. Si el código es válido:
        // - Cambiar estado de cuenta a ACTIVO
        acceso.setEstadoCuenta("ACTIVO");
        // - Resetear intentos fallidos
        acceso.setIntentosFallidos(0);
        accesoRepository.save(acceso);

        // - Marcar código como usado
        unlockCode.markAsUsed();
        verificationCodeRepository.save(unlockCode);

        log.info("Cuenta desbloqueada exitosamente para usuario: {}", request.email());
    }

    /**
     * Genera un código de 6 dígitos para desbloqueo
     */
    private String generateUnlockCode() {
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * Valida el código de desbloqueo de la cuenta
     */
    private VerificationCode validateUnlockCode(String email, String codigo) {
        // 1. Buscar usuario por email
        Optional<Acceso> accesoOpt = accesoRepository.findByCorreoAcceso(email);
        if (accesoOpt.isEmpty()) {
            throw new BadRequestException("El usuario no existe");
        }

        Usuario usuario = accesoOpt.get().getUsuario();

        // 2. Buscar código válido de tipo UNLOCK_ACCOUNT
        Optional<VerificationCode> unlockCodeOpt = verificationCodeRepository
                .findValidCodeByType(usuario, codigo, "UNLOCK_ACCOUNT");

        if (unlockCodeOpt.isEmpty()) {
            // Intentar encontrar el código aunque sea inválido para aumentar intentos
            Optional<VerificationCode> anyCodeOpt = verificationCodeRepository
                    .findByUsuarioAndCodigoAndTipoCodigo(usuario, codigo, "UNLOCK_ACCOUNT");

            if (anyCodeOpt.isPresent()) {
                VerificationCode unlockCode = anyCodeOpt.get();

                // Validar cada condición para dar mensajes específicos
                if (unlockCode.getUsado()) {
                    throw new BadRequestException("El código ya fue utilizado");
                }

                if (unlockCode.isExpired()) {
                    throw new BadRequestException("El código ha expirado");
                }

                if (unlockCode.getIntentos() >= maxAttempts) {
                    throw new BadRequestException("Máximo número de intentos excedido");
                }

                // Si no es ninguna de las anteriores, aumentar intentos
                unlockCode.incrementAttempts();
                verificationCodeRepository.save(unlockCode);
                throw new BadRequestException("El código es inválido");
            }

            throw new BadRequestException("El código no existe");
        }

        return unlockCodeOpt.get();
    }
}
