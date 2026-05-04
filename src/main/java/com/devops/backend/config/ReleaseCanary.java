package com.devops.backend.config;

/**
 * ARCHIVO TEMPORAL — solo para validar política de seguridad en release.
 * Será eliminado antes del merge final a main.
 * @deprecated test-only, no usar en producción
 */
@Deprecated
public class ReleaseCanary {

    // Semgrep detectará esto como ERROR (p/secrets)
    private static final String DB_PASSWORD = "supersecreto123";
    private static final String API_KEY = "sk_live_TESTKEY1234567890";
}