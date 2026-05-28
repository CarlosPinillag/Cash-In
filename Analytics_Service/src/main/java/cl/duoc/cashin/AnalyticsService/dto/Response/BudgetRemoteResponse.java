package cl.duoc.cashin.AnalyticsService.dto.Response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO para mapear la respuesta de budget-service
// Solo contiene los campos necesarios para analytics (no todos los campos del BudgetResponse)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetRemoteResponse {

    private Long idBudget;
    private Long userId;
    private Long categoryId;       // null si es presupuesto global
    private Double montoLimite;
    private String periodo;        // DIARIO, SEMANAL, MENSUAL
    private Boolean activo;
    private Double porcentajeUso;  // % de uso calculado por budget-service
    private LocalDate fechaInicio;
}
