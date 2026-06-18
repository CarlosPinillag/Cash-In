package cl.duoc.cashin.AnalyticsService.dto.Response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
