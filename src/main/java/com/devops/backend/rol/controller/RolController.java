package com.devops.backend.rol.controller;

import com.devops.backend.funcionalidad.dto.FuncionalidadResponse;
import com.devops.backend.rol.dto.AsignarFuncionalidadesRequest;
import com.devops.backend.rol.dto.RolFilterRequest;
import com.devops.backend.rol.dto.RolRequest;
import com.devops.backend.rol.dto.RolResponse;
import com.devops.backend.rol.dto.RolUpdateDto;
import com.devops.backend.rol.service.RolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Role Management", description = "Operaciones relacionadas con roles y sus funcionalidades")
@RestController
@RequestMapping("/v1/roles")
@SecurityRequirement(name = "bearerAuth")
public class RolController {

    private RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @PostMapping()
    @Operation(
            summary = "Crear nuevo rol",
            description = "Crea un nuevo rol en el sistema. Solo administradores pueden crear roles. Se valida que el nombre del rol no exista previamente.",
            tags = {"Role Management"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rol creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Validación fallida: nombre vacío o formato inválido"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden crear roles"),
            @ApiResponse(responseCode = "409", description = "Conflicto: nombre del rol ya existe en el sistema")
    })
    public ResponseEntity<RolResponse> save(@Valid @RequestBody RolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rolService.save(request));
    }

    @GetMapping("/all")
    @Operation(
            summary = "Obtener todos los roles sin paginación",
            description = "Recupera la lista completa de roles activos e inactivos del sistema, sin filtros ni paginación. Útil para poblar selectores y formularios.",
            tags = {"Role Management"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista completa de roles recuperada exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado")
    })
    public ResponseEntity<List<RolResponse>> findAll() {
        return ResponseEntity.ok(rolService.findAll());
    }

    @GetMapping
    @Operation(
            summary = "Listar roles con paginación y filtros",
            description = "Obtiene lista paginada de roles. Soporta filtros por ID, nombre y estado. Útil para administración y búsqueda de roles específicos.",
            tags = {"Role Management"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de roles recuperada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos (page < 0 o size > 100)"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado")
    })
    public ResponseEntity<Page<RolResponse>> findAllFilter(
            @RequestParam(required = false)
            @Parameter(description = "Filtro opcional: ID único del rol", example = "1")
            Long idRol,

            @RequestParam(required = false)
            @Parameter(description = "Filtro opcional: nombre del rol (búsqueda parcial)", example = "ADMIN")
            String nombreRol,

            @RequestParam(required = false)
            @Parameter(description = "Filtro opcional: estado del rol (ACTIVO o INACTIVO)", example = "ACTIVO")
            String estado,

            @RequestParam(defaultValue = "0")
            @Parameter(description = "Número de página (comienza en 0)", example = "0")
            int page,

            @RequestParam(defaultValue = "10")
            @Parameter(description = "Cantidad de registros por página (máximo 100)", example = "10")
            int size
    ) {
        return ResponseEntity.ok(rolService.findAllFilter(new RolFilterRequest(idRol, nombreRol, estado, page, size)));
    }

    @GetMapping("/id/{id}")
    @Operation(
            summary = "Obtener rol por ID",
            description = "Recupera la información completa de un rol específico usando su ID único.",
            tags = {"Role Management"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol encontrado exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado en el sistema")
    })
    public ResponseEntity<RolResponse> findById(
            @PathVariable
            @Parameter(description = "ID único del rol", required = true, example = "1")
            Long id
    ) {
        return ResponseEntity.ok(rolService.findById(id));
    }

    @GetMapping("/nombre({nombre}")
    @Operation(
            summary = "Obtener rol por nombre",
            description = "Recupera la información de un rol buscando por su nombre exacto.",
            tags = {"Role Management"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol encontrado exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "404", description = "Rol con el nombre especificado no encontrado")
    })
    public ResponseEntity<RolResponse> findByName(
            @PathVariable
            @Parameter(description = "Nombre exacto del rol", required = true, example = "ROLE_ADMIN")
            String nombre
    ) {
        return ResponseEntity.ok(rolService.findByName(nombre));
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(
            summary = "Desactivar rol",
            description = "Cambia el estado del rol a INACTIVO. Solo administradores pueden realizar esta operación. Los usuarios con este rol perderán los permisos asociados.",
            tags = {"Role Management"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol desactivado exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden desactivar roles"),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflicto: rol ya está inactivo")
    })
    public ResponseEntity<RolResponse> desactivar(
            @PathVariable
            @Parameter(description = "ID único del rol a desactivar", required = true, example = "1")
            Long id
    ) {
        return ResponseEntity.ok(rolService.desactivarRol(id));
    }

    @PatchMapping("/{id}/activar")
    @Operation(
            summary = "Activar rol",
            description = "Cambia el estado del rol a ACTIVO. Solo administradores pueden realizar esta operación. Útil para rehabilitar roles previamente desactivados.",
            tags = {"Role Management"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol activado exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden activar roles"),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflicto: rol ya está activo")
    })
    public ResponseEntity<RolResponse> activar(
            @PathVariable
            @Parameter(description = "ID único del rol a activar", required = true, example = "1")
            Long id
    ) {
        return ResponseEntity.ok(rolService.activarRol(id));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar rol",
            description = "Actualiza la información de un rol existente. Solo administradores pueden modificar roles. Se valida que el nuevo nombre no esté en uso por otro rol.",
            tags = {"Role Management"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Validación fallida: campos requeridos vacíos o formato inválido"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden modificar roles"),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflicto: nombre del rol ya existe en el sistema")
    })
    public ResponseEntity<RolResponse> updateRol(
            @PathVariable
            @Parameter(description = "ID único del rol a actualizar", required = true, example = "1")
            Long id,

            @Valid @RequestBody RolUpdateDto request
    ) {
        return ResponseEntity.ok(rolService.updateRol(id, request));
    }

    @PostMapping("/{id}/funcionalidades")
    @Operation(
            summary = "Asignar funcionalidades a un rol",
            description = "Reemplaza o asigna el conjunto de funcionalidades (permisos) asociadas a un rol. Los usuarios con este rol obtendrán acceso a las funcionalidades asignadas.",
            tags = {"Role Management"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionalidades asignadas exitosamente al rol"),
            @ApiResponse(responseCode = "400", description = "Validación fallida: lista de funcionalidades inválida o vacía"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden asignar funcionalidades"),
            @ApiResponse(responseCode = "404", description = "Rol o alguna funcionalidad especificada no encontrada")
    })
    public ResponseEntity<RolResponse> asignarFuncionalidades(
            @PathVariable
            @Parameter(description = "ID único del rol al que se asignan funcionalidades", required = true, example = "1")
            Long id,

            @Valid @RequestBody AsignarFuncionalidadesRequest request
    ) {
        return ResponseEntity.ok(rolService.asignarFuncionalidades(id, request));
    }

    @GetMapping("/{id}/funcionalidades")
    @Operation(
            summary = "Listar funcionalidades de un rol",
            description = "Recupera todas las funcionalidades (permisos) asignadas a un rol específico. Útil para auditar y gestionar los permisos de cada rol.",
            tags = {"Role Management"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionalidades del rol recuperadas exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado")
    })
    public ResponseEntity<List<FuncionalidadResponse>> findFuncionalidadesByRol(
            @PathVariable
            @Parameter(description = "ID único del rol a consultar", required = true, example = "1")
            Long id
    ) {
        return ResponseEntity.ok(rolService.findFuncionalidadesByRol(id));
    }

    @DeleteMapping("/{idRol}/funcionalidades/{idFuncionalidad}")
    @Operation(
            summary = "Eliminar funcionalidad de un rol",
            description = "Desvincula una funcionalidad específica de un rol. Los usuarios con este rol perderán el acceso a dicha funcionalidad.",
            tags = {"Role Management"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionalidad eliminada del rol exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden modificar funcionalidades de roles"),
            @ApiResponse(responseCode = "404", description = "Rol o funcionalidad no encontrada, o la funcionalidad no está asignada al rol")
    })
    public ResponseEntity<RolResponse> eliminarFuncionalidad(
            @PathVariable
            @Parameter(description = "ID único del rol", required = true, example = "1")
            Long idRol,

            @PathVariable
            @Parameter(description = "ID único de la funcionalidad a desvincular", required = true, example = "5")
            Long idFuncionalidad
    ) {
        return ResponseEntity.ok(rolService.eliminarFuncionalidad(idRol, idFuncionalidad));
    }
}