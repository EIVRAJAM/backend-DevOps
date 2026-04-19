# OpenAPI/Swagger Documentación - Patrones Reutilizables

**Versión:** 1.0  
**Última actualización:** Abril 2026  
**Idioma:** Español  
**Framework:** Spring Boot 4.0.5 + Springdoc OpenAPI 3.0.2 + JWT Bearer

---

## 1. Introducción y Fundamentos Técnicos

### ¿Por qué estas anotaciones?

| Anotación              | Propósito                           | Beneficio                                                         | Ejemplo                                                             |
| ---------------------- | ----------------------------------- | ----------------------------------------------------------------- | ------------------------------------------------------------------- |
| `@Tag`                 | Agrupa endpoints por módulo/recurso | Frontend ve menús organizados (Authentication, User Mgmt)         | `@Tag(name = "Authentication")`                                     |
| `@Operation`           | Define qué hace el endpoint         | Swagger genera títulos descriptivos en lugar de nombres genéricos | `@Operation(summary = "Iniciar sesión con credenciales")`           |
| `@ApiResponse`         | Documenta respuesta HTTP específica | Frontend sabe qué errores esperar y su significado                | `@ApiResponse(responseCode = "400")`                                |
| `@Parameter`           | Describe parámetros path/query      | Frontend entiende qué campos son opcionales/requeridos            | `@Parameter(description = "ID único del usuario", required = true)` |
| `@Schema`              | Documenta DTOs con ejemplos         | Swagger pre-llena formularios con JSON válido                     | `@Schema(example = "jperez@example.com")`                           |
| `@SecurityRequirement` | Vincula con esquema JWT global      | Swagger muestra 🔒 en endpoints protegidos                        | `@SecurityRequirement(name = "bearerAuth")`                         |

---

## 2. Importaciones Necesarias

Agregar en cada controlador o DTO:

```java
// Para controladores
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.parameters.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

// Para DTOs
import io.swagger.v3.oas.annotations.media.Schema;
```

---

## 3. Patrones por Tipo de Endpoint

### 3.1. Patrón: Endpoint Público (Login, Signup)

**Ubicación:** Controladores públicos (auth)  
**Seguridad:** ❌ NO requiere JWT

```java
@PostMapping("/login")
@Operation(
    summary = "Iniciar sesión",
    description = "Autentica usuario con correo/username y contraseña. Retorna token JWT válido por 24 horas.",
    tags = {"Authentication"}
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Autenticación exitosa. Retorna token JWT en formato: {\"token\": \"eyJhbGc...\"}")
    @ApiResponse(responseCode = "400", description = "Validación fallida: correo/username o contraseña vacíos"),
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas: usuario no existe o contraseña incorrecta"),
    @ApiResponse(responseCode = "429", description = "Demasiados intentos de login fallidos (cuenta bloqueada temporalmente)")
})
public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
    // implementación
}
```

---

### 3.2. Patrón: Endpoint Protegido Básico (GET, DELETE sin parámetros complejos)

**Ubicación:** Controladores privados (usuario, acceso)  
**Seguridad:** ✅ Requiere JWT Bearer token

```java
@GetMapping("/{id}")
@SecurityRequirement(name = "bearerAuth")  // ← Indica que requiere JWT
@Operation(
    summary = "Obtener usuario por ID",
    description = "Recupera información completa de un usuario específico. Solo administradores pueden ver datos de otros usuarios.",
    tags = {"User Management"}
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Usuario encontrado exitosamente"),
    @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
    @ApiResponse(responseCode = "403", description = "Acceso denegado: usuario solo puede ver su propio perfil"),
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado en el sistema")
})
public ResponseEntity<UserListResponse> getUsuario(
    @PathVariable
    @Parameter(description = "ID único del usuario", required = true, example = "123")
    Long id
) {
    // implementación
}
```

---

### 3.3. Patrón: Endpoint con Query Parameters (Paginación + Filtros)

**Ubicación:** Endpoints de listado (usuario, funcionalidad, sesion)  
**Seguridad:** ✅ Requiere JWT  
**Parámetros:** Page, size, filtros opcionales

