package com.devops.backend.acceso.service;

import com.devops.backend.acceso.entity.Acceso;
import com.devops.backend.acceso.repository.AccesoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para manejar intentos fallidos de login.
 * Usa transacciones independientes (REQUIRES_NEW) para asegurar que
 * los cambios se persistan incluso si la transacción padre hace rollback.
 */
@Service
public class IntentoService {

    @Autowired
    private AccesoRepository accesoRepository;

    /**
     * Registra un intento fallido de login y bloquea la cuenta si alcanza 5
     * intentos.
     * Se ejecuta en una transacción independiente (REQUIRES_NEW).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarIntentoFallido(Long idUsuario) {
        Acceso acceso = accesoRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Acceso no encontrado"));

        // Incrementar intentos fallidos
        acceso.setIntentosFallidos(acceso.getIntentosFallidos() + 1);

        // Bloquear cuenta si alcanza 5 intentos
        if (acceso.getIntentosFallidos() >= 5) {
            acceso.setEstadoCuenta("BLOQUEADO");
        }

        accesoRepository.save(acceso);
    }
}
