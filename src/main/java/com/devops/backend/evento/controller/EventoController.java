package com.devops.backend.evento.controller;

import com.devops.backend.evento.dto.ComentarioRequest;
import com.devops.backend.evento.dto.CreateEventoDTO;
import com.devops.backend.evento.dto.EventoResponseDTO;
import com.devops.backend.evento.dto.HistorialEventoDTO;
import com.devops.backend.evento.dto.TicketResponseDTO;
import com.devops.backend.evento.dto.UpdateEventoDTO;
import com.devops.backend.evento.enums.Estado;
import com.devops.backend.evento.enums.EstadoEvento;
import com.devops.backend.evento.service.EventoService;
import com.devops.backend.evento.service.TicketService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Event Management", description = "Operaciones relacionadas con la gestión de eventos del sistema")
@RestController
@RequestMapping("/api/v1/eventos")
@SecurityRequirement(name = "bearerAuth")
public class EventoController {

        @Autowired
        private EventoService eventoService;

        @Autowired
        private TicketService ticketService;

        @Operation(summary = "Crear un nuevo evento", description = "Registra un nuevo evento en el sistema en estado BORRADOR. El usuario creador se obtiene automáticamente del contexto de seguridad. El evento no será visible para otros usuarios hasta que sea publicado.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Evento creado exitosamente en estado BORRADOR"),
                        @ApiResponse(responseCode = "400", description = "Validación fallida: campos requeridos vacíos, fecha en pasado, capacidad negativa, formato de URL inválido"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "404", description = "Usuario creador no encontrado en el sistema")
        })
        @PostMapping
        public ResponseEntity<EventoResponseDTO> crearEvento(@Valid @RequestBody CreateEventoDTO createEventoDTO) {
                EventoResponseDTO eventoCreado = eventoService.crearEvento(createEventoDTO);
                return ResponseEntity.status(HttpStatus.CREATED).body(eventoCreado);
        }

        @Operation(summary = "Listar eventos disponibles (vista usuario)", description = "Devuelve únicamente eventos en estado PUBLICADO + ACTIVO, ordenados por fecha ascendente. "
                        + "Soporta filtros por nombre, lugar, rango de fechas, modalidad de pago y disponibilidad de cupos. "
                        + "Requiere autenticación JWT.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lista de eventos disponibles obtenida exitosamente"),
                        @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado")
        })
        @GetMapping("/disponibles")
        public ResponseEntity<Page<EventoResponseDTO>> listarEventosDisponibles(
                        @Parameter(description = "Número de página (comienza en 0)", example = "0") @RequestParam(defaultValue = "0") int page,

                        @Parameter(description = "Cantidad de registros por página (máximo 100)", example = "20") @RequestParam(defaultValue = "20") int size,

                        @Parameter(description = "Filtro por nombre del evento (búsqueda parcial)", required = false) @RequestParam(required = false) String nombre,

                        @Parameter(description = "Filtro por lugar del evento (búsqueda parcial)", required = false) @RequestParam(required = false) String lugar,

                        @Parameter(description = "Fecha inicio del rango de búsqueda (ISO-8601)", example = "2026-04-01", required = false) @RequestParam(required = false) LocalDate fechaInicio,

                        @Parameter(description = "Fecha fin del rango de búsqueda (ISO-8601)", example = "2026-12-31", required = false) @RequestParam(required = false) LocalDate fechaFin,

                        @Parameter(description = "Filtrar solo eventos de pago (true) o gratuitos (false)", required = false) @RequestParam(required = false) Boolean esDePago,

                        @Parameter(description = "Si es true, devuelve solo eventos con cupos disponibles", required = false) @RequestParam(required = false) Boolean conCupos) {

                Pageable pageable = org.springframework.data.domain.PageRequest.of(page, Math.min(size, 100));
                Page<EventoResponseDTO> eventos = eventoService.listarEventosDisponibles(
                                pageable, nombre, lugar, fechaInicio, fechaFin, esDePago, conCupos);
                return ResponseEntity.ok(eventos);
        }

        @Operation(summary = "Obtener detalle de un evento disponible", description = "Devuelve los detalles de un evento específico solo si se encuentra PUBLICADO y ACTIVO.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Evento obtenido exitosamente"),
                        @ApiResponse(responseCode = "403", description = "El evento no está disponible (puede estar en borrador o cancelado)"),
                        @ApiResponse(responseCode = "404", description = "Evento no encontrado")
        })
        @GetMapping("/disponibles/{id}")
        public ResponseEntity<EventoResponseDTO> obtenerEventoDisponiblePorId(
                        @Parameter(description = "ID único del evento", required = true, example = "1") @PathVariable Long id) {
                EventoResponseDTO evento = eventoService.obtenerEventoDisponiblePorId(id);
                return ResponseEntity.ok(evento);
        }

        @Operation(summary = "Mis eventos (organizador)", description = "Devuelve los eventos creados por el usuario autenticado, con paginación y filtros opcionales. "
                        + "Disponible para ROLE_ORGANIZER y ROLE_ADMIN. El filtro siempre se fuerza al usuario autenticado.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lista de eventos propios obtenida exitosamente"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado")
        })
        @GetMapping("/mis-eventos")
        public ResponseEntity<Page<EventoResponseDTO>> listarMisEventos(
                        @Parameter(description = "Número de página (comienza en 0)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Cantidad de registros por página (máximo 100)", example = "20") @RequestParam(defaultValue = "20") int size,
                        @Parameter(description = "Filtro opcional: estado del evento", required = false) @RequestParam(required = false) EstadoEvento estadoEvento,
                        @Parameter(description = "Filtro opcional: estado general (ACTIVO/INACTIVO)", required = false) @RequestParam(required = false) Estado estado,
                        @Parameter(description = "Filtro opcional: nombre del evento (búsqueda parcial)", required = false) @RequestParam(required = false) String nombre,
                        @Parameter(description = "Filtro opcional: lugar del evento (búsqueda parcial)", required = false) @RequestParam(required = false) String lugar,
                        @Parameter(description = "Filtro opcional: fecha inicio del rango", example = "2026-04-01", required = false) @RequestParam(required = false) LocalDate fechaInicio,
                        @Parameter(description = "Filtro opcional: fecha fin del rango", example = "2026-12-31", required = false) @RequestParam(required = false) LocalDate fechaFin) {

                Pageable pageable = org.springframework.data.domain.PageRequest.of(page, Math.min(size, 100));
                Page<EventoResponseDTO> eventos = eventoService.listarMisEventos(
                                pageable, estadoEvento, estado, nombre, lugar, fechaInicio, fechaFin);
                return ResponseEntity.ok(eventos);
        }

        @Operation(summary = "Listar eventos con paginación y filtros (solo ADMIN)", description = "Endpoint exclusivo para administradores: lista todos los eventos del sistema con cualquier filtro. "
                        + "ORGANIZER debe usar GET /api/v1/eventos/mis-eventos. "
                        + "ROLE_USER debe usar GET /api/v1/eventos/disponibles. "
                        + "Cualquier otro rol recibe 403.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lista de eventos obtenida exitosamente"),
                        @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo ADMIN puede usar este endpoint")
        })
        @GetMapping
        public ResponseEntity<Page<EventoResponseDTO>> listarEventos(
                        @Parameter(description = "Número de página (comienza en 0)", example = "0") @RequestParam(defaultValue = "0") int page,

                        @Parameter(description = "Cantidad de registros por página (máximo 100)", example = "20") @RequestParam(defaultValue = "20") int size,

                        @Parameter(description = "Filtro opcional: estado del evento", example = "PUBLICADO", required = false) @RequestParam(required = false) EstadoEvento estadoEvento,

                        @Parameter(description = "Filtro opcional: estado general del registro (ACTIVO/INACTIVO)", example = "ACTIVO", required = false) @RequestParam(required = false) Estado estado,

                        @Parameter(description = "Filtro opcional: nombre del evento (búsqueda parcial)", example = "DevOps", required = false) @RequestParam(required = false) String nombre,

                        @Parameter(description = "Filtro opcional: lugar del evento (búsqueda parcial)", example = "Bogotá", required = false) @RequestParam(required = false) String lugar,

                        @Parameter(description = "Filtro opcional: ID del usuario creador", example = "1", required = false) @RequestParam(required = false) Long usuarioCreador,

                        @Parameter(description = "Filtro opcional: fecha inicio en rango (formato ISO-8601)", example = "2026-04-01", required = false) @RequestParam(required = false) LocalDate fechaInicio,

                        @Parameter(description = "Filtro opcional: fecha fin en rango (formato ISO-8601)", example = "2026-12-31", required = false) @RequestParam(required = false) LocalDate fechaFin) {

                // Crear Pageable con los parámetros page y size
                Pageable pageable = org.springframework.data.domain.PageRequest.of(page, Math.min(size, 100));
                Page<EventoResponseDTO> eventos = eventoService.listarEventos(
                                pageable, estadoEvento, estado, nombre, lugar, usuarioCreador, fechaInicio, fechaFin);
                return ResponseEntity.ok(eventos);
        }

        @Operation(summary = "Obtener evento por ID (ADMIN y ORGANIZER)", description = "ADMIN: puede ver cualquier evento. "
                        + "ORGANIZER: solo puede ver el detalle de sus propios eventos (403 si el evento es de otro). "
                        + "ROLE_USER: 403 — debe usar GET /api/v1/eventos/disponibles con filtros para explorar eventos.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Evento obtenido exitosamente"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: ROLE_USER no puede usar este endpoint; ORGANIZER intentando ver evento ajeno"),
                        @ApiResponse(responseCode = "404", description = "Evento no encontrado")
        })
        @GetMapping("/{id}")
        public ResponseEntity<EventoResponseDTO> obtenerEventoPorId(
                        @Parameter(description = "ID único del evento", required = true, example = "1") @PathVariable Long id) {
                EventoResponseDTO evento = eventoService.obtenerEventoPorId(id);
                return ResponseEntity.ok(evento);
        }

        @Operation(summary = "Actualizar evento", description = "Modifica los datos de un evento existente (contenido, fecha, hora, ubicación, capacidad, etc.). Solo el creador o un admin puede actualizar. "
                        +
                        "No se pueden actualizar eventos en estado CERRADO o CANCELADO. Todos los campos en el request son opcionales.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Evento actualizado exitosamente"),
                        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (fecha en pasado, capacidad negativa, etc.)"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo el creador o admin puede actualizar este evento"),
                        @ApiResponse(responseCode = "404", description = "Evento no encontrado"),
                        @ApiResponse(responseCode = "409", description = "Conflicto: el evento está en estado CERRADO o CANCELADO y no puede ser editado")
        })
        @PutMapping("/{id}")
        public ResponseEntity<EventoResponseDTO> actualizarEvento(
                        @Parameter(description = "ID único del evento a actualizar", required = true, example = "1") @PathVariable Long id,
                        @RequestBody UpdateEventoDTO updateEventoDTO) {
                eventoService.verificarEventoEditable(id);
                EventoResponseDTO eventoActualizado = eventoService.actualizarEvento(id, updateEventoDTO);
                return ResponseEntity.ok(eventoActualizado);
        }

        @Operation(summary = "Listar eventos de un usuario", description = "Obtiene una lista paginada de eventos creados por un usuario específico. Útil para ver el historial de eventos de un usuario.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lista de eventos del usuario obtenida exitosamente"),
                        @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
        })
        @GetMapping("/usuarios/{idUsuario}/eventos")
        public ResponseEntity<Page<EventoResponseDTO>> listarEventosPorUsuario(
                        @Parameter(description = "ID único del usuario", required = true, example = "1") @PathVariable Long idUsuario,
                        @Parameter(description = "Número de página (comienza en 0)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Cantidad de registros por página (máximo 100)", example = "20") @RequestParam(defaultValue = "20") int size) {
                org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page,
                                Math.min(size, 100));
                Page<EventoResponseDTO> eventos = eventoService.listarEventosPorUsuario(idUsuario, pageable);
                return ResponseEntity.ok(eventos);
        }

        @Operation(summary = "Obtener historial del evento", description = "Obtiene el historial completo de cambios de estado de un evento, incluyendo: quién realizó el cambio, cuándo se realizó, qué estado anterior/nuevo, y comentarios asociados. Útil para auditoría.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "404", description = "Evento no encontrado")
        })
        @GetMapping("/{id}/historial")
        public ResponseEntity<List<HistorialEventoDTO>> obtenerHistorialEvento(
                        @Parameter(description = "ID único del evento", required = true, example = "1") @PathVariable Long id) {
                List<HistorialEventoDTO> historial = eventoService.obtenerHistorialEvento(id);
                return ResponseEntity.ok(historial);
        }

        @Operation(summary = "Publicar evento", description = "Publica un evento, permitiendo que otros usuarios lo vean (transición BORRADOR → PUBLICADO). Solo el creador o un administrador puede realizar esta acción.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Evento publicado exitosamente y ahora visible para otros usuarios"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo el creador o admin puede publicar este evento"),
                        @ApiResponse(responseCode = "404", description = "Evento no encontrado"),
                        @ApiResponse(responseCode = "409", description = "Conflicto: el evento debe estar en estado BORRADOR para ser publicado")
        })
        @PatchMapping("/{id}/publicar")
        public ResponseEntity<EventoResponseDTO> publicarEvento(
                        @Parameter(description = "ID único del evento a publicar", required = true, example = "1") @PathVariable Long id) {
                EventoResponseDTO eventoActualizado = eventoService.publicarEvento(id);
                return ResponseEntity.ok(eventoActualizado);
        }

        @Operation(summary = "Cancelar evento", description = "Cancela un evento sin posibilidad de reapertura (transición BORRADOR/PUBLICADO → CANCELADO). Requiere comentario con el motivo de cancelación. Solo el creador o un administrador puede realizar esta acción.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Evento cancelado exitosamente. Los cambios son irreversibles."),
                        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos: comentario vacío o excede 500 caracteres"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo el creador o admin puede cancelar este evento"),
                        @ApiResponse(responseCode = "404", description = "Evento no encontrado"),
                        @ApiResponse(responseCode = "409", description = "Conflicto: el evento debe estar en estado BORRADOR o PUBLICADO para ser cancelado")
        })
        @PatchMapping("/{id}/cancelar")
        public ResponseEntity<EventoResponseDTO> cancelarEvento(
                        @Parameter(description = "ID único del evento a cancelar", required = true, example = "1") @PathVariable Long id,
                        @Valid @RequestBody ComentarioRequest comentarioRequest) {
                EventoResponseDTO eventoActualizado = eventoService.cancelarEvento(id, comentarioRequest);
                return ResponseEntity.ok(eventoActualizado);
        }

        @Operation(summary = "Cerrar evento", description = "Cierra un evento una vez finalizado, registrando el cierre en el historial (transición PUBLICADO → CERRADO). Solo el creador o un administrador puede realizar esta acción.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Evento cerrado exitosamente y no puede ser editado"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo el creador o admin puede cerrar este evento"),
                        @ApiResponse(responseCode = "404", description = "Evento no encontrado"),
                        @ApiResponse(responseCode = "409", description = "Conflicto: el evento debe estar en estado PUBLICADO para ser cerrado")
        })
        @PatchMapping("/{id}/cerrar")
        public ResponseEntity<EventoResponseDTO> cerrarEvento(
                        @Parameter(description = "ID único del evento a cerrar", required = true, example = "1") @PathVariable Long id) {
                EventoResponseDTO eventoActualizado = eventoService.cerrarEvento(id);
                return ResponseEntity.ok(eventoActualizado);
        }

        @Operation(summary = "Activar evento", description = "Activa un evento desactivado, permitiendo que vuelva a aparecer en listados (transición estado INACTIVO → ACTIVO). Solo el creador o un administrador puede realizar esta acción.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Evento activado exitosamente y ahora visible en listados"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo el creador o admin puede activar este evento"),
                        @ApiResponse(responseCode = "404", description = "Evento no encontrado"),
                        @ApiResponse(responseCode = "409", description = "Conflicto: el evento debe estar en estado INACTIVO para ser activado")
        })
        @PatchMapping("/{id}/activar")
        public ResponseEntity<EventoResponseDTO> activarEvento(
                        @Parameter(description = "ID único del evento a activar", required = true, example = "1") @PathVariable Long id) {
                EventoResponseDTO eventoActualizado = eventoService.activarEvento(id);
                return ResponseEntity.ok(eventoActualizado);
        }

        @Operation(summary = "Desactivar evento", description = "Desactiva un evento, ocultándolo de los listados pero sin eliminarlo (transición estado ACTIVO → INACTIVO). Requiere comentario con el motivo. Solo el creador o un administrador puede realizar esta acción.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Evento desactivado exitosamente y no aparecerá en listados normales"),
                        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos: comentario vacío o excede 500 caracteres"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo el creador o admin puede desactivar este evento"),
                        @ApiResponse(responseCode = "404", description = "Evento no encontrado"),
                        @ApiResponse(responseCode = "409", description = "Conflicto: el evento debe estar en estado ACTIVO para ser desactivado")
        })
        @PatchMapping("/{id}/desactivar")
        public ResponseEntity<EventoResponseDTO> desactivarEvento(
                        @Parameter(description = "ID único del evento a desactivar", required = true, example = "1") @PathVariable Long id,
                        @Valid @RequestBody ComentarioRequest comentarioRequest) {
                EventoResponseDTO eventoActualizado = eventoService.desactivarEvento(id, comentarioRequest);
                return ResponseEntity.ok(eventoActualizado);
        }

        @Operation(summary = "Listar tickets de un evento", description = "Devuelve todos los tickets registrados para un evento específico. "
                        + "Solo el creador del evento o un administrador pueden consultar esta información.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lista de tickets obtenida exitosamente"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Sin permisos: solo el creador del evento o un admin pueden ver los tickets"),
                        @ApiResponse(responseCode = "404", description = "Evento no encontrado")
        })
        @GetMapping("/{id}/tickets")
        public ResponseEntity<List<TicketResponseDTO>> obtenerTicketsPorEvento(
                        @Parameter(description = "ID del evento", required = true, example = "5") @PathVariable Long id,
                        Authentication authentication) {
                Long userId = Long.parseLong(authentication.getName());
                return ResponseEntity.ok(ticketService.obtenerTicketsPorEvento(id, userId));
        }

}
