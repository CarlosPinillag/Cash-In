package cl.duoc.cashin.AlertService.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO para mapear la respuesta de user-service
// Solo contiene los campos necesarios para validar existencia del usuario
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRemoteResponse {

    private Long idUser;
    private String nombre;
    private String email;
    private Boolean activo;
}
