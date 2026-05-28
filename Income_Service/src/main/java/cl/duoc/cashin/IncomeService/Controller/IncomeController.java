package cl.duoc.cashin.IncomeService.Controller;

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

import cl.duoc.cashin.IncomeService.Service.IncomeService;
import cl.duoc.cashin.IncomeService.dto.Request.IncomeRequest;
import cl.duoc.cashin.IncomeService.dto.Request.IncomeUpdateRequest;
import cl.duoc.cashin.IncomeService.dto.Response.IncomeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/incomes")
@RequiredArgsConstructor

public class IncomeController {

    private final IncomeService incomeService;

    // Registrar nuevo ingreso
    @PostMapping
    public ResponseEntity<IncomeResponse> crear(
            @Valid @RequestBody IncomeRequest request) {
        return ResponseEntity.ok(incomeService.crear(request));
    }

    // Obtener detalle de un ingreso
    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(incomeService.obtenerPorId(id));
    }

    // Listar todos los ingresos de un usuario
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<IncomeResponse>> listarPorUsuario(@PathVariable Long userId) {
        return ResponseEntity.ok(incomeService.listarPorUsuario(userId));
    }

    // — Total de ingresos por usuario
    @GetMapping("/user/{userId}/total")
    public ResponseEntity<Double> obtenerTotalPorUsuario(@PathVariable Long userId) {
        return ResponseEntity.ok(incomeService.obtenerTotalPorUsuario(userId));
    }

    // Actualizar un ingreso existente
    @PutMapping("/{id}")
    public ResponseEntity<IncomeResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody IncomeUpdateRequest request) {
        return ResponseEntity.ok(incomeService.actualizar(id, request));
    }

    // — Eliminar un ingreso
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        incomeService.eliminar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 sin body
    }
}
