package cl.duoc.cashin.AuthService.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.cashin.AuthService.Service.AuthTokenService;
import cl.duoc.cashin.AuthService.dto.Request.AuthTokenRequest;
import cl.duoc.cashin.AuthService.dto.Response.AuthTokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
// = @Controller + @ResponseBody: convierte retornos a JSON automaticamente
@RequestMapping("/api/v1/auth")
// Todos los endpoints de este controller empiezan con /api/v1/auth
@RequiredArgsConstructor
public class AuthTokenController {

    private final AuthTokenService authTokenService;
    // Spring inyecta AuthTokenService automaticamente

    // POST /api/v1/auth/login — Verificar credenciales y generar token
    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(
            @Valid @RequestBody AuthTokenRequest request) {
        // @Valid activa las validaciones del DTO (@NotBlank, @Email, @Size)
        // @RequestBody lee el JSON del body de la peticion HTTP
        AuthTokenResponse response = authTokenService.login(request);
        return ResponseEntity.ok(response); // HTTP 200
    }

    // POST /api/v1/auth/logout — Invalidar token activo
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String token) {
        // @RequestParam lee el parametro de query string: ?token=abc123
        authTokenService.logout(token);
        return ResponseEntity.ok("Sesion cerrada exitosamente"); // HTTP 200
    }

    // GET /api/v1/auth/validate — Verificar si un token es valido
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        // Retorna true si el token existe, esta activo y no ha vencido
        // Retorna false si el token no existe, esta inactivo o vencio
        boolean esValido = authTokenService.validateToken(token);
        return ResponseEntity.ok(esValido); // HTTP 200
    }

    // GET /api/v1/auth/{id} — Obtener token por ID
    @GetMapping("/{id}")
    public ResponseEntity<AuthTokenResponse> obtenerPorId(@PathVariable Long id) {
        // @PathVariable extrae el valor {id} de la URL
        return ResponseEntity.ok(authTokenService.obtenerPorId(id));
    }

    // DELETE /api/v1/auth/{id} — Eliminar token por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        authTokenService.eliminar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 sin body
    }
}
