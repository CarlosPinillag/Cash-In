package cl.duoc.cashin.BudgetService.dto.Response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetResponse {

    private Long idBudget;
    private Long userId;
    private Long categoryId;     // null = presupuesto global
    private Double montoLimite;
    private String periodo;
    private Boolean activo;
    private Double porcentajeUso; // calculado en tiempo real al consultar seguimiento
    private LocalDate fechaInicio;
}
