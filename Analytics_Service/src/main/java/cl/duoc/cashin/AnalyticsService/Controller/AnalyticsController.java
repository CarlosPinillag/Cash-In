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
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Generación de análisis financieros y resúmenes estadísticos del usuario")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping
    @Operation(
            summary = "Generar un análisis financiero",
            description = "Solicita la generación de un nuevo análisis financiero para el usuario autenticado, consolidando datos de otros microservicios."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Análisis generado correctamente",
                    content = @Content(schema = @Schema(implementation = AnalyticsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de la solicitud inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    public ResponseEntity<AnalyticsResponse> generarAnalisis(
            @Valid @RequestBody AnalyticsRequest request,
            @Parameter(description = "Token JWT con formato 'Bearer {token}'", required = true)
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(analyticsService.generarAnalisis(request, authHeader)); // HTTP 201
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener un análisis por ID",
            description = "Devuelve el detalle de un análisis financiero específico según su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análisis encontrado",
                    content = @Content(schema = @Schema(implementation = AnalyticsResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un análisis con ese ID")
    })
    public ResponseEntity<AnalyticsResponse> obtenerPorId(
            @Parameter(description = "ID del análisis", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.obtenerPorId(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Historial de análisis de un usuario",
            description = "Devuelve todos los análisis financieros generados previamente para un usuario."
    )
    @ApiResponse(responseCode = "200", description = "Historial de análisis del usuario")
    public ResponseEntity<List<AnalyticsResponse>> obtenerHistorialPorUsuario(
            @Parameter(description = "ID del usuario", example = "10")
            @PathVariable Long userId) {
        return ResponseEntity.ok(analyticsService.obtenerHistorialPorUsuario(userId));
    }

    @GetMapping("/user/{userId}/resumen")
    @Operation(
            summary = "Resumen financiero de un usuario",
            description = "Devuelve un resumen consolidado de ingresos, gastos y presupuestos del usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen calculado correctamente",
                    content = @Content(schema = @Schema(implementation = ResumenFinancieroResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    public ResponseEntity<ResumenFinancieroResponse> obtenerResumenFinanciero(
            @Parameter(description = "ID del usuario", example = "10")
            @PathVariable Long userId,
            @Parameter(description = "Token JWT con formato 'Bearer {token}'", required = true)
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(analyticsService.obtenerResumenFinanciero(userId, authHeader));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar un análisis",
            description = "Elimina de forma permanente un análisis financiero según su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Análisis eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe un análisis con ese ID")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del análisis a eliminar", example = "1")
            @PathVariable Long id) {
        analyticsService.eliminar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 sin body
    }
}
