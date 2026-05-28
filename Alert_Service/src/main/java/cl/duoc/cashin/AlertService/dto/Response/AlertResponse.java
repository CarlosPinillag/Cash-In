package cl.duoc.cashin.AlertService.dto.Response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO de respuesta principal — expuesto al cliente REST
// Los campos idAlert, userId, budgetId, tipo, mensaje y leida coinciden exactamente
// con AlertRemoteResponse del budget-service para garantizar interoperabilidad
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertResponse {

    private Long idAlert;
    private Long userId;
    private Long budgetId;
    private String tipo;
    private String mensaje;
    private Boolean leida;
    private LocalDate fechaCreacion;
}
