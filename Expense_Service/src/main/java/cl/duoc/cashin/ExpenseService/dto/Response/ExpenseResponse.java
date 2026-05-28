package cl.duoc.cashin.ExpenseService.dto.Response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseResponse {

    private Long idExpense;
    private Long userId;
    private Long categoryId;
    private String nombreCategoria;
    private Double monto;
    private String descripcion;
    private LocalDate fecha;
    private String tipo;
}
