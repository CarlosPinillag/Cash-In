package cl.duoc.cashin.AnalyticsService.dto.Response;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumenFinancieroResponse {

    private Long userId;

    //  Análisis financiero actual 
    private Double totalIngresos;
    private Double totalGastos;
    private Double balance;
    private Double tasaAhorro;
    private String estadoBalance;

    //  Métricas históricas 
    // Promedio de tasa de ahorro calculado sobre todos los snapshots del usuario
    private Double promedioTasaAhorroHistorico;
    // Cantidad de veces que el usuario tuvo balance negativo
    private Long cantidadBalancesNegativos;

    //  Presupuestos activos desde budget-service 
    // Lista de presupuestos activos del usuario para contexto adicional
    private List<BudgetRemoteResponse> presupuestosActivos;

    //  Metadatos 
    private LocalDate fechaGeneracion;
    // Mensaje interpretativo generado según el estado financiero del usuario
    private String recomendacion;
}
