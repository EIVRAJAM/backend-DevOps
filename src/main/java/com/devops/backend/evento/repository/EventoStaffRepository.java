package com.devops.backend.evento.repository;

import com.devops.backend.evento.entity.EventoStaff;
import com.devops.backend.evento.enums.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventoStaffRepository extends JpaRepository<EventoStaff, Long> {

    Optional<EventoStaff> findByEvento_IdEventoAndUsuario_IdUsuario(Long eventoId, Long usuarioId);

    List<EventoStaff> findByEvento_IdEventoAndEstado(Long eventoId, Estado estado);

    @Query("SELECT es FROM EventoStaff es JOIN FETCH es.evento e WHERE es.usuario.idUsuario = :usuarioId AND es.estado = :estado")
    List<EventoStaff> findByUsuario_IdUsuarioAndEstadoWithEvento(Long usuarioId, Estado estado);

    boolean existsByEvento_IdEventoAndUsuario_IdUsuarioAndEstado(Long eventoId, Long usuarioId, Estado estado);

    boolean existsByUsuario_IdUsuarioAndEstado(Long usuarioId, Estado estado);

    @Modifying
    @Query("UPDATE EventoStaff es SET es.estado = com.devops.backend.evento.enums.Estado.INACTIVO " +
           "WHERE es.evento.idEvento = :eventoId AND es.estado = com.devops.backend.evento.enums.Estado.ACTIVO")
    int desactivarPorEvento(@Param("eventoId") Long eventoId);
}
