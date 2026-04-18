package com.devops.backend.funcionalidad.specification;

import com.devops.backend.funcionalidad.entity.Funcionalidad;
import com.devops.backend.usuario.entity.Usuario;
import org.springframework.data.jpa.domain.Specification;

public class FuncionalidadSpecification {

    public static Specification<Funcionalidad> porEstado(String estado) {
        return (root, query, cb) -> {
            if (estado == null || estado.isBlank()) return cb.conjunction();
            return cb.equal(
                    cb.upper(root.get("estado")),
                    estado.toUpperCase()
            );
        };
    }

    public static Specification<Funcionalidad> porPadreId(Long padreId) {
        return (root, query, cb) -> {
            if (padreId == null) return cb.conjunction();
            // Filtra por el id del padre (join con la relación ManyToOne)
            return cb.equal(root.get("padre").get("idFuncionalidad"), padreId);
        };
    }
}
