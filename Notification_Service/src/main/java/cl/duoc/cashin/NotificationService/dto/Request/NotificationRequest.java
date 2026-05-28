package cl.duoc.cashin.NotificationService.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {

    @NotNull(message = "EL ID DE USUARIO ES OBLIGATORIO")
    private Long userId;

    @NotBlank(message = "EL CANAL ES OBLIGATORIO")
    @Pattern(regexp = "^(EMAIL|PUSH|IN_APP)$", message = "EL CANAL DEBE SER EMAIL, PUSH O IN_APP")
    private String canal;

    @NotBlank(message = "EL TIPO ES OBLIGATORIO")
    @Pattern(
        regexp = "^(BIENVENIDA|ALERTA_PRESUPUESTO|RESUMEN_MENSUAL|INGRESO_REGISTRADO|GASTO_REGISTRADO)$",
        message = "EL TIPO DEBE SER BIENVENIDA, ALERTA_PRESUPUESTO, RESUMEN_MENSUAL, INGRESO_REGISTRADO O GASTO_REGISTRADO"
    )
    private String tipo;

    @NotBlank(message = "EL TITULO ES OBLIGATORIO")
    @Size(min = 3, max = 200, message = "EL TITULO DEBE TENER ENTRE 3 Y 200 CARACTERES")
    private String titulo;

    @NotBlank(message = "EL MENSAJE ES OBLIGATORIO")
    @Size(min = 5, max = 1000, message = "EL MENSAJE DEBE TENER ENTRE 5 Y 1000 CARACTERES")
    private String mensaje;
}
