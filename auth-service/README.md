# auth-service — CASH-IN

Microservicio de autenticacion. Puerto: 8080

## Prerequisito: agregar endpoint en user-service

Antes de usar auth-service, user-service debe exponer el endpoint por email.
Agregar este metodo en UserController.java del user-service:

```java
// GET /api/v1/users/email/{email} — Buscar usuario por email
// Este endpoint es consumido por auth-service
@GetMapping("/email/{email}")
public ResponseEntity<UserResponse> obtenerPorEmail(@PathVariable String email) {
    return ResponseEntity.ok(userService.obtenerPorEmail(email));
}
```

Y agregar este metodo en UserService.java del user-service:

```java
public UserResponse obtenerPorEmail(String email) {
    log.info("Buscando usuario por email: {}", email);
    UserModel model = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Usuario con email " + email + " no encontrado"));
    return mapToResponse(model);
}
```

IMPORTANTE: UserResponse debe incluir passwordHash para que auth-service
pueda comparar la password. Crear un DTO especifico para uso interno:

```java
// En user-service: dto/Response/UserInternalResponse.java
// (solo para consumo de otros microservicios, NO para clientes externos)
@Data @AllArgsConstructor @NoArgsConstructor
public class UserInternalResponse {
    private Long idUser;
    private String email;
    private String passwordHash; // incluido para auth-service
}
```

---

## SQL — Crear base de datos

```sql
CREATE DATABASE db_auth;
```

---

## Orden de arranque

1. Iniciar user-service (puerto 8081) PRIMERO
2. Iniciar auth-service (puerto 8080) SEGUNDO

---

## Pruebas en Postman

### 1. Crear usuario en user-service (si no existe)

```
POST http://localhost:8081/api/v1/users
Content-Type: application/json

{
    "nombre": "Juan Perez",
    "email": "juan@email.com",
    "password": "pass123",
    "telefono": "56912345678",
    "presupuestoMensual": 500000
}
```

Respuesta esperada HTTP 200:

```json
{
  "idUser": 1,
  "nombre": "Juan Perez",
  "email": "juan@email.com",
  "telefono": "56912345678",
  "fechaRegistro": "2025-05-11",
  "activo": true,
  "presupuestoMensual": 500000.0
}
```

---

### 2. Login — POST /api/v1/auth/login

```
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
    "username": "juan@email.com",
    "password": "pass123"
}
```

Respuesta esperada HTTP 200:

```json
{
  "token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "username": "juan@email.com",
  "expiresAt": "2025-05-12T14:30:00"
}
```

---

### 3. Validar token — GET /api/v1/auth/validate?token=...

```
GET http://localhost:8080/api/v1/auth/validate?token=a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

Respuesta esperada HTTP 200:

```json
true
```

Si el token no existe o vencio:

```json
false
```

---

### 4. Logout — POST /api/v1/auth/logout?token=...

```
POST http://localhost:8080/api/v1/auth/logout?token=a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

Respuesta esperada HTTP 200:

```
Sesion cerrada exitosamente
```

---

### 5. Probar credenciales incorrectas

```
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
    "username": "juan@email.com",
    "password": "passwordMALA"
}
```

Respuesta esperada HTTP 409:

```
Credenciales invalidas — password incorrecta
```

---

### 6. Probar usuario inexistente

```
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
    "username": "noexiste@email.com",
    "password": "pass123"
}
```

Respuesta esperada HTTP 404:

```json
{
  "timestamp": "2025-05-11",
  "status": 404,
  "error": "Not Found",
  "message": "Usuario con email noexiste@email.com no existe en el sistema",
  "path": "/api/v1/auth/login",
  "claseException": "ResourceNotFoundException"
}
```

---

### 7. Probar validacion Bean Validation — campo vacio

```
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
    "username": "",
    "password": "pass123"
}
```

Respuesta esperada HTTP 400:

```json
{
  "username": "EL USERNAME ES OBLIGATORIO"
}
```
