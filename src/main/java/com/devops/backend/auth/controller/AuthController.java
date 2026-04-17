package com.devops.backend.auth.controller;

import com.devops.backend.auth.dto.*;
import com.devops.backend.auth.service.AuthService;
import com.devops.backend.auth.service.PasswordResetService;
import com.devops.backend.auth.service.AccountUnlockService;
import com.devops.backend.exception.ApiValidationError;
import com.devops.backend.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private AccountUnlockService accountUnlockService;

    @Autowired
    private Validator validator;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest signUpRequest) {
        System.out.println("aqui andamos");

        List<ApiValidationError> errors = new ArrayList<>();

        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(signUpRequest);
        for (ConstraintViolation<SignUpRequest> violation : violations) {
            String field = violation.getPropertyPath().toString();
            errors.add(new ApiValidationError(field, violation.getMessage()));
        }

        errors.addAll(authService.validateSignup(signUpRequest));

        if (!errors.isEmpty()) {
            throw new ValidationException("Errores en el registro", errors);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(authService.save(signUpRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest) {
        try {
            Map<String, String> response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token no proporcionado"));
        }

        String token = header.replace("Bearer ", "").trim();
        authService.logout(token);

        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }

    /**
     * Endpoint para solicitar recuperación de contraseña
     * POST /auth/forgot-password
     *
     * @param request ForgotPasswordRequest con el email
     * @return Mensaje de confirmación
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        try {
            passwordResetService.requestPasswordReset(request);
            return ResponseEntity.ok(new PasswordResetResponse(
                    "Si el correo está registrado, recibirás un código de verificación",
                    true));
        } catch (com.devops.backend.exception.BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PasswordResetResponse(
                            e.getMessage(),
                            false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PasswordResetResponse(
                            "Error al procesar la solicitud",
                            false));
        }
    }

    /**
     * Endpoint para resetear la contraseña
     * POST /auth/reset-password
     *
     * @param request ResetPasswordRequest con email, código y nueva contraseña
     * @return Mensaje de confirmación
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request);
            return ResponseEntity.ok(new PasswordResetResponse(
                    "Contraseña actualizada exitosamente",
                    true));
        } catch (com.devops.backend.exception.BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PasswordResetResponse(
                            e.getMessage(),
                            false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PasswordResetResponse(
                            "Error al resetear la contraseña",
                            false));
        }
    }

    /**
     * Endpoint para solicitar desbloqueo de cuenta
     * POST /auth/request-account-unlock
     *
     * @param request ForgotPasswordRequest con el email
     * @return Mensaje de confirmación
     */
    @PostMapping("/request-account-unlock")
    public ResponseEntity<?> requestAccountUnlock(@RequestBody @Valid ForgotPasswordRequest request) {
        try {
            accountUnlockService.requestAccountUnlock(request.email());
            return ResponseEntity.ok(new PasswordResetResponse(
                    "Si la cuenta está bloqueada, recibirás un código de verificación en tu correo",
                    true));
        } catch (com.devops.backend.exception.BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PasswordResetResponse(
                            e.getMessage(),
                            false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PasswordResetResponse(
                            "Error al procesar la solicitud de desbloqueo",
                            false));
        }
    }

    /**
     * Endpoint para desbloquear la cuenta
     * POST /auth/unlock-account
     *
     * @param request UnlockAccountRequest con email y código de verificación
     * @return Mensaje de confirmación
     */
    @PostMapping("/unlock-account")
    public ResponseEntity<?> unlockAccount(@RequestBody @Valid UnlockAccountRequest request) {
        try {
            accountUnlockService.unlockAccount(request);
            return ResponseEntity.ok(new PasswordResetResponse(
                    "Cuenta desbloqueada exitosamente",
                    true));
        } catch (com.devops.backend.exception.BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PasswordResetResponse(
                            e.getMessage(),
                            false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PasswordResetResponse(
                            "Error al desbloquear la cuenta",
                            false));
        }
    }

}