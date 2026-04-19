package com.devops.backend.funcionalidad.repository;

import com.devops.backend.funcionalidad.entity.Funcionalidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FuncionalidadRepository extends JpaRepository<Funcionalidad,Long>, JpaSpecificationExecutor<Funcionalidad> {

    boolean existsByNombreFuncionalidad(String nombreFuncionalidad);
    boolean existsByPadreIdFuncionalidadAndEstado(Long idPadre, String estado);
}
