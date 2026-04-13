package com.devops.backend.acceso.repository;

import com.devops.backend.acceso.entity.Acceso;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AccesoRepository extends CrudRepository<Acceso, Long> {
    boolean existsByCorreoAcceso(String correoAcceso);
    boolean existsByUsername(String username);

    Optional<Acceso> findByUsername(String username);
    Optional<Acceso> findByCorreoAcceso(String correoAcceso);
}