```java
@GetMapping
@SecurityRequirement(name = "bearerAuth")
@Operation(
    summary = "Listar usuarios con paginación y filtros",
    description = "Obtiene lista paginada de usuarios. Administradores ven todos; usuarios normales solo ven su propio registro. Soporta filtros por documento, nombres, apellidos, rol.",
    tags = {"User Management"}
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Lista de usuarios recuperada exitosamente"),
    @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos (page < 0 o size > 100)"),
    @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado")
})
public ResponseEntity<Page<UserListResponse>> listarUsuarios(
    @RequestParam(defaultValue = "0")
    @Parameter(description = "Número de página (comienza en 0)", example = "0")
    int page,

    @RequestParam(defaultValue = "20")
    @Parameter(description = "Cantidad de registros por página (máximo 100)", example = "20")
    int size,

    @RequestParam(required = false)
    @Parameter(description = "Filtro opcional: número de documento", example = "1234567890")
    String documento,

    @RequestParam(required = false)
    @Parameter(description = "Filtro opcional: nombres del usuario (búsqueda parcial)", example = "Juan")
    String nombres,

    @RequestParam(required = false)
    @Parameter(description = "Filtro opcional: apellidos del usuario (búsqueda parcial)", example = "Pérez")
    String apellidos,

    @RequestParam(required = false)
    @Parameter(description = "Filtro opcional: nombre del rol", example = "ADMIN")
    String nombreRol
) {
    // implementación
}
```

---

### 3.4. Patrón: Operación de Estado (Activate, Deactivate, Lock)

**Ubicación:** Endpoints que cambian estado (usuario, acceso)  
**Método HTTP:** PATCH  
**Seguridad:** ✅ Requiere JWT

```java
@PatchMapping("/{id}/activar")
@SecurityRequirement(name = "bearerAuth")
@Operation(
    summary = "Activar usuario",
    description = "Cambia el estado del usuario a ACTIVO. Solo administradores pueden realizar esta operación. Útil para reactivar cuentas desactivadas.",
    tags = {"User Management"}
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Usuario activado exitosamente"),
    @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
    @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden activar usuarios"),
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
    @ApiResponse(responseCode = "409", description = "Conflicto: usuario ya está activo")
})
public ResponseEntity<UserListResponse> activarUsuario(
    @PathVariable
    @Parameter(description = "ID único del usuario a activar", required = true, example = "123")
    Long id
) {
    // implementación
}
```

---

### 3.5. Patrón: Endpoint POST (Create con validaciones)

**Ubicación:** Endpoints de creación (usuario, acceso, funcionalidad)  
**Método HTTP:** POST  
**Seguridad:** ✅ Requiere JWT (excepto signup público)

```java
@PostMapping
@SecurityRequirement(name = "bearerAuth")
@Operation(
    summary = "Crear nuevo usuario",
    description = "Crea un nuevo registro de usuario en el sistema. Solo administradores pueden crear usuarios. Se valida que el documento y username no existan previamente.",
    tags = {"User Management"}
)
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
    @ApiResponse(responseCode = "400", description = "Validación fallida: campos requeridos vacíos, formato de email inválido, contraseña < 8 caracteres"),
    @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
    @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden crear usuarios"),
    @ApiResponse(responseCode = "409", description = "Conflicto: documento o username ya existen en el sistema")
})
public ResponseEntity<UserListResponse> crearUsuario(
    @RequestBody @Valid
    SignUpRequest request
) {
    // implementación
}
```

---

### 3.6. Patrón: Endpoint con Respuesta Role-Based (Admin vs Usuario)

**Ubicación:** Endpoints que retornan diferentes DTO según rol (acceso)  
**Seguridad:** ✅ Requiere JWT  
**Especial:** Documentar ambas respuestas posibles

