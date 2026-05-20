package com.devops.backend.evento.controller;

import com.devops.backend.evento.dto.InscripcionTicketResponseDTO;
import com.devops.backend.evento.dto.MiEstadoInscripcionResponseDTO;
import com.devops.backend.evento.dto.TicketCheckoutResponseDTO;
import com.devops.backend.evento.dto.TicketResponseDTO;
import com.devops.backend.evento.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tickets", description = "Operaciones de inscripción y gestión de tickets de eventos")
@RestController
@RequestMapping("/api/v1/tickets")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    // ─────────────────────────── INSCRIPCION ────────────────────────────────

    @Operation(
            summary = "Inscribirse a un evento",
            description = "Inscribe al usuario autenticado al evento indicado. "
                    + "Para eventos gratuitos, descuenta cupo y emite ticket GRATIS de inmediato. "
                    + "Para eventos de pago, crea un ticket PENDIENTE y devuelve el clientSecret de Stripe. "
                    + "Solo se permite inscribirse a eventos PUBLICADO + ACTIVO con cupos disponibles."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inscripción realizada; ticket emitido"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado"),
            @ApiResponse(responseCode = "409", description = "Evento no disponible, cupos agotados o inscripción duplicada")
    })
    @PostMapping("/evento/{eventoId}")
    public ResponseEntity<InscripcionTicketResponseDTO> inscribirseAEvento(
            @Parameter(description = "ID del evento al que inscribirse", required = true, example = "5")
            @PathVariable Long eventoId,
            Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());
        InscripcionTicketResponseDTO response = ticketService.inscribirseAEvento(eventoId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Obtener checkout pendiente por evento",
            description = "Devuelve el checkout PENDIENTE vigente del usuario autenticado para continuar el pago. "
                    + "Si el checkout ya vencio, el backend verifica Stripe y lo resuelve antes de responder."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Checkout pendiente vigente encontrado"),
            @ApiResponse(responseCode = "401", description = "Token JWT invalido o expirado"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado o sin checkout pendiente vigente")
    })
    @GetMapping("/checkout/evento/{eventoId}")
    public ResponseEntity<TicketCheckoutResponseDTO> obtenerCheckoutPendiente(
            @Parameter(description = "ID del evento", required = true, example = "5")
            @PathVariable Long eventoId,
            Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ticketService.obtenerCheckoutPendiente(eventoId, userId));
    }

    @Operation(
            summary = "Obtener mi estado de inscripcion en un evento",
            description = "Devuelve el estado resumido del usuario autenticado frente a un evento. "
                    + "Permite al front saber si ya esta inscrito, si tiene checkout pendiente, "
                    + "si el pago esta en proceso o si puede iniciar una nueva inscripcion."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de inscripcion obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT invalido o expirado"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    @GetMapping("/evento/{eventoId}/mi-estado")
    public ResponseEntity<MiEstadoInscripcionResponseDTO> obtenerMiEstadoInscripcion(
            @Parameter(description = "ID del evento", required = true, example = "5")
            @PathVariable Long eventoId,
            Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ticketService.obtenerMiEstadoInscripcion(eventoId, userId));
    }

    // ─────────────────────────── MIS TICKETS ────────────────────────────────

    @Operation(
            summary = "Listar mis tickets",
            description = "Devuelve todos los tickets (inscripciones) del usuario autenticado, "
                    + "ordenados por fecha de compra descendente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de tickets obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado")
    })
    @GetMapping("/mis-tickets")
    public ResponseEntity<List<TicketResponseDTO>> obtenerMisTickets(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ticketService.obtenerMisTickets(userId));
    }

    // ─────────────────────────── TICKET POR ID ──────────────────────────────

    @Operation(
            summary = "Obtener ticket por ID",
            description = "Devuelve el detalle de un ticket específico. "
                    + "Solo el propietario del ticket o un administrador pueden consultarlo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket encontrado"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para ver este ticket"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> obtenerTicketPorId(
            @Parameter(description = "ID del ticket", required = true, example = "1")
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ticketService.obtenerTicketPorId(id, userId));
    }

    // ─────────────────────────── CANCELAR TICKET ────────────────────────────

    @Operation(
            summary = "Cancelar un ticket",
            description = "Cancela el ticket del usuario autenticado. "
                    + "Si el ticket estaba activo (GRATIS o PAGADO), devuelve el cupo al evento. "
                    + "No se puede cancelar un ticket ya cancelado o reembolsado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket cancelado exitosamente"),
            @ApiResponse(responseCode = "400", description = "El ticket ya fue cancelado o reembolsado"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "403", description = "Solo puedes cancelar tus propios tickets"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<TicketResponseDTO> cancelarTicket(
            @Parameter(description = "ID del ticket a cancelar", required = true, example = "1")
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ticketService.cancelarTicket(id, userId));
    }

    // ─────────────────────────── QR DEL TICKET ──────────────────────────────

    @Operation(
            summary = "Descargar QR del ticket",
            description = "Devuelve la imagen PNG del código QR del ticket. "
                    + "Solo el propietario del ticket o un administrador pueden descargarlo. "
                    + "El QR codifica únicamente el UUID del ticket, que es el valor que el check-in espera recibir."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imagen PNG del QR devuelta exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver el QR de este ticket"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado o sin código QR")
    })
    @GetMapping(value = "/{id}/qr", produces = "image/png")
    public ResponseEntity<byte[]> obtenerQrTicket(
            @Parameter(description = "ID del ticket", required = true, example = "10")
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());
        byte[] qrBytes = ticketService.generarQrTicket(id, userId);

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"ticket-" + id + "-qr.png\"")
                .body(qrBytes);
    }
}

