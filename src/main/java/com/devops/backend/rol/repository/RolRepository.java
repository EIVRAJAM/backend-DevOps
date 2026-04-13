package com.devops.backend.rol.repository;

import com.devops.backend.rol.entity.*;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RolRepository extends CrudRepository<Rol, Long> {
    Optional<Rol> findByNombreRol(String nombreRol);
}