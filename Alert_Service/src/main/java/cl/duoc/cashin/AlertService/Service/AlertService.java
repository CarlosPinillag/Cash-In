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

@Service // registra esta clase como Bean de lógica de negocio
@RequiredArgsConstructor // Lombok genera constructor con los atributos 'final'

public class AlertService {

    // Logger SLF4J — siempre usar esto, NUNCA System.out.println
    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AlertRepository alertRepository;
    private final UserServiceClient userServiceClient;

    // ── MAPPER privado: AlertModel → AlertResponse ───────────────────
    // Convierte la entidad JPA en el DTO que se devuelve al cliente
    // Es privado porque solo el service lo usa
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

    // ── CREAR ───────────────────────────────────────────────────────────
    // Llamado por budget-service cuando un presupuesto supera el 80% o 100%
    public AlertResponse crear(AlertRequest request) {
        log.info("Creando alerta tipo: {} para userId: {} — budgetId: {}",
                request.getTipo(), request.getUserId(), request.getBudgetId());

        // Regla 1: Validar que el usuario existe en user-service
        // Si retorna 404 → ResourceNotFoundException propagada automáticamente
        userServiceClient.obtenerUsuarioPorId(request.getUserId());
        log.info("Usuario id: {} validado en user-service", request.getUserId());

        // Regla 2: Evitar duplicar alertas del mismo tipo para el mismo presupuesto
        // Si ya existe una ALERTA_80 no leída para el mismo budgetId, no crear otra
        boolean duplicada = alertRepository.findByBudgetId(request.getBudgetId())
                .stream()
                .anyMatch(a -> a.getTipo().equals(request.getTipo()) && !a.getLeida());

        if (duplicada) {
            throw new RuntimeException(
                    "Ya existe una alerta activa de tipo " + request.getTipo()
                    + " para el presupuesto id " + request.getBudgetId()
                    + ". Márcala como leída antes de crear una nueva.");
        }

        // Construir la entidad con todos los campos
        AlertModel model = new AlertModel();
        model.setUserId(request.getUserId());
        model.setBudgetId(request.getBudgetId());
        model.setTipo(request.getTipo());
        model.setMensaje(request.getMensaje());
        model.setLeida(false);              // toda alerta nueva nace sin leer
        model.setFechaCreacion(LocalDate.now());

        AlertModel guardada = alertRepository.save(model);
        log.info("Alerta creada con id: {} — tipo: {} para userId: {}",
                guardada.getIdAlert(), guardada.getTipo(), guardada.getUserId());

        return mapToResponse(guardada);
    }

    // ── OBTENER POR ID ───────────────────────────────────────────────────
    public AlertResponse obtenerPorId(Long id) {
        log.info("Buscando alerta id: {}", id);

        AlertModel model = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alerta con id " + id + " no encontrada"));
        // findById retorna Optional<AlertModel>
        // orElseThrow: si el Optional está vacío, lanza la excepción
        // GlobalExceptionHandler la captura → HTTP 404

        return mapToResponse(model);
    }

    // ── LISTAR POR USUARIO ───────────────────────────────────────────────
    // Retorna todas las alertas del usuario, más recientes primero
    public List<AlertResponse> listarPorUsuario(Long userId) {
        log.info("Listando alertas del usuario id: {}", userId);

        return alertRepository.findByUserIdOrderByFechaCreacionDesc(userId)
                .stream()                       // convierte List en Stream
                .map(this::mapToResponse)       // aplica mapToResponse a cada elemento
                .collect(Collectors.toList());  // vuelve a convertir en List
    }

    // ── LISTAR NO LEÍDAS POR USUARIO ────────────────────────────────────
    // Retorna solo las alertas pendientes de lectura — útil para el panel de notificaciones
    public List<AlertResponse> listarNoLeidasPorUsuario(Long userId) {
        log.info("Listando alertas no leídas del usuario id: {}", userId);

        return alertRepository.findByUserIdAndLeidaFalse(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── MARCAR COMO LEÍDA ────────────────────────────────────────────────
    // Endpoint principal de interacción del usuario con sus alertas
    // PUT /api/v1/alerts/{id}/leer
    public AlertResponse marcarComoLeida(Long id) {
        log.info("Marcando alerta id: {} como leída", id);

        AlertModel model = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alerta con id " + id + " no encontrada"));

        // Regla de negocio: no tiene sentido marcar como leída una alerta ya leída
        if (model.getLeida()) {
            throw new RuntimeException("La alerta con id " + id + " ya fue marcada como leída");
        }

        model.setLeida(true);
        AlertModel actualizada = alertRepository.save(model);
        log.info("Alerta id: {} marcada como leída exitosamente", id);

        return mapToResponse(actualizada);
    }

    // ── CONTAR NO LEÍDAS ─────────────────────────────────────────────────
    // Retorna el contador de alertas pendientes — útil para badges de notificación
    public Long contarNoLeidasPorUsuario(Long userId) {
        log.info("Contando alertas no leídas del usuario id: {}", userId);

        Long total = alertRepository.contarNoLeidasPorUsuario(userId);
        log.info("Usuario id: {} tiene {} alertas no leídas", userId, total);
        return total;
    }

    // ── ELIMINAR ─────────────────────────────────────────────────────────
    // Eliminación física — útil para limpiar alertas antiguas
    public void eliminar(Long id) {
        log.info("Eliminando alerta id: {}", id);

        if (!alertRepository.existsById(id)) {
            throw new ResourceNotFoundException("Alerta con id " + id + " no existe");
        }

        alertRepository.deleteById(id);
        log.info("Alerta id: {} eliminada exitosamente", id);
    }
}
