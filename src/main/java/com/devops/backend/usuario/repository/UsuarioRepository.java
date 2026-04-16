package com.devops.backend.usuario.repository;

import com.devops.backend.usuario.entity.*;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UsuarioRepository extends CrudRepository<Usuario, Long> {
    boolean existsByDocumento(String documento);

    Optional<Usuario> findByDocumento(String documento);
    Optional<Usuario> findByIdUsuario(Long idUsuario);


}
