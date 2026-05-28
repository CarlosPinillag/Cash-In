package cl.duoc.cashin.CategoryService.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "EL NOMBRE ES OBLIGATORIO")
    @Size(min = 2, max = 100, message = "EL NOMBRE DEBE TENER ENTRE 2 Y 100 CARACTERES")
    private String nombre;

    @NotBlank(message = "LA DESCRIPCION ES OBLIGATORIA")
    @Size(min = 3, max = 255, message = "LA DESCRIPCION DEBE TENER ENTRE 3 Y 255 CARACTERES")
    private String descripcion;

    @NotBlank(message = "EL TIPO ES OBLIGATORIO")
    @Pattern(regexp = "^(GASTO|INGRESO)$", message = "EL TIPO DEBE SER GASTO O INGRESO")
    private String tipo;
}