```java
@PutMapping("/{idUsuario}")
@SecurityRequirement(name = "bearerAuth")
@Operation(
    summary = "Actualizar acceso de usuario",
    description = "Actualiza información de acceso del usuario. Administradores ven respuesta completa con datos sensibles; usuarios normales ven respuesta limitada sin datos de auditoria. Retorna AccesoAdminDTO (admin) o AccesoUserDTO (usuario).",
    tags = {"Access Management"}
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Acceso actualizado exitosamente. Respuesta varía según rol del usuario autenticado"),
    @ApiResponse(responseCode = "400", description = "Validación fallida"),
    @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
    @ApiResponse(responseCode = "403", description = "Acceso denegado: usuario solo puede actualizar su propio acceso"),
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
})
public ResponseEntity<?> actualizarAcceso(
    @PathVariable
    @Parameter(description = "ID del usuario cuyo acceso se actualiza", required = true, example = "123")
    Long idUsuario,

    @RequestBody @Valid
    AccesoUserDTO request
) {
    // implementación: retorna AccesoAdminDTO si es admin, AccesoUserDTO si es usuario
}
```

---

### 3.7. Patrón: Endpoint DELETE (Eliminar/Borrar recursos)

**Ubicación:** Endpoints de eliminación (sesion)  
**Método HTTP:** DELETE  
**Respuesta:** 204 No Content (sin body)  
**Seguridad:** ✅ Requiere JWT

```java
@DeleteMapping("/{id}")
@SecurityRequirement(name = "bearerAuth")
@Operation(
    summary = "Cerrar sesión",
    description = "Elimina una sesión activa, forzando al usuario a re-autenticarse. Solo administradores pueden eliminar sesiones de otros usuarios.",
    tags = {"Session Management"}
)
@ApiResponses({
    @ApiResponse(responseCode = "204", description = "Sesión eliminada exitosamente (sin contenido en respuesta)"),
    @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
    @ApiResponse(responseCode = "403", description = "Acceso denegado: usuario solo puede cerrar sus propias sesiones"),
    @ApiResponse(responseCode = "404", description = "Sesión no encontrada")
})
public ResponseEntity<Void> cerrarSesion(
    @PathVariable
    @Parameter(description = "ID único de la sesión a cerrar", required = true, example = "456")
    Long id
) {
    // implementación
}
```

---

### 3.8. Patrón: Endpoint con Recurso Jerárquico (Árbol/Padre-Hijo)

**Ubicación:** Endpoints con relaciones jerárquicas (funcionalidad)  
**Parámetro Especial:** `idPadre` (nullable = raíz del árbol)

```java
@PostMapping
@SecurityRequirement(name = "bearerAuth")
@Operation(
    summary = "Crear nueva funcionalidad",
    description = "Crea una nueva funcionalidad en el sistema. Puede ser una funcionalidad raíz (sin padre) o una subfuncionalidad dentro de una existente. Útil para crear menús jerárquicos y permisos anidados.",
    tags = {"Functionality Management"}
)
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "Funcionalidad creada exitosamente"),
    @ApiResponse(responseCode = "400", description = "Validación fallida: nombre vacío, URL inválida, padre no existe"),
    @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
    @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores pueden crear funcionalidades"),
    @ApiResponse(responseCode = "404", description = "Funcionalidad padre no encontrada (si idPadre fue especificado)")
})
public ResponseEntity<FuncionalidadResponse> crearFuncionalidad(
    @RequestBody @Valid
    FuncionalidadRequest request
) {
    // implementación
}

@GetMapping
@SecurityRequirement(name = "bearerAuth")
@Operation(
    summary = "Listar funcionalidades con filtros",
    description = "Obtiene lista de funcionalidades. Puede filtrar por estado (ACTIVA/INACTIVA) y por funcionalidad padre para ver árbol jerárquico.",
    tags = {"Functionality Management"}
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Lista de funcionalidades recuperada"),
    @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado")
})
public ResponseEntity<List<FuncionalidadResponse>> listarFuncionalidades(
    @RequestParam(required = false)
    @Parameter(description = "Filtro opcional por estado: ACTIVA o INACTIVA", example = "ACTIVA")
    String status,

    @RequestParam(required = false)
    @Parameter(description = "Filtro opcional: ID de funcionalidad padre para ver solo sus hijas (null = solo raíces)", example = "1")
    Long idPadre
) {
    // implementación
}
```

