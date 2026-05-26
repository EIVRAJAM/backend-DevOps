package com.devops.backend.pago.controller;

import com.devops.backend.pago.dto.CrearSolicitudReembolsoRequest;
import com.devops.backend.pago.dto.SolicitudReembolsoResponse;
import com.devops.backend.pago.service.ReembolsoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReembolsoUsuarioController {

    private final ReembolsoService reembolsoService;

    @PostMapping(value = "/tickets/{ticketId}/reembolso", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SolicitudReembolsoResponse> solicitarReembolso(
            @PathVariable Long ticketId,
            @ModelAttribute CrearSolicitudReembolsoRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("[CONTROLLER] Solicitud de reembolso recibida. ticketId={}, userId={}, motivoLength={}",
                ticketId, userId,
                request.getMotivoSolicitud() != null ? request.getMotivoSolicitud().length() : 0);
        if (request.getCertificadoCuenta() != null && !request.getCertificadoCuenta().isEmpty()) {
            log.info("[CONTROLLER] Certificado adjunto: nombre={}, sizeBytes={}, contentType={}",
                    request.getCertificadoCuenta().getOriginalFilename(),
                    request.getCertificadoCuenta().getSize(),
                    request.getCertificadoCuenta().getContentType());
        }
        if (request.getDocumentoAdicional() != null && !request.getDocumentoAdicional().isEmpty()) {
            log.info("[CONTROLLER] Documento adicional adjunto: nombre={}, sizeBytes={}, contentType={}",
                    request.getDocumentoAdicional().getOriginalFilename(),
                    request.getDocumentoAdicional().getSize(),
                    request.getDocumentoAdicional().getContentType());
        }
        SolicitudReembolsoResponse response = reembolsoService.solicitarReembolso(ticketId, userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/reembolsos/mis-solicitudes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SolicitudReembolsoResponse>> obtenerMisSolicitudes(
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(reembolsoService.obtenerMisSolicitudes(userId));
    }

    @GetMapping("/reembolsos/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SolicitudReembolsoResponse> obtenerDetalleSolicitud(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(reembolsoService.obtenerDetalleSolicitud(id, userId, isAdmin));
    }

    @PatchMapping("/reembolsos/{id}/cancelar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelarSolicitud(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        reembolsoService.cancelarSolicitud(id, userId);
        return ResponseEntity.noContent().build();
    }
}
