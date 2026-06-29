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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alertas", description = "Operaciones de alertas automáticas de presupuesto y notificaciones internas")
public class AlertController {

        private final AlertService alertService;

        @PostMapping
        @Operation(summary = "Crear una alerta", description = "Registra una nueva alerta asociada al usuario autenticado mediante el token JWT enviado en el header Authorization.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Alerta creada correctamente", content = @Content(schema = @Schema(implementation = AlertResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Datos de la solicitud inválidos"),
                        @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
        })
        public ResponseEntity<AlertResponse> crear(
                        @Parameter(description = "Token JWT con formato 'Bearer {token}'", required = true) @RequestHeader("Authorization") String authHeader,
                        @Valid @RequestBody AlertRequest request) {
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(alertService.crear(request, authHeader)); // HTTP 201
        }

        @GetMapping("/{id}")
        @Operation(summary = "Obtener una alerta por ID", description = "Devuelve el detalle de una alerta específica según su identificador.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Alerta encontrada", content = @Content(schema = @Schema(implementation = AlertResponse.class))),
                        @ApiResponse(responseCode = "404", description = "No existe una alerta con ese ID")
        })
        public ResponseEntity<AlertResponse> obtenerPorId(
                        @Parameter(description = "ID de la alerta", example = "1") @PathVariable Long id) {
                return ResponseEntity.ok(alertService.obtenerPorId(id));
        }

        @GetMapping("/user/{userId}")
        @Operation(summary = "Listar alertas de un usuario", description = "Devuelve todas las alertas registradas para un usuario específico.")
        @ApiResponse(responseCode = "200", description = "Lista de alertas del usuario")
        public ResponseEntity<List<AlertResponse>> listarPorUsuario(
                        @Parameter(description = "ID del usuario", example = "10") @PathVariable Long userId) {
                return ResponseEntity.ok(alertService.listarPorUsuario(userId));
        }

        @GetMapping("/user/{userId}/no-leidas")
        @Operation(summary = "Listar alertas no leídas de un usuario", description = "Devuelve únicamente las alertas que el usuario aún no ha marcado como leídas.")
        @ApiResponse(responseCode = "200", description = "Lista de alertas no leídas")
        public ResponseEntity<List<AlertResponse>> listarNoLeidasPorUsuario(
                        @Parameter(description = "ID del usuario", example = "10") @PathVariable Long userId) {
                return ResponseEntity.ok(alertService.listarNoLeidasPorUsuario(userId));
        }

        @GetMapping("/user/{userId}/contador")
        @Operation(summary = "Contar alertas no leídas", description = "Devuelve la cantidad de alertas no leídas que tiene un usuario, útil para mostrar un badge en la UI.")
        @ApiResponse(responseCode = "200", description = "Cantidad de alertas no leídas")
        public ResponseEntity<Long> contarNoLeidasPorUsuario(
                        @Parameter(description = "ID del usuario", example = "10") @PathVariable Long userId) {
                return ResponseEntity.ok(alertService.contarNoLeidasPorUsuario(userId));
        }

        @PutMapping("/{id}/leer")
        @Operation(summary = "Marcar una alerta como leída", description = "Actualiza el estado de la alerta indicada para marcarla como leída.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Alerta marcada como leída", content = @Content(schema = @Schema(implementation = AlertResponse.class))),
                        @ApiResponse(responseCode = "404", description = "No existe una alerta con ese ID")
        })
        public ResponseEntity<AlertResponse> marcarComoLeida(
                        @Parameter(description = "ID de la alerta", example = "1") @PathVariable Long id) {
                return ResponseEntity.ok(alertService.marcarComoLeida(id));
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Eliminar una alerta", description = "Elimina de forma permanente una alerta según su identificador.")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Alerta eliminada correctamente"),
                        @ApiResponse(responseCode = "404", description = "No existe una alerta con ese ID")
        })
        public ResponseEntity<Void> eliminar(
                        @Parameter(description = "ID de la alerta a eliminar", example = "1") @PathVariable Long id) {
                alertService.eliminar(id);
                return ResponseEntity.noContent().build(); // HTTP 204 sin body
        }
}
