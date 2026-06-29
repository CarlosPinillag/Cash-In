package cl.duoc.cashin.IncomeService.Controller;

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

import cl.duoc.cashin.IncomeService.Service.IncomeService;
import cl.duoc.cashin.IncomeService.dto.Request.IncomeRequest;
import cl.duoc.cashin.IncomeService.dto.Request.IncomeUpdateRequest;
import cl.duoc.cashin.IncomeService.dto.Response.IncomeResponse;
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
@RequestMapping("/api/v1/incomes")
@RequiredArgsConstructor
@Tag(name = "Ingresos", description = "Operaciones de registro y consulta de ingresos del usuario")
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping
    @Operation(
            summary = "Crear un ingreso",
            description = "Registra un nuevo ingreso asociado al usuario autenticado mediante el token JWT enviado en el header Authorization."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingreso creado correctamente",
                    content = @Content(schema = @Schema(implementation = IncomeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de la solicitud inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    public ResponseEntity<IncomeResponse> crear(
            @Parameter(description = "Token JWT con formato 'Bearer {token}'", required = true)
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody IncomeRequest request) {
        return ResponseEntity.ok(incomeService.crear(request, authHeader));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener un ingreso por ID",
            description = "Devuelve el detalle de un ingreso específico según su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingreso encontrado",
                    content = @Content(schema = @Schema(implementation = IncomeResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un ingreso con ese ID")
    })
    public ResponseEntity<IncomeResponse> obtenerPorId(
            @Parameter(description = "ID del ingreso", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(incomeService.obtenerPorId(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Listar ingresos de un usuario",
            description = "Devuelve todos los ingresos registrados para un usuario específico."
    )
    @ApiResponse(responseCode = "200", description = "Lista de ingresos del usuario")
    public ResponseEntity<List<IncomeResponse>> listarPorUsuario(
            @Parameter(description = "ID del usuario", example = "10")
            @PathVariable Long userId) {
        return ResponseEntity.ok(incomeService.listarPorUsuario(userId));
    }

    @GetMapping("/user/{userId}/total")
    @Operation(
            summary = "Total de ingresos de un usuario",
            description = "Calcula la suma total de todos los ingresos registrados por el usuario."
    )
    @ApiResponse(responseCode = "200", description = "Total calculado correctamente")
    public ResponseEntity<Double> obtenerTotalPorUsuario(
            @Parameter(description = "ID del usuario", example = "10")
            @PathVariable Long userId) {
        return ResponseEntity.ok(incomeService.obtenerTotalPorUsuario(userId));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar un ingreso",
            description = "Modifica los datos de un ingreso existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingreso actualizado correctamente",
                    content = @Content(schema = @Schema(implementation = IncomeResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un ingreso con ese ID")
    })
    public ResponseEntity<IncomeResponse> actualizar(
            @Parameter(description = "ID del ingreso a actualizar", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody IncomeUpdateRequest request) {
        return ResponseEntity.ok(incomeService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar un ingreso",
            description = "Elimina de forma permanente un ingreso según su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ingreso eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe un ingreso con ese ID")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del ingreso a eliminar", example = "1")
            @PathVariable Long id) {
        incomeService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
