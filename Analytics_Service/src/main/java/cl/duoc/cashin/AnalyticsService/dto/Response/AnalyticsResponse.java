package cl.duoc.cashin.AnalyticsService.dto.Response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO de respuesta principal — expuesto al cliente REST
// No expone la entidad JPA directamente (separación DTO/entidad)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsResponse {

    private Long idAnalytics;
    private Long userId;
    private Double totalIngresos;
    private Double totalGastos;
    private Double balance;
    private Double tasaAhorro;
    private String estadoBalance;
    private LocalDate fechaGeneracion;
}
