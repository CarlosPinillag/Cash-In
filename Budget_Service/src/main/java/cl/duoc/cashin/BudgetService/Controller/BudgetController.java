package cl.duoc.cashin.BudgetService.Controller;

import java.util.List;

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

import cl.duoc.cashin.BudgetService.Service.BudgetService;
import cl.duoc.cashin.BudgetService.dto.Request.BudgetRequest;
import cl.duoc.cashin.BudgetService.dto.Request.BudgetUpdateRequest;
import cl.duoc.cashin.BudgetService.dto.Response.BudgetResponse;
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

@RequestMapping("/api/v1/budgets")

@RequiredArgsConstructor

@Tag(name = "Presupuestos", description = "Operaciones de creación, consulta y seguimiento de presupuestos del usuario")
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    @Operation(
            summary = "Crear un presupuesto",
            description = "Registra un nuevo presupuesto para una categoría y periodo determinados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presupuesto creado correctamente",
                    content = @Content(schema = @Schema(implementation = BudgetResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de la solicitud inválidos")
    })
    public ResponseEntity<BudgetResponse> crear(
            @Valid @RequestBody BudgetRequest request) {

        return ResponseEntity.ok(budgetService.crear(request));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener un presupuesto por ID",
            description = "Devuelve el detalle de un presupuesto específico según su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presupuesto encontrado",
                    content = @Content(schema = @Schema(implementation = BudgetResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un presupuesto con ese ID")
    })
    public ResponseEntity<BudgetResponse> obtenerPorId(
            @Parameter(description = "ID del presupuesto", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(budgetService.obtenerPorId(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Listar presupuestos de un usuario",
            description = "Devuelve todos los presupuestos registrados para un usuario específico."
    )
    @ApiResponse(responseCode = "200", description = "Lista de presupuestos del usuario")
    public ResponseEntity<List<BudgetResponse>> listarPorUsuario(
            @Parameter(description = "ID del usuario", example = "10")
            @PathVariable Long userId) {
        return ResponseEntity.ok(budgetService.listarPorUsuario(userId));
    }

    @GetMapping("/{id}/seguimiento")
    @Operation(
            summary = "Seguimiento de un presupuesto",
            description = "Devuelve el avance del presupuesto comparando el monto presupuestado con los gastos reales registrados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seguimiento calculado correctamente",
                    content = @Content(schema = @Schema(implementation = BudgetResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un presupuesto con ese ID"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    public ResponseEntity<BudgetResponse> obtenerSeguimiento(
            @Parameter(description = "ID del presupuesto", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Token JWT con formato 'Bearer {token}'", required = true)
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(budgetService.obtenerSeguimiento(id, authHeader));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar un presupuesto",
            description = "Modifica los datos de un presupuesto existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presupuesto actualizado correctamente",
                    content = @Content(schema = @Schema(implementation = BudgetResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un presupuesto con ese ID")
    })
    public ResponseEntity<BudgetResponse> actualizar(
            @Parameter(description = "ID del presupuesto a actualizar", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody BudgetUpdateRequest request) {
        return ResponseEntity.ok(budgetService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar un presupuesto",
            description = "Elimina de forma permanente un presupuesto según su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Presupuesto eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe un presupuesto con ese ID")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del presupuesto a eliminar", example = "1")
            @PathVariable Long id) {
        budgetService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
