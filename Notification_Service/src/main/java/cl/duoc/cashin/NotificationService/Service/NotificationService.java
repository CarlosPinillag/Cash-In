package cl.duoc.cashin.NotificationService.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cl.duoc.cashin.NotificationService.Client.UserServiceClient;
import cl.duoc.cashin.NotificationService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.NotificationService.Model.NotificationModel;
import cl.duoc.cashin.NotificationService.Repository.NotificationRepository;
import cl.duoc.cashin.NotificationService.dto.Request.NotificationRequest;
import cl.duoc.cashin.NotificationService.dto.Request.NotificationUpdateRequest;
import cl.duoc.cashin.NotificationService.dto.Response.NotificationResponse;
import cl.duoc.cashin.NotificationService.dto.Response.UserRemoteResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserServiceClient userServiceClient;

    private NotificationResponse mapToResponse(NotificationModel model) {
        NotificationResponse response = new NotificationResponse();
        response.setIdNotification(model.getIdNotification());
        response.setUserId(model.getUserId());
        response.setCanal(model.getCanal());
        response.setTipo(model.getTipo());
        response.setTitulo(model.getTitulo());
        response.setMensaje(model.getMensaje());
        response.setEstado(model.getEstado());
        response.setLeida(model.getLeida());
        response.setFechaCreacion(model.getFechaCreacion());
        response.setFechaEnvio(model.getFechaEnvio());
        return response;
    }

    public NotificationResponse crear(NotificationRequest request, String authHeader) {
        log.info("Creando notificacion tipo: {} canal: {} para userId: {}",
                request.getTipo(), request.getCanal(), request.getUserId());

        UserRemoteResponse usuario = userServiceClient.obtenerUsuarioPorId(request.getUserId(), authHeader);
        log.info("Usuario id: {} ({} {}) validado en user-service",
                request.getUserId(), usuario.getNombre(), usuario.getApellido());

        if (!usuario.getActivo()) {
            throw new RuntimeException(
                    "El usuario con id " + request.getUserId() + " no está activo. No se pueden crear notificaciones.");
        }

        NotificationModel model = new NotificationModel();
        model.setUserId(request.getUserId());
        model.setCanal(request.getCanal());
        model.setTipo(request.getTipo());
        model.setTitulo(request.getTitulo());
        model.setMensaje(request.getMensaje());
        model.setEstado("PENDIENTE");
        model.setLeida(false);
        model.setFechaCreacion(LocalDate.now());
        model.setFechaEnvio(null);

        NotificationModel guardada = notificationRepository.save(model);
        log.info("Notificacion creada con id: {} — tipo: {} para userId: {}",
                guardada.getIdNotification(), guardada.getTipo(), guardada.getUserId());

        return mapToResponse(guardada);
    }

    public NotificationResponse obtenerPorId(Long id) {
        log.info("Buscando notificacion id: {}", id);

        NotificationModel model = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notificacion con id " + id + " no encontrada"));

        return mapToResponse(model);
    }

    public List<NotificationResponse> listarPorUsuario(Long userId) {
        log.info("Listando notificaciones del usuario id: {}", userId);

        return notificationRepository.findByUserIdOrderByFechaCreacionDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> listarNoLeidasPorUsuario(Long userId) {
        log.info("Listando notificaciones no leidas del usuario id: {}", userId);

        return notificationRepository.findByUserIdAndLeidaFalse(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> listarPorTipo(Long userId, String tipo) {
        log.info("Listando notificaciones tipo: {} del usuario id: {}", tipo, userId);

        return notificationRepository.findByUserIdAndTipo(userId, tipo)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> listarPorEstado(String estado) {
        log.info("Listando notificaciones en estado: {}", estado);

        return notificationRepository.findByEstado(estado)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public NotificationResponse marcarComoLeida(Long id) {
        log.info("Marcando notificacion id: {} como leida", id);

        NotificationModel model = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notificacion con id " + id + " no encontrada"));

        if (model.getLeida()) {
            throw new RuntimeException("La notificacion con id " + id + " ya fue marcada como leída");
        }

        model.setLeida(true);
        NotificationModel actualizada = notificationRepository.save(model);
        log.info("Notificacion id: {} marcada como leida exitosamente", id);

        return mapToResponse(actualizada);
    }

    public NotificationResponse marcarComoEnviada(Long id) {
        log.info("Marcando notificacion id: {} como enviada", id);

        NotificationModel model = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notificacion con id " + id + " no encontrada"));

        if (!model.getEstado().equals("PENDIENTE")) {
            throw new RuntimeException(
                    "Solo se puede marcar como ENVIADO una notificacion en estado PENDIENTE. Estado actual: " + model.getEstado());
        }

        model.setEstado("ENVIADO");
        model.setFechaEnvio(LocalDate.now());
        NotificationModel actualizada = notificationRepository.save(model);
        log.info("Notificacion id: {} marcada como ENVIADO — fechaEnvio: {}", id, actualizada.getFechaEnvio());

        return mapToResponse(actualizada);
    }

    public NotificationResponse marcarComoFallida(Long id) {
        log.info("Marcando notificacion id: {} como FALLIDO", id);

        NotificationModel model = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notificacion con id " + id + " no encontrada"));

        if (!model.getEstado().equals("PENDIENTE")) {
            throw new RuntimeException(
                    "Solo se puede marcar como FALLIDO una notificacion en estado PENDIENTE. Estado actual: " + model.getEstado());
        }

        model.setEstado("FALLIDO");
        NotificationModel actualizada = notificationRepository.save(model);
        log.warn("Notificacion id: {} marcada como FALLIDO — requiere reintento", id);

        return mapToResponse(actualizada);
    }

    public NotificationResponse actualizar(Long id, NotificationUpdateRequest request) {
        log.info("Actualizando notificacion id: {}", id);

        NotificationModel model = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notificacion con id " + id + " no encontrada"));

        if (request.getTitulo() != null) {
            model.setTitulo(request.getTitulo());
        }
        if (request.getMensaje() != null) {
            model.setMensaje(request.getMensaje());
        }
        if (request.getEstado() != null) {
            if (request.getEstado().equals("ENVIADO") && model.getFechaEnvio() == null) {
                model.setFechaEnvio(LocalDate.now());
            }
            model.setEstado(request.getEstado());
        }
        if (request.getLeida() != null) {
            model.setLeida(request.getLeida());
        }

        NotificationModel actualizada = notificationRepository.save(model);
        log.info("Notificacion id: {} actualizada exitosamente", id);
        return mapToResponse(actualizada);
    }

    public Long contarNoLeidasPorUsuario(Long userId) {
        log.info("Contando notificaciones no leidas del usuario id: {}", userId);

        Long total = notificationRepository.contarNoLeidasPorUsuario(userId);
        log.info("Usuario id: {} tiene {} notificaciones no leidas", userId, total);
        return total;
    }

    public void eliminar(Long id) {
        log.info("Eliminando notificacion id: {}", id);

        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notificacion con id " + id + " no existe");
        }

        notificationRepository.deleteById(id);
        log.info("Notificacion id: {} eliminada exitosamente", id);
    }
}