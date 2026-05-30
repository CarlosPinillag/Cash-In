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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController

@RequestMapping("/api/v1/auth")

@RequiredArgsConstructor
public class AuthTokenController {

    private final AuthTokenService authTokenService;

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(
            @Valid @RequestBody AuthTokenRequest request) {

        AuthTokenResponse response = authTokenService.login(request);
        return ResponseEntity.ok(response); // HTTP 200
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        authTokenService.logout(token);
        return ResponseEntity.ok("Sesion cerrada exitosamente"); // HTTP 200
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {

        boolean esValido = authTokenService.validateToken(token);
        return ResponseEntity.ok(esValido); // HTTP 200 sin body
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthTokenResponse> obtenerPorId(@PathVariable Long id) {

        return ResponseEntity.ok(authTokenService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        authTokenService.eliminar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 sin body
    }
}
