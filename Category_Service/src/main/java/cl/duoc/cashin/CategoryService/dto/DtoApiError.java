package cl.duoc.cashin.CategoryService.dto;

import java.time.LocalDate;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class DtoApiError {

    private LocalDate timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String claseException;
}
