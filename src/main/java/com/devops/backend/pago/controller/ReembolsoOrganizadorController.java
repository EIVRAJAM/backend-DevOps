package com.devops.backend.pago.controller;

import com.devops.backend.pago.dto.AprobarReembolsoRequest;
import com.devops.backend.pago.dto.RechazarReembolsoRequest;
import com.devops.backend.pago.dto.SolicitudReembolsoResponse;
import com.devops.backend.pago.service.ReembolsoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/eventos")
@RequiredArgsConstructor
public class ReembolsoOrganizadorController {

    private final ReembolsoService reembolsoService;

    @GetMapping("/{eventoId}/reembolsos")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<List<SolicitudReembolsoResponse>> listarSolicitudes(
            @PathVariable Long eventoId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(reembolsoService.listarSolicitudesPorEvento(eventoId, userId, isAdmin));
    }

    @GetMapping("/{eventoId}/reembolsos/{solicitudId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<SolicitudReembolsoResponse> verDetalleSolicitud(
            @PathVariable Long eventoId,
            @PathVariable Long solicitudId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(reembolsoService.obtenerDetalleSolicitud(solicitudId, userId, isAdmin));
    }

    @PatchMapping("/{eventoId}/reembolsos/{solicitudId}/revisar")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<SolicitudReembolsoResponse> revisarSolicitud(
            @PathVariable Long eventoId,
            @PathVariable Long solicitudId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(reembolsoService.revisarSolicitud(eventoId, solicitudId, userId, isAdmin));
    }

    @PatchMapping("/{eventoId}/reembolsos/{solicitudId}/aprobar")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<SolicitudReembolsoResponse> aprobarSolicitud(
            @PathVariable Long eventoId,
            @PathVariable Long solicitudId,
            @Valid @RequestBody AprobarReembolsoRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(reembolsoService.aprobarSolicitud(eventoId, solicitudId, userId, isAdmin, request));
    }

    @PatchMapping("/{eventoId}/reembolsos/{solicitudId}/rechazar")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<SolicitudReembolsoResponse> rechazarSolicitud(
            @PathVariable Long eventoId,
            @PathVariable Long solicitudId,
            @Valid @RequestBody RechazarReembolsoRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(reembolsoService.rechazarSolicitud(eventoId, solicitudId, userId, isAdmin, request));
    }

    @PatchMapping("/{eventoId}/reembolsos/{solicitudId}/marcar-reembolsado")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<SolicitudReembolsoResponse> marcarReembolsado(
            @PathVariable Long eventoId,
            @PathVariable Long solicitudId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(reembolsoService.marcarReembolsado(eventoId, solicitudId, userId, isAdmin));
    }
}