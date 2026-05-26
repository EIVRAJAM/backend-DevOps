package com.devops.backend.acceso.controller;

import com.devops.backend.acceso.dto.CreateAccesoAdminRequestDTO;
import com.devops.backend.acceso.dto.AccesoAdminDTO;
import com.devops.backend.acceso.dto.AccesoUserDTO;
import com.devops.backend.acceso.dto.ActualizarPasswordUserDTO;
import com.devops.backend.acceso.dto.ActualizarPasswordAdminDTO;
import com.devops.backend.acceso.entity.Acceso;
import com.devops.backend.acceso.mappers.AccesoMapper;
import com.devops.backend.acceso.service.AccesoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accesos")
@Tag(name = "Access Management", description = "Operaciones de gestión de acceso y credenciales de usuarios. Incluye creación de cuentas, actualización, cambio de contraseña y operaciones de estado (activar/desactivar/bloquear).")
@SecurityRequirement(name = "bearerAuth")
public class AccesoController {

        @Autowired
        private AccesoService accesoService;

        @Autowired
        private AccesoMapper accesoMapper;

        @PostMapping
        @Operation(summary = "Crear acceso para usuario", description = "Crea una nueva cuenta de acceso (username + email) para un usuario existente. Solo administradores pueden crear accesos. Se asigna una contraseña temporal.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Acceso creado exitosamente con contraseña temporal"),
                        @ApiResponse(responseCode = "400", description = "Validación fallida: usuario no existe, username/email ya existe, o datos mal formateados"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden crear accesos"),
                        @ApiResponse(responseCode = "409", description = "Conflicto: username o correo ya registrados en el sistema")
        })
        public ResponseEntity<AccesoAdminDTO> createAcceso(
                        @Valid @RequestBody CreateAccesoAdminRequestDTO requestDTO) {

                Acceso acceso = accesoService.saveWithDefaultPassword(
                                requestDTO.idUsuario(),
                                requestDTO.username(),
                                requestDTO.correoAcceso());

                AccesoAdminDTO responseDTO = accesoMapper.toAccesoAdminDTO(acceso);
                return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        }

        @GetMapping("/{idUsuario}")
        @Operation(summary = "Obtener acceso de usuario", description = "Recupera información de acceso de un usuario especificado. Siempre retorna vista completa (AccesoAdminDTO). Solo administradores pueden ver accesos de otros usuarios.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Acceso encontrado exitosamente"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: usuario solo puede ver su propio acceso"),
                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
        })
        public ResponseEntity<AccesoAdminDTO> getAccesoByIdUsuario(
                        @PathVariable("idUsuario") @Parameter(description = "ID único del usuario", required = true, example = "123") Long idUsuario) {
                return accesoService.findByIdUsuario(idUsuario)
                                .map(accesoMapper::toAccesoAdminDTO)
                                .map(ResponseEntity::ok)
                                .orElseGet(() -> ResponseEntity.notFound().build());
        }

        @PutMapping("/{idUsuario}")
        @Operation(summary = "Actualizar acceso de usuario", description = "Actualiza username y/o correo del usuario. La respuesta varía según el rol: administradores ven AccesoAdminDTO (completo), usuarios normales ven AccesoUserDTO (sin campos de auditoría).")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Acceso actualizado exitosamente. Respuesta varía según rol del usuario autenticado"),
                        @ApiResponse(responseCode = "400", description = "Validación fallida: email inválido, username duplicado, o datos incompletos"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: usuario solo puede actualizar su propio acceso"),
                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                        @ApiResponse(responseCode = "409", description = "Conflicto: username o correo ya registrados")
        })
        public ResponseEntity<?> updateAcceso(
                        @PathVariable("idUsuario") @Parameter(description = "ID del usuario cuyo acceso se actualiza", required = true, example = "123") Long idUsuario,
                        @Valid @RequestBody AccesoUserDTO requestDTO,
                        Authentication authentication) {

                Acceso acceso = accesoService.update(idUsuario, requestDTO);

                // Verificar si el usuario autenticado tiene rol de ADMIN
                boolean isAdmin = authentication.getAuthorities().stream()
                                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

                if (isAdmin) {
                        AccesoAdminDTO responseDTO = accesoMapper.toAccesoAdminDTO(acceso);
                        return ResponseEntity.ok(responseDTO);
                } else {
                        AccesoUserDTO responseDTO = accesoMapper.toAccesoUserDTO(acceso);
                        return ResponseEntity.ok(responseDTO);
                }
        }

        @GetMapping
        @Operation(summary = "Listar todos los accesos", description = "Obtiene lista completa de todos los accesos en el sistema. Solo administradores pueden listar accesos de otros usuarios.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lista de accesos recuperada exitosamente"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden listar todos los accesos")
        })
        public ResponseEntity<List<AccesoAdminDTO>> getAllAccesos() {
                List<AccesoAdminDTO> accesos = accesoService.findAll().stream()
                                .map(accesoMapper::toAccesoAdminDTO)
                                .toList();
                return ResponseEntity.ok(accesos);
        }

        @PatchMapping("/{idUsuario}/desactivar")
        @Operation(summary = "Desactivar cuenta de usuario", description = "Cambia el estado de la cuenta a INACTIVA. El usuario no podrá iniciar sesión hasta que la cuenta sea reactivada. Solo administradores pueden desactivar cuentas de otros usuarios.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Cuenta desactivada exitosamente. Respuesta varía según rol"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: usuario solo puede desactivar su propia cuenta"),
                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                        @ApiResponse(responseCode = "409", description = "Conflicto: cuenta ya está inactiva")
        })
        public ResponseEntity<?> desactivarCuenta(
                        @PathVariable("idUsuario") @Parameter(description = "ID del usuario a desactivar", required = true, example = "123") Long idUsuario,
                        Authentication authentication) {

                Acceso acceso = accesoService.desactivarCuenta(idUsuario);

                // Verificar si el usuario autenticado tiene rol de ADMIN
                boolean isAdmin = authentication.getAuthorities().stream()
                                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

                if (isAdmin) {
                        AccesoAdminDTO responseDTO = accesoMapper.toAccesoAdminDTO(acceso);
                        return ResponseEntity.ok(responseDTO);
                } else {
                        AccesoUserDTO responseDTO = accesoMapper.toAccesoUserDTO(acceso);
                        return ResponseEntity.ok(responseDTO);
                }
        }

        @PatchMapping("/{idUsuario}/activar")
        @Operation(summary = "Activar cuenta de usuario", description = "Cambia el estado de la cuenta a ACTIVA. El usuario podrá iniciar sesión normalmente. Solo administradores pueden activar cuentas.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Cuenta activada exitosamente"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden activar cuentas"),
                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                        @ApiResponse(responseCode = "409", description = "Conflicto: cuenta ya está activa")
        })
        public ResponseEntity<AccesoAdminDTO> activarCuenta(
                        @PathVariable("idUsuario") @Parameter(description = "ID del usuario a activar", required = true, example = "123") Long idUsuario) {

                Acceso acceso = accesoService.activarCuenta(idUsuario);
                AccesoAdminDTO responseDTO = accesoMapper.toAccesoAdminDTO(acceso);
                return ResponseEntity.ok(responseDTO);
        }

        @PatchMapping("/{idUsuario}/bloquear")
        @Operation(summary = "Bloquear cuenta de usuario", description = "Cambia el estado de la cuenta a BLOQUEADA. El usuario no podrá iniciar sesión. Normalmente se bloquea automáticamente después de más de 5 intentos fallidos. Solo administradores pueden bloquear manualmente.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Cuenta bloqueada exitosamente"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden bloquear cuentas"),
                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                        @ApiResponse(responseCode = "409", description = "Conflicto: cuenta ya está bloqueada")
        })
        public ResponseEntity<AccesoAdminDTO> bloquearCuenta(
                        @PathVariable("idUsuario") @Parameter(description = "ID del usuario a bloquear", required = true, example = "123") Long idUsuario) {

                Acceso acceso = accesoService.bloquearCuenta(idUsuario);
                AccesoAdminDTO responseDTO = accesoMapper.toAccesoAdminDTO(acceso);
                return ResponseEntity.ok(responseDTO);
        }

        @PatchMapping("/cambiar-password")
        @Operation(summary = "Cambiar contraseña (usuario)", description = "Permite al usuario cambiar su propia contraseña. Se requiere validar la contraseña actual por seguridad. La contraseña debe tener mínimo 8 caracteres.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Contraseña actualizada exitosamente"),
                        @ApiResponse(responseCode = "400", description = "Validación fallida: contraseña actual incorrecta, nueva contraseña no cumple requisitos"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado")
        })
        public ResponseEntity<AccesoUserDTO> cambiarPassword(
                        @Valid @RequestBody ActualizarPasswordUserDTO passwordDTO,
                        Authentication authentication) {

                // Obtener el idUsuario del token
                Long idUsuario = Long.parseLong(authentication.getName());

                Acceso acceso = accesoService.cambiarPassword(idUsuario, passwordDTO);
                AccesoUserDTO responseDTO = accesoMapper.toAccesoUserDTO(acceso);
                return ResponseEntity.ok(responseDTO);
        }

        @PatchMapping("/{idUsuario}/cambiar-password-admin")
        @Operation(summary = "Cambiar contraseña (administrador)", description = "Permite al administrador resetear la contraseña de un usuario sin validar la contraseña actual. Único para casos de recuperación de acceso olvidado. Usar con cuidado.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Contraseña reseteada exitosamente"),
                        @ApiResponse(responseCode = "400", description = "Validación fallida: nueva contraseña no cumple requisitos"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden resetear contraseñas"),
                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
        })
        public ResponseEntity<AccesoAdminDTO> cambiarPasswordAdmin(
                        @PathVariable("idUsuario") @Parameter(description = "ID del usuario cuya contraseña se resetea", required = true, example = "123") Long idUsuario,
                        @Valid @RequestBody ActualizarPasswordAdminDTO passwordDTO) {

                Acceso acceso = accesoService.cambiarPasswordAdmin(idUsuario, passwordDTO);
                AccesoAdminDTO responseDTO = accesoMapper.toAccesoAdminDTO(acceso);
                return ResponseEntity.ok(responseDTO);
        }
}
