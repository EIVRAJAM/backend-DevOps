package com.devops.backend.sesion.repository;

import com.devops.backend.sesion.entity.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SesionRepository extends JpaRepository<Sesion, Long>, JpaSpecificationExecutor<Sesion> {
    // Optional<Sesion> findByToken(String token); // Token field removed from
    // Sesion entity
}
