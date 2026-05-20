package com.devops.backend.pago.controller;

import com.devops.backend.pago.dto.PagoResponse;
import com.devops.backend.pago.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @GetMapping("/tickets/{ticketId}/pagos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PagoResponse>> obtenerPagosPorTicket(
            @PathVariable Long ticketId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(pagoService.obtenerPagosPorTicket(ticketId, userId, isAdmin));
    }

    @GetMapping("/pagos/mis-pagos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PagoResponse>> obtenerMisPagos(
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(pagoService.obtenerMisPagos(userId));
    }

    @GetMapping("/eventos/{eventoId}/pagos")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<List<PagoResponse>> obtenerPagosPorEvento(
            @PathVariable Long eventoId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(pagoService.obtenerPagosPorEvento(eventoId, userId, isAdmin));
    }
}
