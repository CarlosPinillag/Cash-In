package cl.duoc.cashin.AlertService.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertRequest {

    @NotNull(message = "EL ID DE USUARIO ES OBLIGATORIO")
    @Positive(message = "EL ID DE USUARIO DEBE SER MAYOR A 0")
    private Long userId;

    @NotNull(message = "EL ID DE PRESUPUESTO ES OBLIGATORIO")
    @Positive(message = "EL ID DE PRESUPUESTO DEBE SER MAYOR A 0")
    private Long budgetId;

    @NotBlank(message = "EL TIPO ES OBLIGATORIO")
    @Pattern(regexp = "^(ALERTA_80|ALERTA_100)$", message = "EL TIPO DEBE SER ALERTA_80 O ALERTA_100")
    private String tipo;

    @NotBlank(message = "EL MENSAJE ES OBLIGATORIO")
    @Size(min = 5, max = 500, message = "EL MENSAJE DEBE TENER ENTRE 5 Y 500 CARACTERES")
    private String mensaje;
}
