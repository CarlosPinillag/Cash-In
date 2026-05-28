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

@Service // registra esta clase como Bean de lógica de negocio
@RequiredArgsConstructor // Lombok genera constructor con los atributos 'final'

public class NotificationService {

    // Logger SLF4J — siempre usar esto, NUNCA System.out.println
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserServiceClient userServiceClient;

    // ── MAPPER privado: NotificationModel → NotificationResponse ──────
    // Convierte la entidad JPA en el DTO que se devuelve al cliente
    // Es privado porque solo el service lo usa
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

    // ── CREAR ──────────────────────────────────────────────────────────
    // Llamado por otros microservicios (analytics, auth, expense, income) para
    // registrar una notificación destinada a un usuario
    public NotificationResponse crear(NotificationRequest request) {
        log.info("Creando notificacion tipo: {} canal: {} para userId: {}",
                request.getTipo(), request.getCanal(), request.getUserId());

        // Regla 1: Validar que el usuario existe en user-service
        // Si retorna 404 → ResourceNotFoundException propagada automáticamente
        UserRemoteResponse usuario = userServiceClient.obtenerUsuarioPorId(request.getUserId());
        log.info("Usuario id: {} ({} {}) validado en user-service",
                request.getUserId(), usuario.getNombre(), usuario.getApellido());

        // Regla 2: Verificar que el usuario está activo
        // No tiene sentido notificar a un usuario desactivado
        if (!usuario.getActivo()) {
            throw new RuntimeException(
                    "El usuario con id " + request.getUserId() + " no está activo. No se pueden crear notificaciones.");
        }

        // Construir la entidad con todos los campos
        NotificationModel model = new NotificationModel();
        model.setUserId(request.getUserId());
        model.setCanal(request.getCanal());
        model.setTipo(request.getTipo());
        model.setTitulo(request.getTitulo());
        model.setMensaje(request.getMensaje());
        model.setEstado("PENDIENTE"); // toda notificación nueva nace en estado PENDIENTE
        model.setLeida(false);        // nace sin leer
        model.setFechaCreacion(LocalDate.now());
        model.setFechaEnvio(null);    // se asigna cuando el estado pase a ENVIADO

        NotificationModel guardada = notificationRepository.save(model);
        log.info("Notificacion creada con id: {} — tipo: {} para userId: {}",
                guardada.getIdNotification(), guardada.getTipo(), guardada.getUserId());

        return mapToResponse(guardada);
    }

    // ── OBTENER POR ID ─────────────────────────────────────────────────
    public NotificationResponse obtenerPorId(Long id) {
        log.info("Buscando notificacion id: {}", id);

        NotificationModel model = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notificacion con id " + id + " no encontrada"));
        // findById retorna Optional<NotificationModel>
        // orElseThrow: si el Optional está vacío, lanza la excepción
        // GlobalExceptionHandler la captura → HTTP 404

        return mapToResponse(model);
    }

