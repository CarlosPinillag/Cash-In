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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    public ResponseEntity<PromotionResponse> crear(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PromotionRequest request) {
        
        
        PromotionResponse response = promotionService.crear(request, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); 
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.obtenerPorId(id)); 
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<PromotionResponse> obtenerPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(promotionService.obtenerPorCodigo(codigo)); 
    }

    @GetMapping
    public ResponseEntity<List<PromotionResponse>> listarTodas() {
        return ResponseEntity.ok(promotionService.listarTodas()); 
    }

    @GetMapping("/activas")
    public ResponseEntity<List<PromotionResponse>> listarActivas() {
        
        return ResponseEntity.ok(promotionService.listarActivas()); 
    }

    @GetMapping("/activas/categoria")
    public ResponseEntity<List<PromotionResponse>> listarActivasPorCategoria(
            @RequestParam Long categoryId) {
        
        return ResponseEntity.ok(promotionService.listarActivasPorCategoria(categoryId)); 
    }

    @PostMapping("/aplicar")
    public ResponseEntity<PromotionAplicarResponse> aplicar(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PromotionAplicarRequest request) {
        
        
        PromotionAplicarResponse response = promotionService.aplicar(request, authHeader);
        return ResponseEntity.ok(response); 
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PromotionUpdateRequest request) {
        return ResponseEntity.ok(promotionService.actualizar(id, request)); 
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<PromotionResponse> desactivar(@PathVariable Long id) {
        
        return ResponseEntity.ok(promotionService.desactivar(id)); 
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        promotionService.eliminar(id);
        return ResponseEntity.noContent().build(); 
    }
}

