package cl.duoc.cashin.NotificationService.dto.Request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationUpdateRequest {

    @Pattern(regexp = "^(PENDIENTE|ENVIADO|FALLIDO)$", message = "EL ESTADO DEBE SER PENDIENTE, ENVIADO O FALLIDO")
    private String estado;

    @Size(min = 3, max = 200, message = "EL TITULO DEBE TENER ENTRE 3 Y 200 CARACTERES")
    private String titulo;

    @Size(min = 5, max = 1000, message = "EL MENSAJE DEBE TENER ENTRE 5 Y 1000 CARACTERES")
    private String mensaje;

    // Permite marcar como leída desde el endpoint de actualización general
    private Boolean leida;
}
