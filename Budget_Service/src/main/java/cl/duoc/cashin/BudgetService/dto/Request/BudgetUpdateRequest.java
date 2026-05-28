package cl.duoc.cashin.BudgetService.dto.Request;

import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetUpdateRequest {

    @Positive(message = "EL MONTO DEBE SER MAYOR A 0")
    @DecimalMax(value = "999999999", message = "EL MONTO MAXIMO ES 999.999.999")
    private Double montoLimite;

    @Pattern(regexp = "^(DIARIO|SEMANAL|MENSUAL)$", message = "EL PERIODO DEBE SER DIARIO, SEMANAL O MENSUAL")
    private String periodo;

    private Boolean activo;

    private LocalDate fechaInicio;
}
