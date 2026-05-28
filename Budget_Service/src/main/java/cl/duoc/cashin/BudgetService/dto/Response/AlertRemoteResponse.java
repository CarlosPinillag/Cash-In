package cl.duoc.cashin.BudgetService.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertRemoteResponse {

    private Long idAlert;
    private Long userId;
    private Long budgetId;
    private String tipo;
    private String mensaje;
    private Boolean leida;
}
