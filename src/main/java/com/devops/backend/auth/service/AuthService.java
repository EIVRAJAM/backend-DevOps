package com.devops.backend.auth.service;

import com.devops.backend.auth.dto.*;
import com.devops.backend.exception.ApiValidationError;
import java.util.List;
import java.util.Map;

public interface AuthService {
    SignUpResponse save(SignUpRequest signupRequest);

    List<ApiValidationError> validateSignup(SignUpRequest signupRequest);

    Map<String, String> login(LoginRequest loginRequest);

    void logout(String token);
}
