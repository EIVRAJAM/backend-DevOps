package com.devops.backend.rol.repository;

import com.devops.backend.rol.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol,Long>, JpaSpecificationExecutor<Rol> {
    Optional<Rol> findByNombreRol(String nombreRol);
    boolean existsByNombreRolIgnoreCase(String nombreRol);

}