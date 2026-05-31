package cl.duoc.cashin.AlertService.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.cashin.AlertService.Service.AlertService;
import cl.duoc.cashin.AlertService.dto.Request.AlertRequest;
import cl.duoc.cashin.AlertService.dto.Response.AlertResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor

public class AlertController {

    private final AlertService alertService;

    @PostMapping
    public ResponseEntity<AlertResponse> crear(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AlertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(alertService.crear(request, authHeader)); // HTTP 201
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.obtenerPorId(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AlertResponse>> listarPorUsuario(@PathVariable Long userId) {
        return ResponseEntity.ok(alertService.listarPorUsuario(userId));
    }

    @GetMapping("/user/{userId}/no-leidas")
    public ResponseEntity<List<AlertResponse>> listarNoLeidasPorUsuario(@PathVariable Long userId) {
        return ResponseEntity.ok(alertService.listarNoLeidasPorUsuario(userId));
    }

    @GetMapping("/user/{userId}/contador")
    public ResponseEntity<Long> contarNoLeidasPorUsuario(@PathVariable Long userId) {
        return ResponseEntity.ok(alertService.contarNoLeidasPorUsuario(userId));
    }

    @PutMapping("/{id}/leer")
    public ResponseEntity<AlertResponse> marcarComoLeida(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.marcarComoLeida(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        alertService.eliminar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 sin body
    }
}
