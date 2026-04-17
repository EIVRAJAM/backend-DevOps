package com.devops.backend.funcionalidad.repository;

import com.devops.backend.funcionalidad.entity.Funcionalidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface FuncionalidadRepository extends JpaRepository<Funcionalidad,Long> {

    boolean existsByNombreFuncionalidad(String nombreFuncionalidad);


}
