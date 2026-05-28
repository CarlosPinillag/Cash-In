package cl.duoc.cashin.PromotionService.dto.Response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionResponse {

    private Long idPromotion;
    private Long categoryId;
    private String nombreCategoria;
    private String codigo;
    private String descripcion;
    private String tipoDescuento;
    private Double valorDescuento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer usoMaximo;
    private Integer usosActuales;
    private Boolean activo;
}
