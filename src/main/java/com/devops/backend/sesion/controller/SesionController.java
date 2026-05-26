package com.devops.backend.sesion.controller;

import com.devops.backend.sesion.dto.SesionFilterRequest;
import com.devops.backend.sesion.dto.SesionResponseDto;
import com.devops.backend.sesion.service.SesionService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/sesiones")
@Tag(name = "Session Management", description = "Operaciones de gestión de sesiones de usuario. Permite listar, filtrar y eliminar sesiones activas del sistema.")
@SecurityRequirement(name = "bearerAuth")
public class SesionController {

        private final SesionService sesionService;

        public SesionController(SesionService sesionService) {
                this.sesionService = sesionService;
        }

        @GetMapping
        @Operation(summary = "Listar sesiones con filtros avanzados", description = "Obtiene página de sesiones con filtros opcionales por usuario, rango de fechas y estado. Todos los filtros son opcionales. Útil para auditoría y monitoreo de sesiones activas.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Página de sesiones recuperada exitosamente"),
                        @ApiResponse(responseCode = "400", description = "Validación fallida: paginación inválida (page < 0, size > 100), fechas mal formateadas"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden listar todas las sesiones")
        })
        public ResponseEntity<Page<SesionResponseDto>> findAll(
                        @RequestParam(required = false, name = "id_usuario") @Parameter(description = "Filtro opcional por ID de usuario", example = "5") Long idUsuario,

                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @Parameter(description = "Filtro opcional: fecha/hora mínima de inicio (ISO-8601)", example = "2025-07-01T00:00:00") LocalDateTime fechaInicio,

                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @Parameter(description = "Filtro opcional: fecha/hora máxima de fin (ISO-8601)", example = "2025-07-31T23:59:59") LocalDateTime fechaFin,

                        @RequestParam(required = false) @Parameter(description = "Filtro opcional por estado: true=solo activas, false=solo cerradas", example = "true") Boolean activa,

                        @RequestParam(defaultValue = "0") @Parameter(description = "Número de página (comienza en 0)", example = "0") int page,

                        @RequestParam(defaultValue = "10") @Parameter(description = "Cantidad de registros por página (máximo 100)", example = "10") int size) {

                SesionFilterRequest filter = new SesionFilterRequest(idUsuario, fechaInicio, fechaFin, activa, page,
                                size);
                return ResponseEntity.ok(sesionService.getAllSesiones(filter));
        }

        @GetMapping("/activas")
        @Operation(summary = "Listar solo sesiones activas", description = "Obtiene página de sesiones que están actualmente activas (no cerradas ni expiradas). Útil para monitorear usuarios conectados en tiempo real.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Página de sesiones activas recuperada"),
                        @ApiResponse(responseCode = "400", description = "Validación fallida: paginación inválida"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores")
        })
        public ResponseEntity<Page<SesionResponseDto>> findSesionesActivas(
                        @RequestParam(defaultValue = "0") @Parameter(description = "Número de página (comienza en 0)", example = "0") int page,

                        @RequestParam(defaultValue = "10") @Parameter(description = "Cantidad de registros por página", example = "10") int size) {

                return ResponseEntity.ok(sesionService.getSesionesActivas(page, size));
        }

        @GetMapping("/ultima")
        @Operation(summary = "Obtener última sesión del usuario actual", description = "Recupera la sesión más reciente del usuario que está haciendo la solicitud (usuario autenticado). No requiere admin. Útil para verificar el estado de sesión actual.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Última sesión recuperada exitosamente"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "404", description = "No hay sesiones registradas para este usuario")
        })
        public ResponseEntity<SesionResponseDto> findUltimaSesionDelUsuarioActual() {

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                Long idUsuario = Long.valueOf(authentication.getName());

                return sesionService.getUltimaSesionByUsuario(idUsuario)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Eliminar/Cerrar sesión", description = "Cierra una sesión específica del sistema. El token JWT asociado será invalidado. Solo administradores pueden cerrar sesiones de otros usuarios; usuarios normales solo pueden cerrar su propia sesión.")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Sesión eliminada exitosamente (sin contenido en respuesta)"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: usuario solo puede cerrar su propia sesión"),
                        @ApiResponse(responseCode = "404", description = "Sesión no encontrada")
        })
        public ResponseEntity<Void> deleteSesion(
                        @PathVariable("id") @Parameter(description = "ID de la sesión a eliminar", required = true, example = "42") Long idSesion) {
                sesionService.deleteSesion(idSesion);
                return ResponseEntity.noContent().build();
        }
}
