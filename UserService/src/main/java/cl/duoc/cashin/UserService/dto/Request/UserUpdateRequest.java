package cl.duoc.cashin.UserService.dto.Request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class UserUpdateRequest {

    @Size(min = 4, max = 20, message = "EL NOMBRE DEBE TENER ENTRE 4 Y 20 CARACTERES")
    private String nombre;

    @Email(message = "FORMATO DE EMAIL INVALIDO")
    @Size(min = 9, max = 100, message = "EL EMAIL DEBE TENER ENTRE 9 Y 100 CARACTERES")
    private String email;

    @Size(min = 6, max = 10, message = "EL PASSWORD DEBE TENER ENTRE 6 Y 10 CARACTERES")
    private String password;

    @Size(min = 9, max = 15, message = "EL TELEFONO DEBE TENER ENTRE 9 Y 15 DIGITOS")
    @Pattern(regexp = "^[0-9]+$", message = "EL TELEFONO SOLO DEBE CONTENER NUMEROS")
    private String telefono;

    @Positive(message = "EL PRESUPUESTO DEBE SER MAYOR A 0")
    @DecimalMax(value = "100000000", message = "EL MONTO MAXIMO ES 100.000.000")
    private Double presupuestoMensual;
}