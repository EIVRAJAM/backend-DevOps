package com.devops.backend.funcionalidad.controller;

import com.devops.backend.funcionalidad.dto.FuncionalidadFilterRequest;
import com.devops.backend.funcionalidad.dto.FuncionalidadRequest;
import com.devops.backend.funcionalidad.dto.FuncionalidadResponse;
import com.devops.backend.funcionalidad.dto.FuncionalidadUpdateRequest;
import com.devops.backend.funcionalidad.service.FuncionalidadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/funcionalidad")
@Tag(name = "Functionality Management", description = "Operaciones de gestión de funcionalidades del sistema. Soporta estructura jerárquica (árbol de funcionalidades padre-hijo) para menús y permisos anidados.")
@SecurityRequirement(name = "bearerAuth")
public class FuncionalidadController {

    private final FuncionalidadService funcionalidadService;

    public FuncionalidadController(FuncionalidadService funcionalidadService) {
        this.funcionalidadService = funcionalidadService;
    }

    @PostMapping
    @Operation(summary = "Crear nueva funcionalidad", description = "Crea una nueva funcionalidad en el sistema. Puede ser una funcionalidad raíz (sin padre) o una subfuncionalidad dentro de otra existente. Útil para crear menús jerárquicos y permisos anidados.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Funcionalidad creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Validación fallida: nombre vacío, URL inválida, padre no existe"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden crear funcionalidades"),
            @ApiResponse(responseCode = "404", description = "Funcionalidad padre no encontrada (si idPadre fue especificado)")
    })
    public ResponseEntity<FuncionalidadResponse> crear(@Valid @RequestBody FuncionalidadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(funcionalidadService.save(request));
    }

    @GetMapping
    @Operation(summary = "Listar funcionalidades con filtros", description = "Obtiene lista de funcionalidades. Puede filtrar por estado (ACTIVA/INACTIVA) y por funcionalidad padre para ver árbol jerárquico. Ambos filtros son opcionales.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de funcionalidades recuperada"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado")
    })
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) @Parameter(description = "Filtro opcional por estado: ACTIVA o INACTIVA", example = "ACTIVA") String status,

            @RequestParam(required = false, name = "id_padre") @Parameter(description = "Filtro opcional por ID de funcionalidad padre. Deja vacío o usa null para solo ver raíces", example = "null") Long id_padre) {

        return ResponseEntity.ok(funcionalidadService.findAll(new FuncionalidadFilterRequest(status, id_padre)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener funcionalidad por ID", description = "Recupera información completa de una funcionalidad específica, incluyendo su ID de padre (si pertenece a un árbol).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionalidad encontrada"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "404", description = "Funcionalidad no encontrada")
    })
    public ResponseEntity<FuncionalidadResponse> findById(
            @PathVariable @Parameter(description = "ID único de la funcionalidad\", required = true, example = \"1\"") Long id) {
        return ResponseEntity.ok(funcionalidadService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar funcionalidad", description = "Actualiza nombre, URL, estado e incluso la relación padre de una funcionalidad. Solo administradores pueden actualizar funcionalidades.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionalidad actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Validación fallida: nombre vacío, nuevoPadre inválido"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden actualizar"),
            @ApiResponse(responseCode = "404", description = "Funcionalidad o padre nuevo no encontrado")
    })
    public ResponseEntity<FuncionalidadResponse> update(
            @PathVariable @Parameter(description = "ID de la funcionalidad a actualizar\", required = true, example = \"1\"") Long id,
            @Valid @RequestBody FuncionalidadUpdateRequest request) {

        return ResponseEntity.ok(funcionalidadService.update(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar funcionalidad", description = "Cambia el estado de la funcionalidad a INACTIVA. Los permisos asociados a esta funcionalidad dejarán de estar disponibles.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionalidad desactivada exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores"),
            @ApiResponse(responseCode = "404", description = "Funcionalidad no encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflicto: funcionalidad ya está inactiva")
    })
    public ResponseEntity<?> desactiveFuncionalidad(
            @PathVariable @Parameter(description = "ID de la funcionalidad a desactivar\", required = true, example = \"1\"") Long id) {
        return ResponseEntity.ok(funcionalidadService.desactive(id));
    }

    @PatchMapping("/{id}/activar")
    @Operation(summary = "Activar funcionalidad", description = "Cambia el estado de la funcionalidad a ACTIVA. Los permisos asociados a esta funcionalidad volverán a estar disponibles.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionalidad activada exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores"),
            @ApiResponse(responseCode = "404", description = "Funcionalidad no encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflicto: funcionalidad ya está activa")
    })
    public ResponseEntity<FuncionalidadResponse> activeFuncionalidad(
            @PathVariable @Parameter(description = "ID de la funcionalidad a activar\", required = true, example = \"1\"") Long id) {
        return ResponseEntity.ok(funcionalidadService.activar(id));
    }

}