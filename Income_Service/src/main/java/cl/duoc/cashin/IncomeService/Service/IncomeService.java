package cl.duoc.cashin.IncomeService.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cl.duoc.cashin.IncomeService.Client.UserServiceClient;
import cl.duoc.cashin.IncomeService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.IncomeService.Model.IncomeModel;
import cl.duoc.cashin.IncomeService.Repository.IncomeRepository;
import cl.duoc.cashin.IncomeService.dto.Request.IncomeRequest;
import cl.duoc.cashin.IncomeService.dto.Request.IncomeUpdateRequest;
import cl.duoc.cashin.IncomeService.dto.Response.IncomeResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private static final Logger log = LoggerFactory.getLogger(IncomeService.class);

    private final IncomeRepository incomeRepository;
    private final UserServiceClient userServiceClient;

    private IncomeResponse mapToResponse(IncomeModel model) {
        IncomeResponse response = new IncomeResponse();
        response.setIdIncome(model.getIdIncome());
        response.setUserId(model.getUserId());
        response.setMonto(model.getMonto());
        response.setDescripcion(model.getDescripcion());
        response.setCategoria(model.getCategoria());
        response.setFecha(model.getFecha());
        response.setRecurrente(model.getRecurrente());
        response.setFrecuencia(model.getFrecuencia());
        return response;
    }

    public IncomeResponse crear(IncomeRequest request, String authHeader) {
        log.info("Creando ingreso para userId: {} categoria: {}",
                request.getUserId(), request.getCategoria());

        userServiceClient.obtenerUsuarioPorId(request.getUserId(), authHeader);
        log.info("Usuario id: {} validado en user-service", request.getUserId());

        if (Boolean.TRUE.equals(request.getRecurrente())) {
            if (request.getFrecuencia() == null || request.getFrecuencia().isBlank()) {
                throw new RuntimeException(
                        "LA FRECUENCIA ES OBLIGATORIA CUANDO EL INGRESO ES RECURRENTE. " +
                        "Valores aceptados: MENSUAL, SEMANAL, QUINCENAL");
            }
            String frecuencia = request.getFrecuencia().toUpperCase();
            if (!frecuencia.equals("MENSUAL") && !frecuencia.equals("SEMANAL") && !frecuencia.equals("QUINCENAL")) {
                throw new RuntimeException(
                        "FRECUENCIA INVALIDA: " + request.getFrecuencia() +
                        ". Valores aceptados: MENSUAL, SEMANAL, QUINCENAL");
            }
        }

        IncomeModel model = new IncomeModel();
        model.setUserId(request.getUserId());
        model.setMonto(request.getMonto());
        model.setDescripcion(request.getDescripcion());
        model.setCategoria(request.getCategoria().toUpperCase());
        model.setFecha(request.getFecha());
        model.setRecurrente(request.getRecurrente());
        model.setFrecuencia(Boolean.TRUE.equals(request.getRecurrente())
                ? request.getFrecuencia().toUpperCase()
                : null);

        IncomeModel guardado = incomeRepository.save(model);
        log.info("Ingreso creado con id: {}", guardado.getIdIncome());
        return mapToResponse(guardado);
    }

    public IncomeResponse obtenerPorId(Long id) {
        log.info("Buscando ingreso id: {}", id);

        IncomeModel model = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingreso con id " + id + " no encontrado"));

        return mapToResponse(model);
    }

    public List<IncomeResponse> listarPorUsuario(Long userId) {
        log.info("Listando ingresos del usuario id: {}", userId);

        return incomeRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Double obtenerTotalPorUsuario(Long userId) {
        log.info("Calculando total de ingresos para userId: {}", userId);

        Double total = incomeRepository.sumMontoByUserId(userId);
        return total != null ? total : 0.0;
    }

    public IncomeResponse actualizar(Long id, IncomeUpdateRequest request) {
        log.info("Actualizando ingreso id: {}", id);

        IncomeModel model = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingreso con id " + id + " no encontrado"));

        if (request.getMonto() != null) {
            model.setMonto(request.getMonto());
        }
        if (request.getDescripcion() != null) {
            model.setDescripcion(request.getDescripcion());
        }
        if (request.getCategoria() != null) {
            model.setCategoria(request.getCategoria().toUpperCase());
        }
        if (request.getFecha() != null) {
            model.setFecha(request.getFecha());
        }
        if (request.getRecurrente() != null) {
            model.setRecurrente(request.getRecurrente());
        }
        if (request.getFrecuencia() != null) {
            model.setFrecuencia(request.getFrecuencia().toUpperCase());
        }

        if (Boolean.TRUE.equals(model.getRecurrente()) &&
                (model.getFrecuencia() == null || model.getFrecuencia().isBlank())) {
            throw new RuntimeException(
                    "LA FRECUENCIA ES OBLIGATORIA CUANDO EL INGRESO ES RECURRENTE");
        }

        if (Boolean.FALSE.equals(model.getRecurrente())) {
            model.setFrecuencia(null);
        }

        IncomeModel actualizado = incomeRepository.save(model);
        log.info("Ingreso id: {} actualizado exitosamente", id);
        return mapToResponse(actualizado);
    }

    public void eliminar(Long id) {
        log.info("Eliminando ingreso id: {}", id);

        if (!incomeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ingreso con id " + id + " no existe");
        }

        incomeRepository.deleteById(id);
        log.info("Ingreso id: {} eliminado exitosamente", id);
    }
}
