package cl.duoc.cashin.AuthService.dto.Response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthTokenResponse {

    // Token generado para esta sesion
    private String token;

    // Email del usuario autenticado
    private String username;

    // Momento exacto en que expira el token
    private LocalDateTime expiresAt;
}
