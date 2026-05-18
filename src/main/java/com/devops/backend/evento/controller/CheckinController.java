package com.devops.backend.evento.controller;

import com.devops.backend.evento.dto.CheckinRequestDTO;
import com.devops.backend.evento.dto.CheckinResponseDTO;
import com.devops.backend.evento.dto.CheckinResumenDTO;
import com.devops.backend.evento.service.CheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/eventos")
@RequiredArgsConstructor
@Tag(name = "Check-in Evento", description = "API para gestión del ingreso a eventos")
public class CheckinController {

    private final CheckinService checkinService;

    @Operation(summary = "Realizar check-in", description = "Valida el código QR y marca el ticket como ingresado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Check-in exitoso"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos operativos (no eres staff)"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado o código QR inválido"),
            @ApiResponse(responseCode = "409", description = "Check-in ya realizado o ticket inválido")
    })
    @PostMapping("/{id}/check-in")
    public ResponseEntity<CheckinResponseDTO> realizarCheckin(
            @PathVariable Long id,
            @Valid @RequestBody CheckinRequestDTO request) {
        return ResponseEntity.ok(checkinService.realizarCheckin(id, request));
    }

    @Operation(summary = "Resumen de check-in", description = "Devuelve estadísticas de asistencia del evento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen obtenido exitosamente"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos operativos (no eres staff)"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    @GetMapping("/{id}/check-in/resumen")
    public ResponseEntity<CheckinResumenDTO> obtenerResumen(@PathVariable Long id) {
        return ResponseEntity.ok(checkinService.obtenerResumen(id));
    }
}
