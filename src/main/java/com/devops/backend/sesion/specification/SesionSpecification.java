package com.devops.backend.sesion.specification;

import com.devops.backend.sesion.entity.Sesion;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class SesionSpecification {

    public static Specification<Sesion> porIdUsuario(Long idUsuario) {
        return (root, query, criteriaBuilder) -> {
            if (idUsuario == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.join("usuario").get("idUsuario"), idUsuario);
        };
    }

    public static Specification<Sesion> porFechaInicio(LocalDate fechaInicio) {
        return (root, query, criteriaBuilder) -> {
            if (fechaInicio == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("fechaSesion"), fechaInicio);
        };
    }

    public static Specification<Sesion> porFechaFin(LocalDate fechaFin) {
        return (root, query, criteriaBuilder) -> {
            if (fechaFin == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("fechaSesion"), fechaFin);
        };
    }
}
