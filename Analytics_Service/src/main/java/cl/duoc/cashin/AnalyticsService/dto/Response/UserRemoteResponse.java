package cl.duoc.cashin.AnalyticsService.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRemoteResponse {
    private Long idUsuario;
    private String nombre;
    private String email;
    private String passwordHash;
    private Boolean activo;
}
