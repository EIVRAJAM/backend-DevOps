package com.devops.backend.sesion.specification;

import com.devops.backend.sesion.entity.Sesion;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class SesionSpecification {

    public static Specification<Sesion> porIdUsuario(Long idUsuario) {
        return (root, query, criteriaBuilder) -> {
            if (idUsuario == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.join("usuario").get("idUsuario"), idUsuario);
        };
    }

    public static Specification<Sesion> porFechaInicio(LocalDateTime fechaInicio) {
        return (root, query, criteriaBuilder) -> {
            if (fechaInicio == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("fechaInicio"), fechaInicio);
        };
    }

    public static Specification<Sesion> porFechaFin(LocalDateTime fechaFin) {
        return (root, query, criteriaBuilder) -> {
            if (fechaFin == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.and(
                    criteriaBuilder.isNotNull(root.get("fechaFin")),
                    criteriaBuilder.lessThanOrEqualTo(root.get("fechaFin"), fechaFin)
            );
        };
    }

    public static Specification<Sesion> porActiva(Boolean activa) {
        return (root, query, criteriaBuilder) -> {
            if (activa == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("activa"), activa);
        };
    }

    public static Specification<Sesion> sesionesActivas() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.isTrue(root.get("activa")),
                        criteriaBuilder.isNull(root.get("fechaFin"))
                );
    }
}
