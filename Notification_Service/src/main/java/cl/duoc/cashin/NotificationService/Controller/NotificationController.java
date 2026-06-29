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
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Operaciones de creación, consulta y gestión de estado de notificaciones al usuario")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(
            summary = "Crear una notificación",
            description = "Registra una nueva notificación asociada al usuario autenticado mediante el token JWT enviado en el header Authorization."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notificación creada correctamente",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de la solicitud inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    public ResponseEntity<NotificationResponse> crear(
            @Parameter(description = "Token JWT con formato 'Bearer {token}'", required = true)
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.crear(request, authHeader));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener una notificación por ID",
            description = "Devuelve el detalle de una notificación específica según su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificación encontrada",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe una notificación con ese ID")
    })
    public ResponseEntity<NotificationResponse> obtenerPorId(
            @Parameter(description = "ID de la notificación", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.obtenerPorId(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Listar notificaciones de un usuario",
            description = "Devuelve las notificaciones de un usuario. Si se indica el parámetro 'tipo', filtra solo las de ese tipo."
    )
    @ApiResponse(responseCode = "200", description = "Lista de notificaciones del usuario")
    public ResponseEntity<List<NotificationResponse>> listarPorUsuario(
            @Parameter(description = "ID del usuario", example = "10")
            @PathVariable Long userId,
            @Parameter(description = "Filtra por tipo de notificación. Si se omite, devuelve todas.")
            @RequestParam(required = false) String tipo) {
        if (tipo != null) {
            return ResponseEntity.ok(notificationService.listarPorTipo(userId, tipo));
        }
        return ResponseEntity.ok(notificationService.listarPorUsuario(userId));
    }

    @GetMapping("/user/{userId}/no-leidas")
    @Operation(
            summary = "Listar notificaciones no leídas de un usuario",
            description = "Devuelve únicamente las notificaciones que el usuario aún no ha marcado como leídas."
    )
    @ApiResponse(responseCode = "200", description = "Lista de notificaciones no leídas")
    public ResponseEntity<List<NotificationResponse>> listarNoLeidasPorUsuario(
            @Parameter(description = "ID del usuario", example = "10")
            @PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.listarNoLeidasPorUsuario(userId));
    }

    @GetMapping("/user/{userId}/contador")
    @Operation(
            summary = "Contar notificaciones no leídas",
            description = "Devuelve la cantidad de notificaciones no leídas que tiene un usuario, útil para mostrar un badge en la UI."
    )
    @ApiResponse(responseCode = "200", description = "Cantidad de notificaciones no leídas")
    public ResponseEntity<Long> contarNoLeidasPorUsuario(
            @Parameter(description = "ID del usuario", example = "10")
            @PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.contarNoLeidasPorUsuario(userId));
    }

    @GetMapping("/estado/{estado}")
    @Operation(
            summary = "Listar notificaciones por estado",
            description = "Devuelve todas las notificaciones que se encuentran en un estado específico, por ejemplo 'PENDIENTE', 'ENVIADA' o 'FALLIDA'."
    )
    @ApiResponse(responseCode = "200", description = "Lista de notificaciones en el estado indicado")
    public ResponseEntity<List<NotificationResponse>> listarPorEstado(
            @Parameter(description = "Estado de la notificación", example = "PENDIENTE")
            @PathVariable String estado) {
        return ResponseEntity.ok(notificationService.listarPorEstado(estado));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar una notificación",
            description = "Modifica los datos de una notificación existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificación actualizada correctamente",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe una notificación con ese ID")
    })
    public ResponseEntity<NotificationResponse> actualizar(
            @Parameter(description = "ID de la notificación a actualizar", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody NotificationUpdateRequest request) {
        return ResponseEntity.ok(notificationService.actualizar(id, request));
    }

    @PutMapping("/{id}/leer")
    @Operation(
            summary = "Marcar una notificación como leída",
            description = "Actualiza el estado de la notificación indicada para marcarla como leída."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificación marcada como leída",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe una notificación con ese ID")
    })
    public ResponseEntity<NotificationResponse> marcarComoLeida(
            @Parameter(description = "ID de la notificación", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.marcarComoLeida(id));
    }

    @PutMapping("/{id}/enviar")
    @Operation(
            summary = "Marcar una notificación como enviada",
            description = "Actualiza el estado de la notificación indicada a 'ENVIADA', normalmente luego de confirmarse su entrega."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificación marcada como enviada",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe una notificación con ese ID")
    })
    public ResponseEntity<NotificationResponse> marcarComoEnviada(
            @Parameter(description = "ID de la notificación", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.marcarComoEnviada(id));
    }

    @PutMapping("/{id}/fallar")
    @Operation(
            summary = "Marcar una notificación como fallida",
            description = "Actualiza el estado de la notificación indicada a 'FALLIDA', normalmente cuando el envío no pudo completarse."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificación marcada como fallida",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe una notificación con ese ID")
    })
    public ResponseEntity<NotificationResponse> marcarComoFallida(
            @Parameter(description = "ID de la notificación", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.marcarComoFallida(id));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar una notificación",
            description = "Elimina de forma permanente una notificación según su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notificación eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe una notificación con ese ID")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la notificación a eliminar", example = "1")
            @PathVariable Long id) {
        notificationService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
