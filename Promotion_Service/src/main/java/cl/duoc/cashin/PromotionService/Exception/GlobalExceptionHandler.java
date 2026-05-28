package cl.duoc.cashin.PromotionService.Exception;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cl.duoc.cashin.PromotionService.dto.DtoApiError;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

        // ── Errores de validacion
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, String>> handleValidation(
                        MethodArgumentNotValidException ex) {

                Map<String, String> errores = new HashMap<>();
                ex.getBindingResult()
                                .getFieldErrors()
                                .forEach(error -> errores.put(
                                                error.getField(), // "codigo", "valorDescuento", etc.
                                                error.getDefaultMessage() // mensaje del @NotBlank / @Pattern
                                ));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores); // HTTP 400
        }

        // ── ResourceNotFoundException
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<DtoApiError> handleNotFound(
                        ResourceNotFoundException ex, HttpServletRequest request) {

                DtoApiError error = DtoApiError.builder()
                                .timestamp(LocalDate.now())
                                .status(HttpStatus.NOT_FOUND.value()) // 404
                                .error(HttpStatus.NOT_FOUND.getReasonPhrase()) // "Not Found"
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .claseException("ResourceNotFoundException")
                                .build();

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        // ── RuntimeException
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<String> handleRuntime(RuntimeException ex) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage()); // HTTP 409
        }

        // ── Exception generica
        @ExceptionHandler(Exception.class)
        public ResponseEntity<DtoApiError> handleGeneral(
                        Exception ex, HttpServletRequest request) {

                DtoApiError error = DtoApiError.builder()
                                .timestamp(LocalDate.now())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()) // 500
                                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()) // "Internal Server Error"
                                .message("Error interno del servidor: " + ex.getMessage())
                                .path(request.getRequestURI())
                                .claseException(ex.getClass().getSimpleName())
                                .build();

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
}
