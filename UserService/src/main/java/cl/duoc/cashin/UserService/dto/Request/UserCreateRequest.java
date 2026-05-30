package cl.duoc.cashin.UserService.dto.Request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class UserCreateRequest {

    @NotBlank(message = "EL NOMBRE ES OBLIGATORIO")
    @Size(min = 4, max = 20, message = "EL NOMBRE DEBE TENER MINIMO 4 Y MAXIMO 20 CARACTERES")
    private String nombre;

    @NotBlank(message = "EL EMAIL ES OBLIGATORIO")
    @Email(message = "FORMATO DE EMAIL INVALIDO")
    @Size(min = 9, max = 100, message = "EL EMAIL DEBE TENER MINIMO 9 Y MAXIMO 100 CARACTERES")
    private String email;

    @NotBlank(message = "EL PASSWORD ES OBLIGATORIO")
    @Size(min = 6, max = 10, message = "EL PASSWORD DEBE TENER MINIMO 6 Y MAXIMO 10 CARACTERES")
    private String password;

    @NotBlank(message = "EL TELEFONO ES OBLIGATORIO")
    @Size(min = 9, max = 15, message = "EL TELEFONO DEBE TENER ENTRE 9 Y 15 DIGITOS")
    @Pattern(regexp = "^[0-9]+$", message = "EL TELEFONO SOLO DEBE CONTENER NUMEROS")
    private String telefono;

    @NotNull(message = "EL PRESUPUESTO ES OBLIGATORIO")
    @Positive(message = "EL PRESUPUESTO DEBE SER MAYOR A 0")
    @DecimalMax(value = "100000000", message = "EL MONTO MAXIMO ES 100.000.000")
    private Double presupuestoMensual;
}
