package com.devops.backend.sesion.repository;

import com.devops.backend.sesion.entity.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SesionRepository extends JpaRepository<Sesion, Long> {
    // Optional<Sesion> findByToken(String token); // Token field removed from
    // Sesion entity
}
