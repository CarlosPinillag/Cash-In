package cl.duoc.cashin.AuthService.Exception;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cl.duoc.cashin.AuthService.dto.DtoApiError;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
// Intercepta todas las excepciones lanzadas en cualquier @RestController
// Sin esto, Spring retorna un JSON generico con HTTP 500 siempre
public class GlobalExceptionHandler {

    // ── Errores de validacion @Valid ──────────────────────────────────
    // Se activa cuando un campo del Request no pasa @NotBlank, @Email, @Size, etc.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errores.put(
                        error.getField(),           // "username", "password", etc.
                        error.getDefaultMessage()   // mensaje del @NotBlank
                ));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores); // HTTP 400
    }

    // ── ResourceNotFoundException → HTTP 404 ─────────────────────────
    // Se activa cuando buscamos un token o usuario que no existe
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<DtoApiError> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        DtoApiError error = DtoApiError.builder()
                .timestamp(LocalDate.now())
                .status(HttpStatus.NOT_FOUND.value())           // 404
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())  // "Not Found"
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .claseException("ResourceNotFoundException")
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // ── RuntimeException (reglas de negocio) → HTTP 409 ──────────────
    // Se activa con: throw new RuntimeException("credenciales invalidas")
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage()); // HTTP 409
    }

    // ── Exception generica → HTTP 500 ────────────────────────────────
    // Captura cualquier excepcion no manejada por los handlers anteriores
    @ExceptionHandler(Exception.class)
    public ResponseEntity<DtoApiError> handleGeneral(
            Exception ex, HttpServletRequest request) {

        DtoApiError error = DtoApiError.builder()
                .timestamp(LocalDate.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())           // 500
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())  // "Internal Server Error"
                .message("Error interno del servidor: " + ex.getMessage())
                .path(request.getRequestURI())
                .claseException(ex.getClass().getSimpleName())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
