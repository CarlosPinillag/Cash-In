package cl.duoc.cashin.AuthService.dto.Response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthTokenResponse {

    private String token;

    private String username;

    private LocalDateTime expiresAt;
}
