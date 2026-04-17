package com.devops.backend.usuario.specification;

import com.devops.backend.usuario.entity.Usuario;
import org.springframework.data.jpa.domain.Specification;

public class UsuarioSpecification {

    public static Specification<Usuario> porDocumento(String doc) {
        return (root, query, cb) -> {
            if (doc == null || doc.isEmpty()) return cb.conjunction(); //No aplica el filtro
            return cb.equal(root.get("documento"), doc);
        };
    }

    public static Specification<Usuario> porNombre(String nombres) {
        return (root, query, cb) -> {
            if (nombres == null || nombres.isEmpty()) return cb.conjunction();
            return cb.like(cb.lower(root.get("nombres")), "%" + nombres.toLowerCase() + "%");
        };
    }

    public static Specification<Usuario> porApellido(String apellidos) {
        return (root, query, cb) -> {
            if (apellidos == null || apellidos.isEmpty()) return cb.conjunction();
            return cb.like(cb.lower(root.get("apellidos")), "%" + apellidos.toLowerCase() + "%");
        };
    }

    public static Specification<Usuario> porRol(String nombreRol) {
        return (root, query, cb) -> {
            if (nombreRol == null || nombreRol.isEmpty()) return cb.conjunction();
            // Join hacia la entidad Rol
            return cb.equal(root.join("rol").get("nombreRol"), nombreRol);
        };
    }


}
