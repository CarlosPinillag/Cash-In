package cl.duoc.cashin.ExpenseService.Controller;

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

import cl.duoc.cashin.ExpenseService.Service.ExpenseService;
import cl.duoc.cashin.ExpenseService.dto.Request.ExpenseRequest;
import cl.duoc.cashin.ExpenseService.dto.Request.ExpenseUpdateRequest;
import cl.duoc.cashin.ExpenseService.dto.Response.ExpenseResponse;
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

@RequestMapping("/api/v1/expenses")

@RequiredArgsConstructor

@Tag(name = "Gastos", description = "Operaciones de registro y consulta de gastos del usuario")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @Operation(
            summary = "Crear un gasto",
            description = "Registra un nuevo gasto asociado al usuario autenticado mediante el token JWT enviado en el header Authorization."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gasto creado correctamente",
                    content = @Content(schema = @Schema(implementation = ExpenseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de la solicitud inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    public ResponseEntity<ExpenseResponse> crear(
            @Parameter(description = "Token JWT con formato 'Bearer {token}'", required = true)
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ExpenseRequest request) {

        return ResponseEntity.ok(expenseService.crear(request, authHeader));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener un gasto por ID",
            description = "Devuelve el detalle de un gasto específico según su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gasto encontrado",
                    content = @Content(schema = @Schema(implementation = ExpenseResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un gasto con ese ID")
    })
    public ResponseEntity<ExpenseResponse> obtenerPorId(
            @Parameter(description = "ID del gasto", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(expenseService.obtenerPorId(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Listar gastos de un usuario",
            description = "Devuelve todos los gastos registrados para un usuario específico."
    )
    @ApiResponse(responseCode = "200", description = "Lista de gastos del usuario")
    public ResponseEntity<List<ExpenseResponse>> listarPorUsuario(
            @Parameter(description = "ID del usuario", example = "10")
            @PathVariable Long userId) {
        return ResponseEntity.ok(expenseService.listarPorUsuario(userId));
    }

    @GetMapping("/user/{userId}/total")
    @Operation(
            summary = "Total de gastos de un usuario",
            description = "Calcula la suma total de todos los gastos registrados por el usuario."
    )
    @ApiResponse(responseCode = "200", description = "Total calculado correctamente")
    public ResponseEntity<Double> obtenerTotalPorUsuario(
            @Parameter(description = "ID del usuario", example = "10")
            @PathVariable Long userId) {
        return ResponseEntity.ok(expenseService.obtenerTotalPorUsuario(userId));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar un gasto",
            description = "Modifica los datos de un gasto existente. Requiere token JWT válido."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gasto actualizado correctamente",
                    content = @Content(schema = @Schema(implementation = ExpenseResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un gasto con ese ID"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    public ResponseEntity<ExpenseResponse> actualizar(
            @Parameter(description = "ID del gasto a actualizar", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Token JWT con formato 'Bearer {token}'", required = true)
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ExpenseUpdateRequest request) {
        return ResponseEntity.ok(expenseService.actualizar(id, request, authHeader));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar un gasto",
            description = "Elimina de forma permanente un gasto según su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Gasto eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe un gasto con ese ID")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del gasto a eliminar", example = "1")
            @PathVariable Long id) {
        expenseService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
