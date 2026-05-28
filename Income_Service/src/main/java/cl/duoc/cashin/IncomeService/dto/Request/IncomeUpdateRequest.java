package cl.duoc.cashin.IncomeService.dto.Request;

import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncomeUpdateRequest {

    @Positive(message = "EL MONTO DEBE SER MAYOR A 0")
    @DecimalMax(value = "999999999", message = "EL MONTO MAXIMO ES 999.999.999")
    private Double monto;

    @Size(min = 3, max = 200, message = "LA DESCRIPCION DEBE TENER ENTRE 3 Y 200 CARACTERES")
    private String descripcion;

    @Pattern(regexp = "^(SALARIO|FREELANCE|ARRIENDO|OTRO)$", message = "LA CATEGORIA DEBE SER SALARIO, FREELANCE, ARRIENDO u OTRO")
    private String categoria;

    @PastOrPresent(message = "LA FECHA NO PUEDE SER FUTURA")
    private LocalDate fecha;

    private Boolean recurrente;

    private String frecuencia;
}
