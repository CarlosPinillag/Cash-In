package cl.duoc.cashin.NotificationService.Controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.cashin.NotificationService.Service.NotificationService;
import cl.duoc.cashin.NotificationService.dto.Request.NotificationRequest;
import cl.duoc.cashin.NotificationService.dto.Request.NotificationUpdateRequest;
import cl.duoc.cashin.NotificationService.dto.Response.NotificationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor

public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> crear(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.crear(request, authHeader));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.obtenerPorId(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> listarPorUsuario(
            @PathVariable Long userId,
            @RequestParam(required = false) String tipo) {
        if (tipo != null) {
            return ResponseEntity.ok(notificationService.listarPorTipo(userId, tipo));
        }
        return ResponseEntity.ok(notificationService.listarPorUsuario(userId));
    }

    @GetMapping("/user/{userId}/no-leidas")
    public ResponseEntity<List<NotificationResponse>> listarNoLeidasPorUsuario(
            @PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.listarNoLeidasPorUsuario(userId));
    }

    @GetMapping("/user/{userId}/contador")
    public ResponseEntity<Long> contarNoLeidasPorUsuario(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.contarNoLeidasPorUsuario(userId));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<NotificationResponse>> listarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(notificationService.listarPorEstado(estado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody NotificationUpdateRequest request) {
        return ResponseEntity.ok(notificationService.actualizar(id, request));
    }

    @PutMapping("/{id}/leer")
    public ResponseEntity<NotificationResponse> marcarComoLeida(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.marcarComoLeida(id));
    }

    @PutMapping("/{id}/enviar")
    public ResponseEntity<NotificationResponse> marcarComoEnviada(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.marcarComoEnviada(id));
    }

    @PutMapping("/{id}/fallar")
    public ResponseEntity<NotificationResponse> marcarComoFallida(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.marcarComoFallida(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        notificationService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
