package com.devops.backend.usuario.controller;

import com.devops.backend.funcionalidad.service.FuncionalidadService;
import com.devops.backend.usuario.dto.*;
import com.devops.backend.usuario.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "User Management", description = "Operaciones de gestión de usuarios del sistema. Incluye creación, listado, búsqueda, actualización y cambios de estado (activar/desactivar/bloquear).")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

        private final UsuarioService uService;
        public UsuarioController(UsuarioService uService, FuncionalidadService funcionalidadService) {
                this.uService = uService;
        }

        @PreAuthorize("hasRole('ADMIN')") //Verificamos si es Admin
        @PostMapping()
        @Operation(summary = "Crear nuevo usuario - ADMIN", description = "Crea un nuevo usuario en el sistema. Se valida que el documento sea únicos. Los usuarios creados por esta ruta requieren que se cree su acceso luego en el módulo de Acceso.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
                        @ApiResponse(responseCode = "400", description = "Validación fallida: campos requeridos vacíos, formato inválido, documento o username duplicados"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden crear usuarios"),
                        @ApiResponse(responseCode = "409", description = "Conflicto: documento o username ya existen")
        })
        public ResponseEntity<SignUpResponseUsuario> add(@Valid @RequestBody SignUpUserRequest signUpRequest) {
                return ResponseEntity.status(HttpStatus.CREATED).body(uService.saveUser(signUpRequest));
        }


        @PreAuthorize("hasRole('ADMIN')") //Verificamos si es Admin
        @GetMapping()
        @Operation(summary = "Listar usuarios con paginación y filtros - ADMIN", description = "Obtiene lista paginada de usuarios. Soporta filtros por documento, nombres, apellidos y rol. Todos los parámetros de filtro son opcionales.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Página de usuarios recuperada exitosamente"),
                        @ApiResponse(responseCode = "400", description = "Validación fallida: parámetros de paginación inválidos (page < 0, size > 100)"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado")
        })
        public ResponseEntity<Page<UserListResponse>> findAll(
                        @RequestParam(required = false) @Parameter(description = "Filtro opcional: número de documento (búsqueda exacta)", example = "1234567890") String documento,

                        @RequestParam(required = false) @Parameter(description = "Filtro opcional: nombres del usuario (búsqueda parcial)", example = "Juan") String nombres,

                        @RequestParam(required = false) @Parameter(description = "Filtro opcional: apellidos del usuario (búsqueda parcial)", example = "Pérez") String apellidos,

                        @RequestParam(required = false) @Parameter(description = "Filtro opcional: nombre del rol", example = "ADMIN") String nombreRol,

                        @RequestParam(defaultValue = "0") @Parameter(description = "Número de página (comienza en 0)", example = "0") int page,

                        @RequestParam(defaultValue = "10") @Parameter(description = "Cantidad de registros por página (máximo 100)", example = "10") int size) {

                return ResponseEntity.ok(
                                uService.getAllUsers(new UsuarioFilterRequest(documento, nombres, apellidos, nombreRol,
                                                page, size)));
        }


        @PreAuthorize("hasRole('ADMIN')") //Verificamos si es Admin
        @GetMapping("/{id}")
        @Operation(summary = "Obtener usuario por ID - ADMIN", description = "Recupera información completa de un usuario específico por su ID. Solo administradores pueden ver datos de otros usuario.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Usuario encontrado exitosamente"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: usuario solo puede ver su propio perfil"),
                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
        })
        public ResponseEntity<UserResponseAdmin> findById(
                        @PathVariable @Parameter(description = "ID único del usuario", required = true, example = "\"123\"") Long id) {

                return ResponseEntity.ok(uService.findById(id));
        }

        @GetMapping("/id")
        @Operation(summary = "Obtener usuario actual", description = "Recupera información completa del usuario de la sesión activa .")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Usuario encontrado exitosamente"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: usuario solo puede ver su propio perfil"),
                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
        })
        public ResponseEntity<UserListResponse> findByIdUser() {

                Authentication aut = SecurityContextHolder.getContext().getAuthentication();
                Long idUsuario = Long.parseLong(aut.getName());
                return ResponseEntity.ok(uService.findByIdUser(idUsuario));
        }

        @PreAuthorize("hasRole('ADMIN')") //Verificamos si es Admin
        @GetMapping("/document/{document}")
        @Operation(summary = "Obtener usuario por documento - ADMIN   ", description = "Recupera información de un usuario usando su número de documento de identificación (búsqueda exacta).")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Usuario encontrado exitosamente"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado para el documento especificado")
        })
        public ResponseEntity<UserResponseAdmin> findByDocument(
                        @PathVariable @Parameter(description = "Número de documento de identificación", required = true, example = "1234567890") String document) {
                return ResponseEntity.ok(uService.findByDocumento(document));
        }

        @PutMapping("")
        @Operation(summary = "Actualizar información de usuario", description = "Actualiza datos personales de un usuario (nombres, apellidos, teléfono, género, fecha nacimiento,). Usuarios normales solo pueden actualizar sus propios datos.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
                        @ApiResponse(responseCode = "400", description = "Validación fallida: datos incompletos o formato inválido"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: usuario solo puede actualizar su propio perfil"),
                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                        @ApiResponse(responseCode = "409", description = "Conflicto: documento ya registrado a otro usuario")
        })
        public ResponseEntity<UserListResponse> update(@Valid @RequestBody UpdateUsuarioRequest request) {

                Authentication aut = SecurityContextHolder.getContext().getAuthentication();
                Long idUsuario = Long.parseLong(aut.getName());
                return ResponseEntity.ok(uService.updateUser(idUsuario, request));
        }

        @PreAuthorize("hasRole('ADMIN')") //Verificamos si es Admin
        @PutMapping("/{id}/admin")
        @Operation(summary = "Actualizar usuario - ADMIN", description = "Actualiza todos los atributos de un usuario incluyendo su estado y rol. Solo administradores pueden usar este endpoint. Permite cambiar el estado del usuario (ACTIVO, INACTIVO, BLOQUEADO).")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
                        @ApiResponse(responseCode = "400", description = "Validación fallida: datos incompletos, formato inválido, o estado no válido"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden usar este endpoint"),
                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                        @ApiResponse(responseCode = "409", description = "Conflicto: documento o teléfono ya registrado a otro usuario")
        })
        public ResponseEntity<UserUpdateAdminResponse> updateUserAdmin(
                        @PathVariable @Parameter(description = "ID del usuario a actualizar", required = true, example = "123") Long id,
                        @Valid @RequestBody UserUpdateAdminDto request) {

                return ResponseEntity.ok(uService.updateUserAdmin(id, request));
        }

        @PreAuthorize("hasRole('ADMIN')") //Verificamos si es Admin
        @PatchMapping("/{id}/activar")
        @Operation(summary = "Activar usuario - ADMIN", description = "Cambia el estado del usuario a ACTIVO. Solo administradores pueden realizar esta operación. Usuarios activos pueden iniciar sesión y acceder al sistema.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Usuario activado exitosamente"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden activar usuarios"),
                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                        @ApiResponse(responseCode = "409", description = "Conflicto: usuario ya está activo")
        })
        public ResponseEntity<UserResponseAdmin> activateUser(
                        @PathVariable @Parameter(description = "ID del usuario a activar", required = true, example = "123") Long id) {

                return ResponseEntity.ok(uService.activar(id));
        }

        @PreAuthorize("hasRole('ADMIN')") //Verificamos si es Admin
        @PatchMapping("/{id}/desactivar")
        @Operation(summary = "Desactivar usuario - ADMIN", description = "Cambia el estado del usuario a INACTIVO. Solo administradores pueden realizar esta operación. Usuarios inactivos no pueden iniciar sesión.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Usuario desactivado exitosamente"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden desactivar usuarios"),
                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                        @ApiResponse(responseCode = "409", description = "Conflicto: usuario ya está inactivo")
        })
        public ResponseEntity<UserResponseAdmin> deactivateUser(
                        @PathVariable @Parameter(description = "ID del usuario a desactivar", required = true, example = "123") Long id) {

                return ResponseEntity.ok(uService.desactivar(id));
        }

        @PreAuthorize("hasRole('ADMIN')") //Verificamos si es Admin
        @PatchMapping("/{id}/bloquear")
        @Operation(summary = "Bloquear usuario - ADMIN", description = "Cambia el estado del usuario a BLOQUEADO. Solo administradores pueden realizar esta operación. Usuarios bloqueados no pueden iniciar sesión hasta ser desbloqueados.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Usuario bloqueado exitosamente"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden bloquear usuarios"),
                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                        @ApiResponse(responseCode = "409", description = "Conflicto: usuario ya está bloqueado")
        })
        public ResponseEntity<UserResponseAdmin> blockUser(
                        @PathVariable @Parameter(description = "ID del usuario a bloquear", required = true, example = "123") Long id) {

                return ResponseEntity.ok(uService.bloquear(id));
        }

        @GetMapping("/complete-status")
        @Operation(
                summary = "Verificar si el usuario debe completar su registro",
                description = "Retorna si el usuario autenticado tiene campos obligatorios incompletos (caso OAuth)."
        )
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Estado de completitud obtenido exitosamente"),
                @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
        })
        public ResponseEntity<CompleteStatusResponse> getCompleteStatus() {

           Authentication aut = SecurityContextHolder.getContext().getAuthentication();
           Long idUsuario = Long.parseLong(aut.getName());

            return ResponseEntity.ok(uService.getCompleteStatus(idUsuario));
        }


}
