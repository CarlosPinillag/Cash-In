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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.cashin.PromotionService.Service.PromotionService;
import cl.duoc.cashin.PromotionService.dto.Request.PromotionAplicarRequest;
import cl.duoc.cashin.PromotionService.dto.Request.PromotionRequest;
import cl.duoc.cashin.PromotionService.dto.Request.PromotionUpdateRequest;
import cl.duoc.cashin.PromotionService.dto.Response.PromotionAplicarResponse;
import cl.duoc.cashin.PromotionService.dto.Response.PromotionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    public ResponseEntity<PromotionResponse> crear(
            @Valid @RequestBody PromotionRequest request) {
        // @Valid activa las validaciones del DTO (@NotBlank, @Pattern, @Positive, etc.)
        // @RequestBody lee el JSON del body de la peticion HTTP
        PromotionResponse response = promotionService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // HTTP 201
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.obtenerPorId(id)); // HTTP 200
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<PromotionResponse> obtenerPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(promotionService.obtenerPorCodigo(codigo)); // HTTP 200
    }

    @GetMapping
    public ResponseEntity<List<PromotionResponse>> listarTodas() {
        return ResponseEntity.ok(promotionService.listarTodas()); // HTTP 200
    }

    @GetMapping("/activas")
    public ResponseEntity<List<PromotionResponse>> listarActivas() {
        // Endpoint principal para mostrar promos disponibles al usuario
        return ResponseEntity.ok(promotionService.listarActivas()); // HTTP 200
    }

    @GetMapping("/activas/categoria")
    public ResponseEntity<List<PromotionResponse>> listarActivasPorCategoria(
            @RequestParam Long categoryId) {
        // @RequestParam lee el parametro de query string: ?categoryId=3
        return ResponseEntity.ok(promotionService.listarActivasPorCategoria(categoryId)); // HTTP 200
    }

    @PostMapping("/aplicar")
    public ResponseEntity<PromotionAplicarResponse> aplicar(
            @Valid @RequestBody PromotionAplicarRequest request) {
        // Endpoint de negocio central: valida el codigo, calcula el descuento y
        // registra el uso
        PromotionAplicarResponse response = promotionService.aplicar(request);
        return ResponseEntity.ok(response); // HTTP 200
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PromotionUpdateRequest request) {
        return ResponseEntity.ok(promotionService.actualizar(id, request)); // HTTP 200
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<PromotionResponse> desactivar(@PathVariable Long id) {
        // Setea activo=false sin eliminar el registro de la BD
        return ResponseEntity.ok(promotionService.desactivar(id)); // HTTP 200
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        promotionService.eliminar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 sin body
    }
}
