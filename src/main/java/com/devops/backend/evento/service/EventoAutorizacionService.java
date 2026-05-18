package com.devops.backend.evento.service;

public interface EventoAutorizacionService {
    
    /**
     * Valida que el usuario autenticado tiene permisos de GESTIÓN sobre el evento.
     * Permitido para: ADMIN y Organizador creador del evento.
     * 
     * @param eventoId ID del evento
     * @throws org.springframework.security.access.AccessDeniedException si no tiene permiso
     */
    void validarAccesoGestion(Long eventoId);

    /**
     * Valida que el usuario autenticado tiene permisos OPERATIVOS sobre el evento.
     * Permitido para: ADMIN, Organizador creador del evento, y Staff Activo asignado.
     * 
     * @param eventoId ID del evento
     * @throws org.springframework.security.access.AccessDeniedException si no tiene permiso
     */
    void validarAccesoOperativo(Long eventoId);

    /**
     * Retorna true si el usuario autenticado es un staff activo del evento.
     * 
     * @param eventoId ID del evento
     * @return true si es staff activo, false de lo contrario
     */
    boolean esStaffActivo(Long eventoId);
}
