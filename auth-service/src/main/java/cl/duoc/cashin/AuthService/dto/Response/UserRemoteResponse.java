package cl.duoc.cashin.AuthService.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRemoteResponse {

    // ID del usuario en user-service
    private Long idUser;

    // Email del usuario — se usa como username en auth-service
    private String email;

    // Hash del password guardado en user-service
    // Se usa para comparar con el password recibido en el login
    private String passwordHash;
}
