package cl.duoc.cashin.NotificationService.dto.Response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {

    private Long idNotification;
    private Long userId;
    private String canal;
    private String tipo;
    private String titulo;
    private String mensaje;
    private String estado;
    private Boolean leida;
    private LocalDate fechaCreacion;
    private LocalDate fechaEnvio;
}
