package cl.duoc.cashin.ExpenseService.dto.Response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO que mapea la respuesta JSON que devuelve user-service
// Solo los campos que expense-service necesita para validar
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRemoteResponse {

    private Long idUser;
    private String nombre;
    private String email;
    private String telefono;
    private LocalDate fechaRegistro;
    private Boolean activo;
    private Double presupuestoMensual;
}
