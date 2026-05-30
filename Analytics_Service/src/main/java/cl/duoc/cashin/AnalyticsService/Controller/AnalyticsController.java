package cl.duoc.cashin.AnalyticsService.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.cashin.AnalyticsService.Service.AnalyticsService;
import cl.duoc.cashin.AnalyticsService.dto.Request.AnalyticsRequest;
import cl.duoc.cashin.AnalyticsService.dto.Response.AnalyticsResponse;
import cl.duoc.cashin.AnalyticsService.dto.Response.ResumenFinancieroResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor

public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping
    public ResponseEntity<AnalyticsResponse> generarAnalisis(
            @Valid @RequestBody AnalyticsRequest request,
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(analyticsService.generarAnalisis(request, authHeader)); // HTTP 201
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalyticsResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.obtenerPorId(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AnalyticsResponse>> obtenerHistorialPorUsuario(
            @PathVariable Long userId) {
        return ResponseEntity.ok(analyticsService.obtenerHistorialPorUsuario(userId));
    }

    @GetMapping("/user/{userId}/resumen")
    public ResponseEntity<ResumenFinancieroResponse> obtenerResumenFinanciero(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(analyticsService.obtenerResumenFinanciero(userId, authHeader));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        analyticsService.eliminar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 sin body
    }
}
