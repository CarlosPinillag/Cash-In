package cl.duoc.cashin.ExpenseService.dto.Request;

import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ExpenseRequest {

    @NotNull(message = "EL ID DE USUARIO ES OBLIGATORIO")
    private Long userId;

    @NotNull(message = "EL ID DE CATEGORIA ES OBLIGATORIO")
    private Long categoryId;

    @NotNull(message = "EL MONTO ES OBLIGATORIO")
    @Positive(message = "EL MONTO DEBE SER MAYOR A 0")
    @DecimalMax(value = "100000000", message = "EL MONTO MAXIMO ES 100.000.000")
    private Double monto;

    @NotBlank(message = "LA DESCRIPCION ES OBLIGATORIA")
    @Size(min = 3, max = 200, message = "LA DESCRIPCION DEBE TENER ENTRE 3 Y 200 CARACTERES")
    private String descripcion;

    @NotNull(message = "LA FECHA ES OBLIGATORIA")
    @PastOrPresent(message = "LA FECHA NO PUEDE SER FUTURA")
    private LocalDate fecha;

    @NotBlank(message = "EL TIPO ES OBLIGATORIO")
    @Pattern(regexp = "^(MANUAL|AUTO)$", message = "EL TIPO DEBE SER MANUAL O AUTO")
    private String tipo;
}
