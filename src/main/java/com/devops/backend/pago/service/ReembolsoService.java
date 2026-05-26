package com.devops.backend.pago.service;

import com.devops.backend.pago.dto.*;

import java.util.List;

public interface ReembolsoService {
    SolicitudReembolsoResponse solicitarReembolso(Long idTicket, Long idUsuarioAuth, CrearSolicitudReembolsoRequest request);
    void cancelarSolicitud(Long idSolicitud, Long idUsuarioAuth);
    List<SolicitudReembolsoResponse> obtenerMisSolicitudes(Long idUsuarioAuth);
    SolicitudReembolsoResponse obtenerDetalleSolicitud(Long idSolicitud, Long idUsuarioAuth, boolean isAdmin);

    List<SolicitudReembolsoResponse> listarSolicitudesPorEvento(Long idEvento, Long idUsuarioAuth, boolean isAdmin);
    SolicitudReembolsoResponse revisarSolicitud(Long idEvento, Long idSolicitud, Long idUsuarioAuth, boolean isAdmin);
    SolicitudReembolsoResponse aprobarSolicitud(Long idEvento, Long idSolicitud, Long idUsuarioAuth, boolean isAdmin, AprobarReembolsoRequest request);
    SolicitudReembolsoResponse rechazarSolicitud(Long idEvento, Long idSolicitud, Long idUsuarioAuth, boolean isAdmin, RechazarReembolsoRequest request);

    SolicitudReembolsoResponse marcarReembolsado(Long idEvento, Long idSolicitud, Long idUsuarioAuth, boolean isAdmin);
}
