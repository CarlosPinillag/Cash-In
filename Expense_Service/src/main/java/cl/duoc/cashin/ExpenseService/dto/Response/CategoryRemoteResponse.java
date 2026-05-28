package cl.duoc.cashin.ExpenseService.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO que mapea la respuesta JSON que devuelve category-service
// Solo los campos que expense-service necesita para validar y guardar el nombre
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRemoteResponse {

    private Long idCategory;
    private String nombre;
    private String descripcion;
    private Boolean activo;
}