    // ── LISTAR POR USUARIO ─────────────────────────────────────────────
    // Retorna todas las notificaciones del usuario, más recientes primero
    public List<NotificationResponse> listarPorUsuario(Long userId) {
        log.info("Listando notificaciones del usuario id: {}", userId);

        return notificationRepository.findByUserIdOrderByFechaCreacionDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── LISTAR NO LEÍDAS POR USUARIO ───────────────────────────────────
    // Retorna solo las notificaciones pendientes de lectura
    public List<NotificationResponse> listarNoLeidasPorUsuario(Long userId) {
        log.info("Listando notificaciones no leidas del usuario id: {}", userId);

        return notificationRepository.findByUserIdAndLeidaFalse(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── LISTAR POR TIPO ────────────────────────────────────────────────
    // Filtra notificaciones de un usuario por tipo de evento
    public List<NotificationResponse> listarPorTipo(Long userId, String tipo) {
        log.info("Listando notificaciones tipo: {} del usuario id: {}", tipo, userId);

        return notificationRepository.findByUserIdAndTipo(userId, tipo)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── LISTAR POR ESTADO ──────────────────────────────────────────────
    // Endpoint administrativo: lista notificaciones en estado PENDIENTE o FALLIDO
    // Útil para disparar reenvíos masivos
    public List<NotificationResponse> listarPorEstado(String estado) {
        log.info("Listando notificaciones en estado: {}", estado);

        return notificationRepository.findByEstado(estado)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── MARCAR COMO LEÍDA ──────────────────────────────────────────────
    // Endpoint principal de interacción del usuario con su bandeja de notificaciones
    // PUT /api/v1/notifications/{id}/leer
    public NotificationResponse marcarComoLeida(Long id) {
        log.info("Marcando notificacion id: {} como leida", id);

        NotificationModel model = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notificacion con id " + id + " no encontrada"));

        // Regla de negocio: no reabrir una notificación ya leída
        if (model.getLeida()) {
            throw new RuntimeException("La notificacion con id " + id + " ya fue marcada como leída");
        }

        model.setLeida(true);
        NotificationModel actualizada = notificationRepository.save(model);
        log.info("Notificacion id: {} marcada como leida exitosamente", id);

        return mapToResponse(actualizada);
    }

    // ── MARCAR COMO ENVIADA ────────────────────────────────────────────
    // Cambia el estado de PENDIENTE a ENVIADO y registra la fecha de envío
    // Permite simular el envío efectivo del canal (email, push, etc.)
    public NotificationResponse marcarComoEnviada(Long id) {
        log.info("Marcando notificacion id: {} como enviada", id);

        NotificationModel model = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notificacion con id " + id + " no encontrada"));

        // Regla: solo se puede marcar como ENVIADO desde estado PENDIENTE
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

    // ── MARCAR COMO FALLIDA ────────────────────────────────────────────
    // Registra un fallo en el envío para poder reintentarlo después
    public NotificationResponse marcarComoFallida(Long id) {
        log.info("Marcando notificacion id: {} como FALLIDO", id);

        NotificationModel model = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notificacion con id " + id + " no encontrada"));

        // Regla: solo se puede marcar como FALLIDO desde estado PENDIENTE
        if (!model.getEstado().equals("PENDIENTE")) {
            throw new RuntimeException(
                    "Solo se puede marcar como FALLIDO una notificacion en estado PENDIENTE. Estado actual: " + model.getEstado());
        }

        model.setEstado("FALLIDO");
        NotificationModel actualizada = notificationRepository.save(model);
        log.warn("Notificacion id: {} marcada como FALLIDO — requiere reintento", id);

        return mapToResponse(actualizada);
    }

    // ── ACTUALIZAR ─────────────────────────────────────────────────────
    // Permite modificar campos editables (título, mensaje, estado, leída)
    public NotificationResponse actualizar(Long id, NotificationUpdateRequest request) {
        log.info("Actualizando notificacion id: {}", id);

        NotificationModel model = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notificacion con id " + id + " no encontrada"));

        // ── ACTUALIZAR SOLO CAMPOS NO NULL ──────────────────────────────
        if (request.getTitulo() != null) {
            model.setTitulo(request.getTitulo());
        }
        if (request.getMensaje() != null) {
            model.setMensaje(request.getMensaje());
        }
        if (request.getEstado() != null) {
            // Si se cambia manualmente a ENVIADO, registrar la fecha
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

    // ── CONTAR NO LEÍDAS ───────────────────────────────────────────────
    // Retorna el contador de notificaciones pendientes — útil para badges
    public Long contarNoLeidasPorUsuario(Long userId) {
        log.info("Contando notificaciones no leidas del usuario id: {}", userId);

        Long total = notificationRepository.contarNoLeidasPorUsuario(userId);
        log.info("Usuario id: {} tiene {} notificaciones no leidas", userId, total);
        return total;
    }

    // ── ELIMINAR ────────────────────────────────────────────────────────
    // Eliminación física de una notificación
    public void eliminar(Long id) {
        log.info("Eliminando notificacion id: {}", id);

        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notificacion con id " + id + " no existe");
        }

        notificationRepository.deleteById(id);
        log.info("Notificacion id: {} eliminada exitosamente", id);
    }
}
