package cl.duoc.cashin.PromotionService.dto.Request;

import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionUpdateRequest {

    // Todos los campos son opcionales — solo se actualizan los que no sean null

    @Size(min = 5, max = 300, message = "LA DESCRIPCION DEBE TENER ENTRE 5 Y 300 CARACTERES")
    private String descripcion;

    @Positive(message = "EL VALOR DE DESCUENTO DEBE SER MAYOR A 0")
    @DecimalMax(value = "100000000", message = "EL VALOR DE DESCUENTO NO PUEDE SUPERAR 100.000.000")
    private Double valorDescuento;

    @Future(message = "LA FECHA DE FIN DEBE SER FUTURA")
    private LocalDate fechaFin;

    @Positive(message = "EL USO MAXIMO DEBE SER MAYOR A 0")
    private Integer usoMaximo;

    @Pattern(regexp = "^(true|false)$", message = "EL CAMPO ACTIVO DEBE SER true O false")
    private String activo;
}
