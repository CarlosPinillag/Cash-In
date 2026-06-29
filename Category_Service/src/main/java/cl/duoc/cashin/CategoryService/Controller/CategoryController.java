package cl.duoc.cashin.CategoryService.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.cashin.CategoryService.Service.CategoryService;
import cl.duoc.cashin.CategoryService.dto.Request.CategoryRequest;
import cl.duoc.cashin.CategoryService.dto.Request.CategoryUpdateRequest;
import cl.duoc.cashin.CategoryService.dto.Response.CategoryResponse;
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
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "Operaciones de gestión de categorías financieras (ingresos y gastos)")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(
            summary = "Crear una categoría",
            description = "Registra una nueva categoría financiera (de ingreso o gasto)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoría creada correctamente",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de la solicitud inválidos")
    })
    public ResponseEntity<CategoryResponse> crear(
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.crear(request));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener una categoría por ID",
            description = "Devuelve el detalle de una categoría específica según su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría encontrada",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe una categoría con ese ID")
    })
    public ResponseEntity<CategoryResponse> obtenerPorId(
            @Parameter(description = "ID de la categoría", example = "1")
            @PathVariable Long id) {
        // @PathVariable extrae el valor {id} de la URL
        return ResponseEntity.ok(categoryService.obtenerPorId(id));
    }

    @GetMapping
    @Operation(
            summary = "Listar categorías",
            description = "Devuelve todas las categorías. Si se indica el parámetro 'activo', filtra solo las categorías activas o inactivas."
    )
    @ApiResponse(responseCode = "200", description = "Lista de categorías")
    public ResponseEntity<List<CategoryResponse>> listar(
            @Parameter(description = "Filtra por estado activo (true) o inactivo (false). Si se omite, devuelve todas.")
            @RequestParam(required = false) Boolean activo) {
        // @RequestParam(required = false) → si no viene en la URL, activo = null
        if (activo != null) {
            return ResponseEntity.ok(categoryService.listarPorActivo(activo));
        }
        return ResponseEntity.ok(categoryService.listarTodas());
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(
            summary = "Listar categorías por tipo",
            description = "Devuelve las categorías filtradas por tipo, por ejemplo 'INGRESO' o 'GASTO'."
    )
    @ApiResponse(responseCode = "200", description = "Lista de categorías del tipo indicado")
    public ResponseEntity<List<CategoryResponse>> listarPorTipo(
            @Parameter(description = "Tipo de categoría", example = "GASTO")
            @PathVariable String tipo) {
        return ResponseEntity.ok(categoryService.listarPorTipo(tipo));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar una categoría",
            description = "Modifica los datos de una categoría existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría actualizada correctamente",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe una categoría con ese ID")
    })
    public ResponseEntity<CategoryResponse> actualizar(
            @Parameter(description = "ID de la categoría a actualizar", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request) {
        return ResponseEntity.ok(categoryService.actualizar(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(
            summary = "Desactivar una categoría",
            description = "Marca una categoría como inactiva sin eliminarla físicamente, para que deje de estar disponible en nuevos registros."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoría desactivada correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe una categoría con ese ID")
    })
    public ResponseEntity<Void> desactivar(
            @Parameter(description = "ID de la categoría a desactivar", example = "1")
            @PathVariable Long id) {
        categoryService.desactivar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 sin body
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar una categoría",
            description = "Elimina de forma permanente una categoría según su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoría eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe una categoría con ese ID")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la categoría a eliminar", example = "1")
            @PathVariable Long id) {
        categoryService.eliminar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 sin body
    }
}
