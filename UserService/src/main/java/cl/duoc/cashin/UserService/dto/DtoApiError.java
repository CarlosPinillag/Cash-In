package cl.duoc.cashin.UserService.dto;

import java.time.LocalDate;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder // permite: DtoApiError.builder().status(404).message("...").build()

public class DtoApiError {

    private LocalDate timestamp; // cuándo ocurrió el error
    private int status; // código HTTP numérico: 404, 400, 409...
    private String error; // texto HTTP: "Not Found", "Bad Request"...
    private String message; // mensaje personalizado
    private String path; // URL que causó el error
    private String claseException; // nombre de la excepción (para debugging)
}
