package com.devops.backend.evento.controller;

import com.devops.backend.evento.dto.AsignarStaffRequestDTO;
import com.devops.backend.evento.dto.MisAsignacionesStaffDTO;
import com.devops.backend.evento.dto.StaffResponseDTO;
import com.devops.backend.evento.service.EventoStaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/eventos")
@RequiredArgsConstructor
@Tag(name = "Evento Staff", description = "API para gestionar staff de eventos")
public class EventoStaffController {

    private final EventoStaffService eventoStaffService;

    @Operation(summary = "Mis asignaciones como staff", description = "Devuelve los eventos donde el usuario autenticado está asignado como staff activo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de asignaciones obtenida exitosamente")
    })
    @GetMapping("/staff/mis-asignaciones")
    public ResponseEntity<List<MisAsignacionesStaffDTO>> obtenerMisAsignaciones() {
        return ResponseEntity.ok(eventoStaffService.obtenerMisAsignaciones());
    }

    @Operation(summary = "Verificar si es staff", description = "Devuelve true si el usuario tiene al menos una asignación activa.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificación exitosa")
    })
    @GetMapping("/staff/tiene-asignaciones")
    public ResponseEntity<Boolean> tieneAsignaciones() {
        return ResponseEntity.ok(eventoStaffService.tieneAsignacionesActivas());
    }

    @Operation(summary = "Asignar staff a evento", description = "Asigna un usuario registrado como staff para controlar el ingreso a un evento.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Staff asignado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos de gestión sobre este evento"),
            @ApiResponse(responseCode = "404", description = "Evento o usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "El usuario ya está asignado")
    })
    @PostMapping("/{id}/staff")
    public ResponseEntity<StaffResponseDTO> asignarStaff(
            @PathVariable Long id,
            @Valid @RequestBody AsignarStaffRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoStaffService.asignarStaff(id, request));
    }

    @Operation(summary = "Listar staff del evento", description = "Lista todos los usuarios asignados como staff, activos e inactivos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    @GetMapping("/{id}/staff")
    public ResponseEntity<List<StaffResponseDTO>> listarStaff(@PathVariable Long id) {
        return ResponseEntity.ok(eventoStaffService.listarStaff(id));
    }

    @Operation(summary = "Activar staff", description = "Reactiva una asignación de staff inactiva.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Staff reactivado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos"),
            @ApiResponse(responseCode = "404", description = "Asignación no encontrada"),
            @ApiResponse(responseCode = "409", description = "El staff ya está activo")
    })
    @PatchMapping("/{id}/staff/{usuarioId}/activar")
    public ResponseEntity<StaffResponseDTO> activarStaff(
            @PathVariable Long id,
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(eventoStaffService.activarStaff(id, usuarioId));
    }

    @Operation(summary = "Desactivar staff", description = "Inactiva una asignación de staff, impidiendo que haga check-in.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Staff desactivado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos"),
            @ApiResponse(responseCode = "404", description = "Asignación no encontrada"),
            @ApiResponse(responseCode = "409", description = "El staff ya está inactivo")
    })
    @PatchMapping("/{id}/staff/{usuarioId}/desactivar")
    public ResponseEntity<StaffResponseDTO> desactivarStaff(
            @PathVariable Long id,
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(eventoStaffService.desactivarStaff(id, usuarioId));
    }
}
