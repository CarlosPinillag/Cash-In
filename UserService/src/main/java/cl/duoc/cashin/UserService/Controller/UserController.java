package cl.duoc.cashin.UserService.Controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import cl.duoc.cashin.UserService.Service.UserService;
import cl.duoc.cashin.UserService.dto.Request.UserCreateRequest;
import cl.duoc.cashin.UserService.dto.Request.UserUpdateRequest;
import cl.duoc.cashin.UserService.dto.Response.UserResponse;

@RestController
// = @Controller + @ResponseBody: convierte retornos a JSON automáticamente
@RequestMapping("/api/v1/users")
// Todos los endpoints de este controller empiezan con /api/v1/users
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;

    // POST /api/v1/users — Crear usuario
    @PostMapping
    public ResponseEntity<UserResponse> crear(
            @Valid @RequestBody UserCreateRequest request) {
        // @Valid activa las validaciones del DTO antes de entrar al método
        // @RequestBody lee el JSON del body de la petición HTTP
        return ResponseEntity.ok(userService.crearUsuario(request));
    }

    // GET /api/v1/users/{id} — Obtener por ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> obtenerPorId(@PathVariable Long id) {
        // @PathVariable extrae el valor {id} de la URL
        return ResponseEntity.ok(userService.obtenerPorId(id));
    }

    // GET /api/v1/users — Listar todos
    @GetMapping
    public ResponseEntity<List<UserResponse>> listarTodos() {
        return ResponseEntity.ok(userService.listarTodos());
    }

    // PUT /api/v1/users/{id} — Actualizar
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.actualizar(id, request));
    }

    // DELETE /api/v1/users/{id} — Desactivar (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        userService.eliminar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 sin body
    }
}
