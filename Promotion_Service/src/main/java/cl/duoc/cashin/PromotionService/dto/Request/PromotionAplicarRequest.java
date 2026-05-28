package cl.duoc.cashin.PromotionService.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionAplicarRequest {

    @NotNull(message = "EL ID DE USUARIO ES OBLIGATORIO")
    private Long userId;

    @NotBlank(message = "EL CODIGO DE PROMOCION ES OBLIGATORIO")
    private String codigo;

    @NotNull(message = "EL MONTO ORIGINAL ES OBLIGATORIO")
    @Positive(message = "EL MONTO ORIGINAL DEBE SER MAYOR A 0")
    private Double montoOriginal;
}
