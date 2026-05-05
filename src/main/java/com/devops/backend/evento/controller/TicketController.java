package com.devops.backend.evento.controller;

import com.devops.backend.evento.dto.InscripcionTicketResponseDTO;
import com.devops.backend.evento.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    
    @PostMapping("/evento/{eventoId}")
    public ResponseEntity<InscripcionTicketResponseDTO> inscribirseAEvento(
            @PathVariable Long eventoId,
            Authentication authentication) {

        // Extrae el userId del claim sub del JWT (auth.getName() devuelve el sub)
        Long userId = Long.parseLong(authentication.getName());

        InscripcionTicketResponseDTO response = ticketService.inscribirseAEvento(eventoId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
