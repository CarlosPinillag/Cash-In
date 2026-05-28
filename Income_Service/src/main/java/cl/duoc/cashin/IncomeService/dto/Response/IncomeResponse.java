package cl.duoc.cashin.IncomeService.dto.Response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncomeResponse {

    private Long idIncome;
    private Long userId;
    private Double monto;
    private String descripcion;
    private String categoria;
    private LocalDate fecha;
    private Boolean recurrente;
    private String frecuencia;
}
