package cl.duoc.cashin.AuthService.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRemoteResponse {

    private Long idUser;

    private String email;

    private String passwordHash;
}
