package cl.duoc.cashin.UserService.dto.Response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class UserResponse {

    private Long idUser;

    private String nombre;

    private String email;

    private String passwordHash;

    private String telefono;

    private LocalDate fechaRegistro;

    private boolean activo;

    private double presupuestoMensual;

}