---

## 4. Patrón: Documentación de DTOs (Records)

### 4.1 DTO Básico (sin relaciones)

```java
package com.devops.backend.auth.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credenciales requeridas para iniciar sesión")
public record LoginRequest(
    @NotBlank(message = "El correo o username es obligatorio")
    @Schema(
        description = "Nombre de usuario o correo electrónico registrado",
        example = "jperez"
    )
    String usernameOrEmail,

    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(
        description = "Contraseña asociada a la cuenta",
        example = "SecurePass123"
    )
    String password
) {}
```

### 4.2 DTO con Validaciones Específicas

```java
@Schema(description = "Información para crear nueva cuenta de usuario")
public record SignUpRequest(
    @NotBlank
    @Schema(description = "Número de documento (cédula, pasaporte, etc.)", example = "1234567890")
    String documento,

    @Email
    @Schema(description = "Correo electrónico (debe ser único)", example = "jperez@example.com")
    String correoAcceso,

    @Size(min = 8, max = 128)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).*$", message = "Debe contener mayúsculas, minúsculas y números")
    @Schema(
        description = "Contraseña (mínimo 8 caracteres, debe incluir mayúsculas, minúsculas y números)",
        example = "SecurePass123"
    )
    String claveAcceso
) {}
```

### 4.3 DTO de Respuesta Complejo

```java
@Schema(description = "Información completa de una sesión activa del usuario")
public record SesionResponseDto(
    @Schema(description = "ID único de la sesión", example = "456")
    Long idSesion,

    @Schema(description = "ID del usuario propietario de la sesión", example = "123")
    Long idUsuario,

    @Schema(description = "Nombres completos del usuario", example = "Juan")
    String nombresUsuario,

    @Schema(description = "Apellidos del usuario", example = "Pérez García")
    String apellidosUsuario,

    @Schema(description = "Marca de tiempo de inicio de sesión (ISO-8601)", example = "2026-04-18T10:00:00")
    LocalDateTime fechaInicio,

    @Schema(description = "Marca de tiempo de cierre (null si sesión activa)", example = "2026-04-18T18:00:00")
    LocalDateTime fechaFin,

    @Schema(description = "Indica si la sesión está actualmente activa", example = "true")
    Boolean activa,

    @Schema(description = "JWT Token ID (JTI) para auditoría y blacklist", example = "550e8400-e29b-41d4-a716-446655440000")
    String tokenJti
) {}
```

---

## 5. Estructura de Respuesta Global

### 5.1 Respuesta Exitosa (200/201)

**DTO devuelto directamente** con estructura específica del recurso.

**Ejemplo para Usuario 201:**

```json
{
  "idUsuario": 123,
  "documento": "1234567890",
  "nombres": "Juan",
  "apellidos": "Pérez",
  "correoAcceso": "jperez@example.com",
  "estado": "ACTIVO"
}
```

### 5.2 Respuestas de Error (4xx/5xx)

**400 Bad Request** - Validación:

```json
{
  "timestamp": "2026-04-18T14:30:00",
  "status": 400,
  "error": "Validation Error",
  "message": "El correo debe ser válido",
  "path": "/v1/usuarios"
}
```

**401 Unauthorized** - JWT inválido:

```json
{
  "timestamp": "2026-04-18T14:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token JWT inválido o expirado"
}
```

**403 Forbidden** - Permisos insuficientes:

```json
{
  "timestamp": "2026-04-18T14:30:00",
  "status": 403,
  "error": "Access Denied",
  "message": "Usuario solo puede ver su propio perfil"
}
```

**404 Not Found**:

```json
{
  "timestamp": "2026-04-18T14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Usuario con ID 999 no encontrado"
}
```

**409 Conflict** - Recurso duplicado:

```json
{
  "timestamp": "2026-04-18T14:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Documento 1234567890 ya existe en el sistema"
}
```

---

## 6. Mejores Prácticas

### 6.1 En Controladores

