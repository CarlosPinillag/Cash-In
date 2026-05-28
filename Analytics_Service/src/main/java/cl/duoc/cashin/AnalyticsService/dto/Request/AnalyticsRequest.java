package cl.duoc.cashin.AnalyticsService.dto.Request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsRequest {

    @NotNull(message = "EL ID DE USUARIO ES OBLIGATORIO")
    @Positive(message = "EL ID DE USUARIO DEBE SER MAYOR A 0")
    // ID del usuario para quien se genera el análisis financiero
    private Long userId;
}
