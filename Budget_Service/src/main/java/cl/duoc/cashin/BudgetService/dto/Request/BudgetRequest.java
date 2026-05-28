package cl.duoc.cashin.BudgetService.dto.Request;

import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetRequest {

    @NotNull(message = "EL ID DE USUARIO ES OBLIGATORIO")
    private Long userId;

    // Opcional — null = presupuesto global del mes (no filtrado por categoría)
    private Long categoryId;

    @NotNull(message = "EL MONTO LIMITE ES OBLIGATORIO")
    @Positive(message = "EL MONTO DEBE SER MAYOR A 0")
    @DecimalMax(value = "999999999", message = "EL MONTO MAXIMO ES 999.999.999")
    private Double montoLimite;

    @NotBlank(message = "EL PERIODO ES OBLIGATORIO")
    @Pattern(regexp = "^(DIARIO|SEMANAL|MENSUAL)$",
             message = "EL PERIODO DEBE SER DIARIO, SEMANAL O MENSUAL")
    private String periodo;

    @NotNull(message = "LA FECHA DE INICIO ES OBLIGATORIA")
    private LocalDate fechaInicio;
}
