package cl.duoc.cashin.AuthService.dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthTokenRequest {

    @NotBlank(message = "EL USERNAME ES OBLIGATORIO")
    @Email(message = "EL USERNAME DEBE SER UN EMAIL VALIDO")
    private String username;

    @NotBlank(message = "EL PASSWORD ES OBLIGATORIO")
    @Size(min = 6, message = "EL PASSWORD DEBE TENER MINIMO 6 CARACTERES")
    private String password;
}