✅ **HACER:**

```java
@Tag(name = "User Management", description = "Operaciones relacionadas con usuarios del sistema")
@RestController
@RequestMapping("/v1/usuarios")
public class UsuarioController {

    @PostMapping
    @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario...")
    @ApiResponses({...})
    public ResponseEntity<> crear(@RequestBody @Valid SignUpRequest req) {}
}
```

❌ **NO HACER:**

```java
@RestController
@RequestMapping("/v1/usuarios")
public class UsuarioController {

    @PostMapping
    public ResponseEntity<> crear(@RequestBody SignUpRequest req) {}  // Sin documentación
}
```

### 6.2 En DTOs

✅ **HACER:**

```java
public record LoginRequest(
    @NotBlank
    @Schema(description = "Email o username", example = "jperez")
    String usernameOrEmail
) {}
```

❌ **NO HACER:**

```java
public record LoginRequest(
    @Schema
    String usernameOrEmail
) {}  // Sin description ni example
```

### 6.3 En Parámetros

✅ **HACER:**

```java
public ResponseEntity<> listar(
    @RequestParam(defaultValue = "0")
    @Parameter(description = "Número de página", example = "0")
    int page
) {}
```

❌ **NO HACER:**

```java
public ResponseEntity<> listar(
    @RequestParam int page  // Sin @Parameter
) {}
```

---

## 7. Validación en Swagger UI

Después de implementar, validar en **http://localhost:3020/swagger-ui.html**:

- [ ] Cada módulo aparece como Tab separado (Authentication, User Management, etc.)
- [ ] Cada endpoint tiene **description** clara (no solo "post" o "get")
- [ ] Los DTOs muestran ejemplos JSON en los "Try it out" forms
- [ ] Los errores HTTP están documentados (400, 401, 403, 404, 409)
- [ ] Los parámetros query/path tienen @Parameter con description
- [ ] Los endpoints protegidos muestran 🔒 (lock icon)

---

## 8. Patrones Aplicables por Módulo

| Módulo            | Endpoints | Patrones Aplicables                                          | Complejidad |
| ----------------- | --------- | ------------------------------------------------------------ | ----------- |
| **auth**          | 7         | Público (3.1), Role-Based (3.6 - respuesta)                  | ⭐⭐        |
| **acceso**        | 8         | Protegido (3.2), Query (3.3), Estado (3.4), Role-Based (3.6) | ⭐⭐⭐      |
| **usuario**       | 7         | Protegido (3.2), Query (3.3), POST (3.5), Estado (3.4)       | ⭐⭐⭐      |
| **funcionalidad** | 6         | Jerárquico (3.8), Query (3.3), Estado (3.4), POST (3.5)      | ⭐⭐⭐⭐    |
| **sesion**        | 4         | Query (3.3), DELETE (3.7)                                    | ⭐⭐        |
| **rol**           | ?         | Protegido (3.2), POST (3.5), Query (3.3), Estado (3.4)       | ⭐⭐        |
| **preferencia**   | ?         | Protegido (3.2), POST (3.5)                                  | ⭐⭐        |

---

## 9. Checklist de Implementación

- [ ] Agregar importaciones de `io.swagger.v3.oas.annotations.*`
- [ ] Agregar `@Tag` en nivel de clase del controlador
- [ ] Agregar `@Operation` en cada método
- [ ] Agregar `@ApiResponses` con códigos HTTP relevantes (200, 400, 401, 403, 404, 409)
- [ ] Agregar `@Parameter` en path/query parameters
- [ ] Agregar `@Schema` con description y example en cada DTO
- [ ] Agregar `@SecurityRequirement(name = "bearerAuth")` en endpoints protegidos
- [ ] Compilar sin errores: `mvn clean compile`
- [ ] Ejecutar tests: `mvn test`
- [ ] Iniciar servidor: `mvn spring-boot:run`
- [ ] Abrir Swagger UI: http://localhost:3020/swagger-ui.html
- [ ] Validar que todos los endpoints aparezcan correctamente documentados

---

**Fin de Patrones Reutilizables**
