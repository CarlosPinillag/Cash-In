package cl.duoc.cashin.CategoryService.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {

    private Long idCategory;
    private String nombre;
    private String descripcion;
    private String tipo;
    private Boolean activo;
}
