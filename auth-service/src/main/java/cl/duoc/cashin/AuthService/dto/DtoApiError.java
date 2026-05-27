package cl.duoc.cashin.AuthService.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// Builder permite: DtoApiError.builder().status(404).message("...").build()
public class DtoApiError {

    private LocalDate timestamp;    // cuando ocurrio el error
    private int status;             // codigo HTTP numerico: 404, 400, 409...
    private String error;           // texto HTTP: "Not Found", "Bad Request"...
    private String message;         // mensaje personalizado del error
    private String path;            // URL que causo el error
    private String claseException;  // nombre de la excepcion (para debugging)
}
