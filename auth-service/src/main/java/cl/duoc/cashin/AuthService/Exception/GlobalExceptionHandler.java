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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {

        // registra qué campos fallaron la validación 
        log.warn("Error de validación: {}", ex.getBindingResult().getFieldErrors());

        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errores.put(
                        error.getField(),
                        error.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<DtoApiError> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        // registra qué recurso no se encontró y en qué path 
        log.warn("Recurso no encontrado en {}: {}", request.getRequestURI(), ex.getMessage());

        DtoApiError error = DtoApiError.builder()
                .timestamp(LocalDate.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .claseException("ResourceNotFoundException")
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {

        // registra reglas de negocio violadas (409 Conflict) 
        log.warn("Conflicto de negocio: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DtoApiError> handleGeneral(
            Exception ex, HttpServletRequest request) {

        // registra errores inesperados (500) con stack trace 
        log.error("Error interno en {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        DtoApiError error = DtoApiError.builder()
                .timestamp(LocalDate.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("Error interno del servidor: " + ex.getMessage())
                .path(request.getRequestURI())
                .claseException(ex.getClass().getSimpleName())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
