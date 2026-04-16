package com.devops.backend.auth.repository;

import com.devops.backend.auth.entity.PasswordResetCode;
import com.devops.backend.usuario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {

    /**
     * Busca un código de reseteo de contraseña por usuario y código
     */
    Optional<PasswordResetCode> findByUsuarioAndCodigo(Usuario usuario, String codigo);

    /**
     * Busca un código de reseteo válido (no usado, no expirado) por usuario y
     * código
     */
    @Query("SELECT p FROM PasswordResetCode p WHERE p.usuario = :usuario AND p.codigo = :codigo AND p.usado = false AND p.fechaExpiracion > CURRENT_TIMESTAMP")
    Optional<PasswordResetCode> findValidCode(Usuario usuario, String codigo);

    /**
     * Invalida todos los códigos anteriores de un usuario
     */
    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetCode p SET p.usado = true WHERE p.usuario = :usuario AND p.usado = false")
    void invalidateAllCodesForUser(Usuario usuario);

    /**
     * Elimina todos los códigos expirados
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetCode p WHERE p.fechaExpiracion <= :now")
    void deleteExpiredCodes(LocalDateTime now);

    /**
     * Busca el código más reciente no usado de un usuario
     */
    Optional<PasswordResetCode> findFirstByUsuarioAndUsadoFalseOrderByCreadoEnDesc(Usuario usuario);
}
