package com.devops.backend.usuario.repository;

import com.devops.backend.usuario.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    boolean existsByDocumento(String documento);
    Optional<Usuario> findByDocumento(String documento);
    Optional<Usuario> findByIdUsuario(Long idUsuario);

    // Verifica duplicado excluyendo el propio id
    boolean existsByDocumentoAndIdUsuarioNot(String documento, Long idUsuario);
    boolean existsByTelefonoAndIdUsuarioNot(String telefono, Long idUsuario);

}
