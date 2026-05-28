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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor

public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> crear(
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.crear(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> obtenerPorId(@PathVariable Long id) {
        // @PathVariable extrae el valor {id} de la URL
        return ResponseEntity.ok(categoryService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> listar(
            @RequestParam(required = false) Boolean activo) {
        // @RequestParam(required = false) → si no viene en la URL, activo = null
        if (activo != null) {
            return ResponseEntity.ok(categoryService.listarPorActivo(activo));
        }
        return ResponseEntity.ok(categoryService.listarTodas());
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<CategoryResponse>> listarPorTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(categoryService.listarPorTipo(tipo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request) {
        return ResponseEntity.ok(categoryService.actualizar(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        categoryService.desactivar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 sin body
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoryService.eliminar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 sin body
    }
}
