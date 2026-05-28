package cl.duoc.cashin.ExpenseService.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.cashin.ExpenseService.Service.ExpenseService;
import cl.duoc.cashin.ExpenseService.dto.Request.ExpenseRequest;
import cl.duoc.cashin.ExpenseService.dto.Request.ExpenseUpdateRequest;
import cl.duoc.cashin.ExpenseService.dto.Response.ExpenseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController

@RequestMapping("/api/v1/expenses")

@RequiredArgsConstructor

public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> crear(
            @Valid @RequestBody ExpenseRequest request) {

        return ResponseEntity.ok(expenseService.crear(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> obtenerPorId(@PathVariable Long id) {

        return ResponseEntity.ok(expenseService.obtenerPorId(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ExpenseResponse>> listarPorUsuario(@PathVariable Long userId) {
        return ResponseEntity.ok(expenseService.listarPorUsuario(userId));
    }

    @GetMapping("/user/{userId}/total")
    public ResponseEntity<Double> obtenerTotalPorUsuario(@PathVariable Long userId) {
        return ResponseEntity.ok(expenseService.obtenerTotalPorUsuario(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseUpdateRequest request) {
        return ResponseEntity.ok(expenseService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        expenseService.eliminar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 sin body
    }
}
