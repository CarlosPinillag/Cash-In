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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController

@RequestMapping("/api/v1/budgets")

@RequiredArgsConstructor

public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetResponse> crear(
            @Valid @RequestBody BudgetRequest request) {

        return ResponseEntity.ok(budgetService.crear(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> obtenerPorId(@PathVariable Long id) {

        return ResponseEntity.ok(budgetService.obtenerPorId(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BudgetResponse>> listarPorUsuario(@PathVariable Long userId) {
        return ResponseEntity.ok(budgetService.listarPorUsuario(userId));
    }

    @GetMapping("/{id}/seguimiento")
    public ResponseEntity<BudgetResponse> obtenerSeguimiento(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(budgetService.obtenerSeguimiento(id, authHeader));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody BudgetUpdateRequest request) {
        return ResponseEntity.ok(budgetService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        budgetService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
