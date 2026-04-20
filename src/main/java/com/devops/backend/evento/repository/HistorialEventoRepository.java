package com.devops.backend.evento.repository;

import com.devops.backend.evento.entity.HistorialEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad HistorialEvento
 */
@Repository
public interface HistorialEventoRepository extends JpaRepository<HistorialEvento, Long> {

    /**
     * Busca el historial de un evento, ordenado por fecha de cambio descendente
     *
     * @param idEvento ID del evento
     * @return lista de cambios de estado ordenados por fecha descendente
     */
    List<HistorialEvento> findByEvento_IdEventoOrderByFechaCambioDesc(Long idEvento);

    /**
     * Cuenta los registros de historial para un evento específico
     *
     * @param idEvento ID del evento
     * @return cantidad de registros
     */
    long countByEvento_IdEvento(Long idEvento);
}
