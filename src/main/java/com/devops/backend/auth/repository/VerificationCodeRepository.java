package com.devops.backend.auth.repository;

import com.devops.backend.auth.entity.VerificationCode;
import com.devops.backend.usuario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    /**
     * Busca un código de reseteo de contraseña por usuario y código
     */
    Optional<VerificationCode> findByUsuarioAndCodigo(Usuario usuario, String codigo);

    /**
     * Busca un código de reseteo válido (no usado, no expirado) por usuario y
     * código
     */
    @Query("SELECT p FROM VerificationCode p WHERE p.usuario = :usuario AND p.codigo = :codigo AND p.usado = false AND p.fechaExpiracion > CURRENT_TIMESTAMP")
    Optional<VerificationCode> findValidCode(Usuario usuario, String codigo);

    /**
     * Invalida todos los códigos anteriores de un usuario
     */
    @Modifying
    @Transactional
    @Query("UPDATE VerificationCode p SET p.usado = true WHERE p.usuario = :usuario AND p.usado = false")
    void invalidateAllCodesForUser(Usuario usuario);

    /**
     * Elimina todos los códigos expirados
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationCode p WHERE p.fechaExpiracion <= :now")
    void deleteExpiredCodes(LocalDateTime now);

    /**
     * Busca el código más reciente no usado de un usuario
     */
    Optional<VerificationCode> findFirstByUsuarioAndUsadoFalseOrderByCreadoEnDesc(Usuario usuario);

    /**
     * Invalida todos los códigos anteriores de un usuario de un tipo específico
     */
    @Modifying
    @Transactional
    @Query("UPDATE VerificationCode p SET p.usado = true WHERE p.usuario = :usuario AND p.tipoCodigo = :tipoCodigo AND p.usado = false")
    void invalidateAllCodesForUserByType(Usuario usuario, String tipoCodigo);

    /**
     * Busca un código válido (no usado, no expirado) por usuario, código y tipo de
     * código
     */
    @Query("SELECT p FROM VerificationCode p WHERE p.usuario = :usuario AND p.codigo = :codigo AND p.tipoCodigo = :tipoCodigo AND p.usado = false AND p.fechaExpiracion > CURRENT_TIMESTAMP")
    Optional<VerificationCode> findValidCodeByType(Usuario usuario, String codigo, String tipoCodigo);

    /**
     * Busca un código por usuario, código y tipo de código (sin validación de
     * expiración o uso)
     */
    Optional<VerificationCode> findByUsuarioAndCodigoAndTipoCodigo(Usuario usuario, String codigo, String tipoCodigo);

}
