package com.devops.backend.auth.controller;

import com.devops.backend.auth.dto.*;
import com.devops.backend.auth.service.AuthService;
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

}