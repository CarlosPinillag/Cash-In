package cl.duoc.cashin.AuthService.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.cashin.AuthService.Service.AuthTokenService;
import cl.duoc.cashin.AuthService.dto.Request.AuthTokenRequest;
import cl.duoc.cashin.AuthService.dto.Response.AuthTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController

@RequestMapping("/api/v1/auth")

@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Operaciones de login, logout y validación de tokens JWT")
public class AuthTokenController {

    private final AuthTokenService authTokenService;

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Valida las credenciales del usuario y devuelve un token JWT para autenticarse en el resto de microservicios. No requiere token previo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso, token generado",
                    content = @Content(schema = @Schema(implementation = AuthTokenResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    public ResponseEntity<AuthTokenResponse> login(
            @Valid @RequestBody AuthTokenRequest request) {

        AuthTokenResponse response = authTokenService.login(request);
        return ResponseEntity.ok(response); // HTTP 200
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Cerrar sesión",
            description = "Invalida el token JWT enviado, finalizando la sesión activa del usuario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesión cerrada exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    public ResponseEntity<String> logout(
            @Parameter(description = "Token JWT con formato 'Bearer {token}'", required = true)
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        authTokenService.logout(token);
        return ResponseEntity.ok("Sesion cerrada exitosamente"); // HTTP 200
    }

    @GetMapping("/validate")
    @Operation(
            summary = "Validar un token",
            description = "Verifica si un token JWT es válido y no ha expirado. Usado internamente por otros microservicios y por el API Gateway."
    )
    @ApiResponse(responseCode = "200", description = "Resultado de la validación (true/false)")
    public ResponseEntity<Boolean> validateToken(
            @Parameter(description = "Token JWT a validar (sin el prefijo 'Bearer')", required = true)
            @RequestParam String token) {

        boolean esValido = authTokenService.validateToken(token);
        return ResponseEntity.ok(esValido); // HTTP 200 sin body
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener un registro de token por ID",
            description = "Devuelve el detalle de un registro de autenticación según su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro encontrado",
                    content = @Content(schema = @Schema(implementation = AuthTokenResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese ID")
    })
    public ResponseEntity<AuthTokenResponse> obtenerPorId(
            @Parameter(description = "ID del registro de autenticación", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(authTokenService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar un registro de token",
            description = "Elimina de forma permanente un registro de autenticación según su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese ID")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del registro a eliminar", example = "1")
            @PathVariable Long id) {
        authTokenService.eliminar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 sin body
    }
}
