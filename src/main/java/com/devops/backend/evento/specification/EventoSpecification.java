package com.devops.backend.evento.specification;

import com.devops.backend.evento.entity.Evento;
import com.devops.backend.evento.enums.Estado;
import com.devops.backend.evento.enums.EstadoEvento;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Especificaciones JPA para consultas complejas sobre Evento
 */
@Component
public class EventoSpecification {

    public Specification<Evento> filtrarPorEstadoEvento(EstadoEvento estadoEvento) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("estadoEvento"), estadoEvento);
    }

    public Specification<Evento> filtrarPorEstado(Estado estado) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("estado"), estado);
    }

    public Specification<Evento> filtrarPorUsuarioCreador(Long idUsuario) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("usuarioCreador").get("idUsuario"),
                idUsuario);
    }

    public Specification<Evento> filtrarPorFecha(LocalDate inicio, LocalDate fin) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("fechaEvento"), inicio, fin);
    }

    public Specification<Evento> filtrarPorNombre(String nombre) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("nombreEvento")),
                "%" + nombre.toLowerCase() + "%");
    }

    public Specification<Evento> filtrarPorLugar(String lugar) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("lugarEvento")),
                "%" + lugar.toLowerCase() + "%");
    }

    public Specification<Evento> filtrarPorEsDePago(Boolean esDePago) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("esDePago"), esDePago);
    }

    public Specification<Evento> filtrarConCuposDisponibles() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("capacidadDisponible"), 0);
    }

    /**
     * Combina múltiples especificaciones con AND
     */
    public Specification<Evento> filtrarEventosPublicadosActivos() {
        return Specification.where(filtrarPorEstadoEvento(EstadoEvento.PUBLICADO))
                .and(filtrarPorEstado(Estado.ACTIVO));
    }
}
