package com.devops.backend.rol.specification;

import com.devops.backend.rol.entity.Rol;
import com.devops.backend.rol.entity.dto.RolFilterRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class RolSpecification{

    public static Specification<Rol> withFilters(RolFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.idRol() != null) {
                predicates.add(cb.equal(root.get("idRol"), filter.idRol()));
            }

            if (filter.nombreRol() != null && !filter.nombreRol().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("nombreRol")),
                        "%" + filter.nombreRol().toLowerCase() + "%"
                ));
            }

            if (filter.estado() != null && !filter.estado().isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("estado")),
                        filter.estado().toLowerCase()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}