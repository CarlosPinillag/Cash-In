package cl.duoc.cashin.PromotionService.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionAplicarResponse {

    // Codigo de la promocion que se aplico
    private String codigo;

    // Monto original antes del descuento
    private Double montoOriginal;

    // Valor del descuento calculado (en pesos)
    private Double descuentoAplicado;

    // Monto final despues de aplicar el descuento
    // montoFinal = montoOriginal - descuentoAplicado
    private Double montoFinal;

    // Tipo de descuento aplicado: PORCENTAJE o MONTO_FIJO
    private String tipoDescuento;

    // Mensaje descriptivo del descuento aplicado
    private String mensaje;

    // Usos restantes de la promocion tras este canje
    private Integer usosRestantes;
}
