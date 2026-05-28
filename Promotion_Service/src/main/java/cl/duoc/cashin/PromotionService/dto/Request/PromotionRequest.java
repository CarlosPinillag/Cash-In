package cl.duoc.cashin.PromotionService.dto.Request;

import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionRequest {

    private Long categoryId;

    @NotBlank(message = "EL CODIGO ES OBLIGATORIO")
    @Size(min = 3, max = 30, message = "EL CODIGO DEBE TENER ENTRE 3 Y 30 CARACTERES")
    // Solo letras mayusculas, numeros y guion bajo
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "EL CODIGO SOLO PUEDE CONTENER LETRAS MAYUSCULAS, NUMEROS Y GUION BAJO")
    private String codigo;

    @NotBlank(message = "LA DESCRIPCION ES OBLIGATORIA")
    @Size(min = 5, max = 300, message = "LA DESCRIPCION DEBE TENER ENTRE 5 Y 300 CARACTERES")
    private String descripcion;

    @NotBlank(message = "EL TIPO DE DESCUENTO ES OBLIGATORIO")
    @Pattern(regexp = "^(PORCENTAJE|MONTO_FIJO)$", message = "EL TIPO DE DESCUENTO DEBE SER PORCENTAJE O MONTO_FIJO")
    private String tipoDescuento;

    @NotNull(message = "EL VALOR DE DESCUENTO ES OBLIGATORIO")
    @Positive(message = "EL VALOR DE DESCUENTO DEBE SER MAYOR A 0")
    @DecimalMax(value = "100000000", message = "EL VALOR DE DESCUENTO NO PUEDE SUPERAR 100.000.000")
    private Double valorDescuento;

    @NotNull(message = "LA FECHA DE INICIO ES OBLIGATORIA")
    private LocalDate fechaInicio;

    @NotNull(message = "LA FECHA DE FIN ES OBLIGATORIA")
    @Future(message = "LA FECHA DE FIN DEBE SER FUTURA")
    private LocalDate fechaFin;

    @NotNull(message = "EL USO MAXIMO ES OBLIGATORIO")
    @Positive(message = "EL USO MAXIMO DEBE SER MAYOR A 0")
    private Integer usoMaximo;
}
