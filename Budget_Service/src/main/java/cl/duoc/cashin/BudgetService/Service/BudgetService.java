package cl.duoc.cashin.BudgetService.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cl.duoc.cashin.BudgetService.Client.AlertServiceClient;
import cl.duoc.cashin.BudgetService.Client.ExpenseServiceClient;
import cl.duoc.cashin.BudgetService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.BudgetService.Model.BudgetModel;
import cl.duoc.cashin.BudgetService.Repository.BudgetRepository;
import cl.duoc.cashin.BudgetService.dto.Request.BudgetRequest;
import cl.duoc.cashin.BudgetService.dto.Request.BudgetUpdateRequest;
import cl.duoc.cashin.BudgetService.dto.Response.BudgetResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class BudgetService {

    private static final Logger log = LoggerFactory.getLogger(BudgetService.class);

    private final BudgetRepository budgetRepository;
    private final ExpenseServiceClient expenseServiceClient;
    private final AlertServiceClient alertServiceClient;

    private BudgetResponse mapToResponse(BudgetModel model) {
        BudgetResponse response = new BudgetResponse();
        response.setIdBudget(model.getIdBudget());
        response.setUserId(model.getUserId());
        response.setCategoryId(model.getCategoryId());
        response.setMontoLimite(model.getMontoLimite());
        response.setPeriodo(model.getPeriodo());
        response.setActivo(model.getActivo());
        response.setPorcentajeUso(model.getPorcentajeUso());
        response.setFechaInicio(model.getFechaInicio());
        return response;
    }

    // ── CREAR
    public BudgetResponse crear(BudgetRequest request) {
        log.info("Creando presupuesto para userId: {} con periodo: {}",
                request.getUserId(), request.getPeriodo());

        // Regla 1:

        if (request.getCategoryId() != null) {
            Optional<BudgetModel> duplicado = budgetRepository
                    .findByUserIdAndCategoryIdAndPeriodoAndActivoTrue(
                            request.getUserId(), request.getCategoryId(), request.getPeriodo());

            if (duplicado.isPresent()) {
                throw new RuntimeException(
                        "Ya existe un presupuesto activo para el usuario id " + request.getUserId()
                                + " con categoria id " + request.getCategoryId()
                                + " y periodo " + request.getPeriodo());
            }
        } else {
            // Presupuesto global (sin categoría)
            Optional<BudgetModel> duplicadoGlobal = budgetRepository
                    .findByUserIdAndCategoryIdIsNullAndPeriodoAndActivoTrue(
                            request.getUserId(), request.getPeriodo());

            if (duplicadoGlobal.isPresent()) {
                throw new RuntimeException(
                        "Ya existe un presupuesto global activo para el usuario id " + request.getUserId()
                                + " con periodo " + request.getPeriodo());
            }
        }

        // entidad con todos los campos
        BudgetModel model = new BudgetModel();
        model.setUserId(request.getUserId());
        model.setCategoryId(request.getCategoryId());
        model.setMontoLimite(request.getMontoLimite());
        model.setPeriodo(request.getPeriodo());
        model.setActivo(true);
        model.setPorcentajeUso(0.0);
        model.setFechaInicio(request.getFechaInicio());

        BudgetModel guardado = budgetRepository.save(model);
        log.info("Presupuesto creado con id: {}", guardado.getIdBudget());
        return mapToResponse(guardado);
    }

    // ── OBTENER POR ID
    public BudgetResponse obtenerPorId(Long id) {
        log.info("Buscando presupuesto id: {}", id);

        BudgetModel model = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Presupuesto con id " + id + " no encontrado"));

        return mapToResponse(model);
    }

    // ── LISTAR POR USUARIO
    public List<BudgetResponse> listarPorUsuario(Long userId) {
        log.info("Listando presupuestos del usuario id: {}", userId);

        return budgetRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── SEGUIMIENTO:
    // Este endpoint llama a expense-service para calcular en tiempo real el
    // porcentaje usado
    // y dispara alertas a alert-service si se superan los umbrales (80% y 100%)
    public BudgetResponse obtenerSeguimiento(Long id) {
        log.info("Calculando seguimiento del presupuesto id: {}", id);

        BudgetModel model = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Presupuesto con id " + id + " no encontrado"));

        if (!model.getActivo()) {
            throw new RuntimeException("El presupuesto id " + id + " no esta activo");
        }

        // Paso 1:
        Double totalGastado = expenseServiceClient.obtenerTotalGastadoPorUsuario(model.getUserId());
        // Si no hay gastos, expense-service retorna 0.0
        log.info("Total gastado para userId {}: {}", model.getUserId(), totalGastado);

        // Paso 2: Calcular porcentaje de uso

        Double porcentajeUso = (totalGastado / model.getMontoLimite()) * 100;
        log.info("Porcentaje de uso del presupuesto id {}: {}%", id, porcentajeUso);

        // Paso 3: Disparar alertas si se superan los limites

        if (porcentajeUso >= 80 && porcentajeUso < 100) {
            log.info("Presupuesto id {} supero el 80%. Creando alerta ALERTA_80", id);
            try {
                alertServiceClient.crearAlerta(
                        model.getUserId(),
                        model.getIdBudget(),
                        "ALERTA_80",
                        "Has consumido el " + String.format("%.1f", porcentajeUso)
                                + "% de tu presupuesto de " + model.getPeriodo().toLowerCase()
                                + " ($" + model.getMontoLimite() + ")");
            } catch (RuntimeException e) {

                log.error("No se pudo crear la alerta ALERTA_80 para budgetId {}: {}", id, e.getMessage());
            }
        }

        // >= 100 (límite superado)
        if (porcentajeUso >= 100) {
            log.info("Presupuesto id {} supero el 100%. Creando alerta ALERTA_100", id);
            try {
                alertServiceClient.crearAlerta(
                        model.getUserId(),
                        model.getIdBudget(),
                        "ALERTA_100",
                        "Has superado tu presupuesto de " + model.getPeriodo().toLowerCase()
                                + " ($" + model.getMontoLimite() + "). Gastado: $" + totalGastado);
            } catch (RuntimeException e) {
                log.error("No se pudo crear la alerta ALERTA_100 para budgetId {}: {}", id, e.getMessage());
            }
        }

        // Paso 4: Actualizar porcentajeUso en la entidad y persistir
        model.setPorcentajeUso(porcentajeUso);
        BudgetModel actualizado = budgetRepository.save(model);
        log.info("Seguimiento del presupuesto id {} calculado: {}%", id, porcentajeUso);

        return mapToResponse(actualizado);
    }

    // ── ACTUALIZAR
    public BudgetResponse actualizar(Long id, BudgetUpdateRequest request) {
        log.info("Actualizando presupuesto id: {}", id);

        BudgetModel model = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Presupuesto con id " + id + " no encontrado"));

        // Actualizar solo los campos no-null
        if (request.getMontoLimite() != null) {
            model.setMontoLimite(request.getMontoLimite());
        }
        if (request.getPeriodo() != null) {
            model.setPeriodo(request.getPeriodo());
        }
        if (request.getActivo() != null) {
            model.setActivo(request.getActivo());
        }
        if (request.getFechaInicio() != null) {
            model.setFechaInicio(request.getFechaInicio());
        }

        BudgetModel actualizado = budgetRepository.save(model);
        log.info("Presupuesto id: {} actualizado exitosamente", id);
        return mapToResponse(actualizado);
    }

    // ── ELIMINAR
    public void eliminar(Long id) {
        log.info("Eliminando presupuesto id: {}", id);

        if (!budgetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Presupuesto con id " + id + " no existe");
        }

        budgetRepository.deleteById(id);
        log.info("Presupuesto id: {} eliminado exitosamente", id);
    }
}
