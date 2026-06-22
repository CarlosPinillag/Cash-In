package cl.duoc.cashin.IncomeService.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRemoteResponse {

    // Campos que user-service retorna 
    private Long idUser;
    private String nombre;
    private String email;
    private Boolean activo;
}
