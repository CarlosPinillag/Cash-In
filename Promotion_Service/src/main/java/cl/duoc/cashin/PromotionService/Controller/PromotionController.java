package cl.duoc.cashin.PromotionService.Controller;

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

import cl.duoc.cashin.PromotionService.Service.PromotionService;
import cl.duoc.cashin.PromotionService.dto.Request.PromotionAplicarRequest;
import cl.duoc.cashin.PromotionService.dto.Request.PromotionRequest;
import cl.duoc.cashin.PromotionService.dto.Request.PromotionUpdateRequest;
import cl.duoc.cashin.PromotionService.dto.Response.PromotionAplicarResponse;
import cl.duoc.cashin.PromotionService.dto.Response.PromotionResponse;
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
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
@Tag(name = "Promociones", description = "Operaciones de gestión y aplicación de promociones y recomendaciones de ahorro")
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    @Operation(
            summary = "Crear una promoción",
            description = "Registra una nueva promoción asociada al usuario autenticado mediante el token JWT enviado en el header Authorization."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Promoción creada correctamente",
                    content = @Content(schema = @Schema(implementation = PromotionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de la solicitud inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    public ResponseEntity<PromotionResponse> crear(
            @Parameter(description = "Token JWT con formato 'Bearer {token}'", required = true)
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PromotionRequest request) {
        
        
        PromotionResponse response = promotionService.crear(request, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); 
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener una promoción por ID",
            description = "Devuelve el detalle de una promoción específica según su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promoción encontrada",
                    content = @Content(schema = @Schema(implementation = PromotionResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe una promoción con ese ID")
    })
    public ResponseEntity<PromotionResponse> obtenerPorId(
            @Parameter(description = "ID de la promoción", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(promotionService.obtenerPorId(id)); 
    }

    @GetMapping("/codigo/{codigo}")
    @Operation(
            summary = "Obtener una promoción por código",
            description = "Devuelve el detalle de una promoción a partir de su código único."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promoción encontrada",
                    content = @Content(schema = @Schema(implementation = PromotionResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe una promoción con ese código")
    })
    public ResponseEntity<PromotionResponse> obtenerPorCodigo(
            @Parameter(description = "Código de la promoción", example = "AHORRO10")
            @PathVariable String codigo) {
        return ResponseEntity.ok(promotionService.obtenerPorCodigo(codigo)); 
    }

    @GetMapping
    @Operation(
            summary = "Listar todas las promociones",
            description = "Devuelve el listado completo de promociones registradas, activas e inactivas."
    )
    @ApiResponse(responseCode = "200", description = "Lista de promociones")
    public ResponseEntity<List<PromotionResponse>> listarTodas() {
        return ResponseEntity.ok(promotionService.listarTodas()); 
    }

    @GetMapping("/activas")
    @Operation(
            summary = "Listar promociones activas",
            description = "Devuelve únicamente las promociones que se encuentran vigentes actualmente."
    )
    @ApiResponse(responseCode = "200", description = "Lista de promociones activas")
    public ResponseEntity<List<PromotionResponse>> listarActivas() {
        
        return ResponseEntity.ok(promotionService.listarActivas()); 
    }

    @GetMapping("/activas/categoria")
    @Operation(
            summary = "Listar promociones activas por categoría",
            description = "Devuelve las promociones activas filtradas por una categoría financiera específica."
    )
    @ApiResponse(responseCode = "200", description = "Lista de promociones activas de la categoría")
    public ResponseEntity<List<PromotionResponse>> listarActivasPorCategoria(
            @Parameter(description = "ID de la categoría", example = "3")
            @RequestParam Long categoryId) {
        
        return ResponseEntity.ok(promotionService.listarActivasPorCategoria(categoryId)); 
    }

    @PostMapping("/aplicar")
    @Operation(
            summary = "Aplicar una promoción",
            description = "Aplica una promoción al usuario autenticado, por ejemplo mediante un código de descuento."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promoción aplicada correctamente",
                    content = @Content(schema = @Schema(implementation = PromotionAplicarResponse.class))),
            @ApiResponse(responseCode = "400", description = "La promoción no es válida o ya fue utilizada"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    public ResponseEntity<PromotionAplicarResponse> aplicar(
            @Parameter(description = "Token JWT con formato 'Bearer {token}'", required = true)
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PromotionAplicarRequest request) {
        
        
        PromotionAplicarResponse response = promotionService.aplicar(request, authHeader);
        return ResponseEntity.ok(response); 
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar una promoción",
            description = "Modifica los datos de una promoción existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promoción actualizada correctamente",
                    content = @Content(schema = @Schema(implementation = PromotionResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe una promoción con ese ID")
    })
    public ResponseEntity<PromotionResponse> actualizar(
            @Parameter(description = "ID de la promoción a actualizar", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody PromotionUpdateRequest request) {
        return ResponseEntity.ok(promotionService.actualizar(id, request)); 
    }

    @PutMapping("/{id}/desactivar")
    @Operation(
            summary = "Desactivar una promoción",
            description = "Marca una promoción como inactiva, sin eliminarla físicamente, para que deje de aplicarse a nuevos usuarios."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promoción desactivada correctamente",
                    content = @Content(schema = @Schema(implementation = PromotionResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe una promoción con ese ID")
    })
    public ResponseEntity<PromotionResponse> desactivar(
            @Parameter(description = "ID de la promoción a desactivar", example = "1")
            @PathVariable Long id) {
        
        return ResponseEntity.ok(promotionService.desactivar(id)); 
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar una promoción",
            description = "Elimina de forma permanente una promoción según su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Promoción eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe una promoción con ese ID")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la promoción a eliminar", example = "1")
            @PathVariable Long id) {
        promotionService.eliminar(id);
        return ResponseEntity.noContent().build(); 
    }
}

