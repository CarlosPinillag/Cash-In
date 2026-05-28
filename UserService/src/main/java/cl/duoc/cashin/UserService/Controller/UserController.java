package cl.duoc.cashin.UserService.Controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import cl.duoc.cashin.UserService.Model.UserModel;
import cl.duoc.cashin.UserService.Service.UserService;
import cl.duoc.cashin.UserService.dto.Request.UserCreateRequest;
import cl.duoc.cashin.UserService.dto.Request.UserUpdateRequest;
import cl.duoc.cashin.UserService.dto.Response.UserResponse;

@RestController

@RequestMapping("/api/v1/users")

@RequiredArgsConstructor

public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> crear(
            @Valid @RequestBody UserCreateRequest request) {

        return ResponseEntity.ok(userService.crearUsuario(request));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> obtenerPorEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.obtenerPorEmail(email));
    }

    @GetMapping("/internal/email/{email}")
    public ResponseEntity<UserModel> obtenerModeloPorEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.obtenerModeloPorEmail(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> obtenerPorId(@PathVariable Long id) {

        return ResponseEntity.ok(userService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listarTodos() {
        return ResponseEntity.ok(userService.listarTodos());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        userService.eliminar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 sin body
    }
}
