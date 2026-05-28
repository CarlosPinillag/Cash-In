package cl.duoc.cashin.NotificationService.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO que mapea la respuesta JSON que devuelve user-service
// Solo los campos que notification-service necesita para validar y personalizar
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRemoteResponse {

    private Long idUser;
    private String nombre;
    private String apellido;
    private String email;
    private Boolean activo;
}
