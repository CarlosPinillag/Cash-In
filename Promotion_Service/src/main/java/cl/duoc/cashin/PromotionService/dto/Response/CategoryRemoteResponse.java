package cl.duoc.cashin.PromotionService.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRemoteResponse {

    // ID de la categoria en category-service
    private Long idCategory;

    // Nombre de la categoria — se guarda localmente en PromotionModel.nombreCategoria
    private String nombre;

    // Descripcion de la categoria
    private String descripcion;

    // true = categoria activa; false = desactivada
    private Boolean activo;
}
