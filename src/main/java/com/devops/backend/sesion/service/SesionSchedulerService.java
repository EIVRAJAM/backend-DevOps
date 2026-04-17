package com.devops.backend.sesion.service;

import com.devops.backend.sesion.repository.SesionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SesionSchedulerService {

    private final SesionRepository sesionRepository;

    @Value("${jwt.expiration-minutes}")
    private long jwtExpirationMinutes;

    /**
     * Cierra automáticamente las sesiones cuyo JWT ha expirado.
     * Se ejecuta cada 1 minuto.
     */
    @Scheduled(fixedRate = 60000) // cada 1 minuto
    @Transactional
    public void cerrarSesionesExpiradas() {
        try {
            LocalDateTime ahora = LocalDateTime.now();

            // Calcula el límite de tiempo (JWT expirados)
            LocalDateTime limite = ahora.minusMinutes(jwtExpirationMinutes);

            int actualizadas = sesionRepository.cerrarSesionesExpiradas(limite, ahora);

            if (actualizadas > 0) {
                System.out.println("[SCHEDULER] Sesiones cerradas automáticamente por expiración: " + actualizadas);
            }
        } catch (Exception e) {
            System.err.println("[SCHEDULER ERROR] Error al cerrar sesiones expiradas: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
