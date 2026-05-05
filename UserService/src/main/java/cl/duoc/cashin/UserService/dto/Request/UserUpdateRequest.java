package cl.duoc.cashin.UserService.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class UserUpdateRequest {

    @NotBlank(message = "EL NOMBRE ES OBLIGATORRIO")
    @Size(min = 4, max = 20, message = "EL NOMBRE DEBE TENER MINIMO 4 CARATERES Y MAXIMO 20")
    private String nombre;

    @NotBlank(message = "EL EMAIL ES OBLIGATORRIO")
    @Size(min = 9, max = 100, message = "EL EMAIL DEBE TENER MINIMO 9 CARATERES Y MAXIMO 100")
    private String email;

    @NotBlank(message = "EL PASSWORD ES OBLIGATORRIO")
    @Size(min = 6, max = 10, message = "EL PASSWORD DEBE TENER MINIMO 6 CARATERES Y MAXIMO 10")
    private String passwordHash;

    @NotBlank(message = "EL TELEFONO ES OBLIGATORRIO")
    @Size(min = 11, message = "EL TELEFONO DEBE TENER 11 DIGITOS ")
    private String telefono;

    @NotBlank(message = "EL PRESUPUESTO INICIAL ES OBLIGATORRIO")
    @Size(max = 100000000, message = "EL MONTO MAXIMO DEBE SER 100.000.000 ")
    @Positive(message = "LA CANTIDAD TIENE QUE SER MAYOR A 0")
    private double presupuestoMensual;

}
