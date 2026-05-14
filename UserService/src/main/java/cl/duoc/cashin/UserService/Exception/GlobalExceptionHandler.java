package cl.duoc.cashin.UserService.Exception;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cl.duoc.cashin.UserService.dto.DtoApiError;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
// Intercepta todas las excepciones lanzadas en cualquier @RestController
// Sin esto, Spring retorna un JSON genérico con HTTP 500 siempre

public class GlobalExceptionHandler {

    // ── Errores de validación @Valid ─────────────────────────────────
    // Se activa cuando un campo del Request no pasa @NotBlank, @Positive, etc.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errores.put(
                        error.getField(), // "nombre", "email", etc.
                        error.getDefaultMessage() // mensaje del @NotBlank
                ));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores); // 400
    }

    // ── ResourceNotFoundException → HTTP 404 ─────────────────────────
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

    // ── RuntimeException (reglas de negocio) → HTTP 409 ─────────────
    // Se activa con: throw new RuntimeException("email ya existe")
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage()); // 409
    }
}
