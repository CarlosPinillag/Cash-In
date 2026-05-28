package cl.duoc.cashin.AlertService.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DtoApiError {

    private LocalDate timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String claseException;
}
