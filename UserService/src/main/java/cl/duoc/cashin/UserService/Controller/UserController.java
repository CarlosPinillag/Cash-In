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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController

@RequestMapping("/api/v1/users")

@RequiredArgsConstructor

@Tag(name = "Usuarios", description = "Operaciones de creación, consulta y gestión de usuarios de Cash-In")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(
            summary = "Crear un usuario",
            description = "Registra un nuevo usuario en el sistema."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario creado correctamente",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de la solicitud inválidos")
    })
    public ResponseEntity<UserResponse> crear(
            @Valid @RequestBody UserCreateRequest request) {

        return ResponseEntity.ok(userService.crearUsuario(request));
    }

    @GetMapping("/email/{email}")
    @Operation(
            summary = "Obtener un usuario por email",
            description = "Devuelve el detalle público de un usuario según su correo electrónico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con ese email")
    })
    public ResponseEntity<UserResponse> obtenerPorEmail(
            @Parameter(description = "Email del usuario", example = "usuario@cashin.cl")
            @PathVariable String email) {
        return ResponseEntity.ok(userService.obtenerPorEmail(email));
    }

    @GetMapping("/internal/email/{email}")
    @Operation(
            summary = "Obtener el modelo interno de un usuario por email",
            description = "Endpoint de uso interno entre microservicios (por ejemplo auth-service) que devuelve el modelo completo del usuario, incluyendo datos no expuestos públicamente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con ese email")
    })
    public ResponseEntity<UserModel> obtenerModeloPorEmail(
            @Parameter(description = "Email del usuario", example = "usuario@cashin.cl")
            @PathVariable String email) {
        return ResponseEntity.ok(userService.obtenerModeloPorEmail(email));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener un usuario por ID",
            description = "Devuelve el detalle de un usuario específico según su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con ese ID")
    })
    public ResponseEntity<UserResponse> obtenerPorId(
            @Parameter(description = "ID del usuario", example = "10")
            @PathVariable Long id) {

        return ResponseEntity.ok(userService.obtenerPorId(id));
    }

    @GetMapping
    @Operation(
            summary = "Listar todos los usuarios",
            description = "Devuelve el listado completo de usuarios registrados en el sistema."
    )
    @ApiResponse(responseCode = "200", description = "Lista de usuarios")
    public ResponseEntity<List<UserResponse>> listarTodos() {
        return ResponseEntity.ok(userService.listarTodos());
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar un usuario",
            description = "Modifica los datos de un usuario existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con ese ID")
    })
    public ResponseEntity<UserResponse> actualizar(
            @Parameter(description = "ID del usuario a actualizar", example = "10")
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar un usuario",
            description = "Elimina de forma permanente un usuario según su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con ese ID")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del usuario a eliminar", example = "10")
            @PathVariable Long id) {
        userService.eliminar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 sin body
    }
}
