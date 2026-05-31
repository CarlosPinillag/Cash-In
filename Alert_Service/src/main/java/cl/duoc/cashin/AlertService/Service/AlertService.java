package cl.duoc.cashin.AlertService.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cl.duoc.cashin.AlertService.Client.UserServiceClient;
import cl.duoc.cashin.AlertService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.AlertService.Model.AlertModel;
import cl.duoc.cashin.AlertService.Repository.AlertRepository;
import cl.duoc.cashin.AlertService.dto.Request.AlertRequest;
import cl.duoc.cashin.AlertService.dto.Response.AlertResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AlertRepository alertRepository;
    private final UserServiceClient userServiceClient;

    private AlertResponse mapToResponse(AlertModel model) {
        AlertResponse response = new AlertResponse();
        response.setIdAlert(model.getIdAlert());
        response.setUserId(model.getUserId());
        response.setBudgetId(model.getBudgetId());
        response.setTipo(model.getTipo());
        response.setMensaje(model.getMensaje());
        response.setLeida(model.getLeida());
        response.setFechaCreacion(model.getFechaCreacion());
        return response;
    }

    public AlertResponse crear(AlertRequest request, String authHeader) {
        log.info("Creando alerta tipo: {} para userId: {} — budgetId: {}",
                request.getTipo(), request.getUserId(), request.getBudgetId());

        userServiceClient.obtenerUsuarioPorId(request.getUserId(), authHeader);
        log.info("Usuario id: {} validado en user-service", request.getUserId());

        boolean duplicada = alertRepository.findByBudgetId(request.getBudgetId())
                .stream()
                .anyMatch(a -> a.getTipo().equals(request.getTipo()) && !a.getLeida());

        if (duplicada) {
            throw new RuntimeException(
                    "Ya existe una alerta activa de tipo " + request.getTipo()
                            + " para el presupuesto id " + request.getBudgetId()
                            + ". Márcala como leída antes de crear una nueva.");
        }

        AlertModel model = new AlertModel();
        model.setUserId(request.getUserId());
        model.setBudgetId(request.getBudgetId());
        model.setTipo(request.getTipo());
        model.setMensaje(request.getMensaje());
        model.setLeida(false);
        model.setFechaCreacion(LocalDate.now());

        AlertModel guardada = alertRepository.save(model);
        log.info("Alerta creada con id: {} — tipo: {} para userId: {}",
                guardada.getIdAlert(), guardada.getTipo(), guardada.getUserId());

        return mapToResponse(guardada);
    }

    public AlertResponse obtenerPorId(Long id) {
        log.info("Buscando alerta id: {}", id);

        AlertModel model = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alerta con id " + id + " no encontrada"));

        return mapToResponse(model);
    }

    public List<AlertResponse> listarPorUsuario(Long userId) {
        log.info("Listando alertas del usuario id: {}", userId);

        return alertRepository.findByUserIdOrderByFechaCreacionDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AlertResponse> listarNoLeidasPorUsuario(Long userId) {
        log.info("Listando alertas no leídas del usuario id: {}", userId);

        return alertRepository.findByUserIdAndLeidaFalse(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AlertResponse marcarComoLeida(Long id) {
        log.info("Marcando alerta id: {} como leída", id);

        AlertModel model = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alerta con id " + id + " no encontrada"));

        if (model.getLeida()) {
            throw new RuntimeException("La alerta con id " + id + " ya fue marcada como leída");
        }

        model.setLeida(true);
        AlertModel actualizada = alertRepository.save(model);
        log.info("Alerta id: {} marcada como leída exitosamente", id);

        return mapToResponse(actualizada);
    }

    public Long contarNoLeidasPorUsuario(Long userId) {
        log.info("Contando alertas no leídas del usuario id: {}", userId);

        Long total = alertRepository.contarNoLeidasPorUsuario(userId);
        log.info("Usuario id: {} tiene {} alertas no leídas", userId, total);
        return total;
    }

    public void eliminar(Long id) {
        log.info("Eliminando alerta id: {}", id);

        if (!alertRepository.existsById(id)) {
            throw new ResourceNotFoundException("Alerta con id " + id + " no existe");
        }

        alertRepository.deleteById(id);
        log.info("Alerta id: {} eliminada exitosamente", id);
    }
}
