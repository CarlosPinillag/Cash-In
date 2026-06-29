package cl.duoc.cashin.NotificationService;

import cl.duoc.cashin.NotificationService.Client.UserServiceClient;
import cl.duoc.cashin.NotificationService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.NotificationService.Model.NotificationModel;
import cl.duoc.cashin.NotificationService.Repository.NotificationRepository;
import cl.duoc.cashin.NotificationService.Service.NotificationService;
import cl.duoc.cashin.NotificationService.dto.Request.NotificationRequest;
import cl.duoc.cashin.NotificationService.dto.Request.NotificationUpdateRequest;
import cl.duoc.cashin.NotificationService.dto.Response.NotificationResponse;
import cl.duoc.cashin.NotificationService.dto.Response.UserRemoteResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService – pruebas unitarias de lógica de negocio")
class NotificationServiceApplicationTests {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserServiceClient userServiceClient;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationModel notificationModel;
    private NotificationRequest notificationRequest;
    private UserRemoteResponse usuarioActivo;

    @BeforeEach
    void setUp() {
        notificationModel = new NotificationModel();
        notificationModel.setIdNotification(1L);
        notificationModel.setUserId(10L);
        notificationModel.setCanal("EMAIL");
        notificationModel.setTipo("ALERTA_PRESUPUESTO");
        notificationModel.setTitulo("Alerta de presupuesto");
        notificationModel.setMensaje("Has superado el 80% de tu presupuesto");
        notificationModel.setEstado("PENDIENTE");
        notificationModel.setLeida(false);
        notificationModel.setFechaCreacion(LocalDate.now());
        notificationModel.setFechaEnvio(null);

        usuarioActivo = new UserRemoteResponse();
        usuarioActivo.setIdUser(10L);
        usuarioActivo.setNombre("Juan");
        usuarioActivo.setApellido("Pérez");
        usuarioActivo.setEmail("juan@email.com");
        usuarioActivo.setActivo(true);

        notificationRequest = new NotificationRequest();
        notificationRequest.setUserId(10L);
        notificationRequest.setCanal("EMAIL");
        notificationRequest.setTipo("ALERTA_PRESUPUESTO");
        notificationRequest.setTitulo("Alerta de presupuesto");
        notificationRequest.setMensaje("Has superado el 80% de tu presupuesto");
    }

    // ── crear ────────────────────────────────────────────────────────

    @Test
    @DisplayName("crear: crea notificación con estado PENDIENTE y leida=false")
    void crear_usuarioActivo_retornaNotificacionPendiente() {
        when(userServiceClient.obtenerUsuarioPorId(10L, "Bearer token")).thenReturn(usuarioActivo);
        when(notificationRepository.save(any(NotificationModel.class))).thenReturn(notificationModel);

        NotificationResponse respuesta = notificationService.crear(notificationRequest, "Bearer token");

        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getEstado()).isEqualTo("PENDIENTE");
        assertThat(respuesta.getLeida()).isFalse();
        verify(notificationRepository).save(any(NotificationModel.class));
    }

    @Test
    @DisplayName("crear: lanza RuntimeException si el usuario está inactivo")
    void crear_usuarioInactivo_lanzaExcepcion() {
        usuarioActivo.setActivo(false);
        when(userServiceClient.obtenerUsuarioPorId(10L, "Bearer token")).thenReturn(usuarioActivo);

        assertThatThrownBy(() -> notificationService.crear(notificationRequest, "Bearer token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no está activo");

        verify(notificationRepository, never()).save(any());
    }

    // ── obtenerPorId ─────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerPorId: retorna notificación cuando existe")
    void obtenerPorId_existente_retornaNotificacion() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notificationModel));

        NotificationResponse respuesta = notificationService.obtenerPorId(1L);

        assertThat(respuesta.getIdNotification()).isEqualTo(1L);
        assertThat(respuesta.getTipo()).isEqualTo("ALERTA_PRESUPUESTO");
    }

    @Test
    @DisplayName("obtenerPorId: lanza ResourceNotFoundException si no existe")
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Notificacion con id 99 no encontrada");
    }

    // ── listarPorUsuario ──────────────────────────────────────────────

    @Test
    @DisplayName("listarPorUsuario: retorna lista de notificaciones del usuario")
    void listarPorUsuario_conNotificaciones_retornaLista() {
        when(notificationRepository.findByUserIdOrderByFechaCreacionDesc(10L))
                .thenReturn(List.of(notificationModel));

        List<NotificationResponse> resultado = notificationService.listarPorUsuario(10L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getUserId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("listarNoLeidasPorUsuario: retorna solo las no leídas")
    void listarNoLeidas_retornaNotificacionesPendientes() {
        when(notificationRepository.findByUserIdAndLeidaFalse(10L))
                .thenReturn(List.of(notificationModel));

        List<NotificationResponse> resultado = notificationService.listarNoLeidasPorUsuario(10L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getLeida()).isFalse();
    }

    @Test
    @DisplayName("listarPorTipo: retorna notificaciones filtradas por tipo")
    void listarPorTipo_conCoincidencias_retornaLista() {
        when(notificationRepository.findByUserIdAndTipo(10L, "ALERTA_PRESUPUESTO"))
                .thenReturn(List.of(notificationModel));

        List<NotificationResponse> resultado = notificationService.listarPorTipo(10L, "ALERTA_PRESUPUESTO");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipo()).isEqualTo("ALERTA_PRESUPUESTO");
    }

    // ── marcarComoLeida ───────────────────────────────────────────────

    @Test
    @DisplayName("marcarComoLeida: cambia leida a true y persiste")
    void marcarComoLeida_notificacionPendiente_actualizaEstado() {
        NotificationModel leida = new NotificationModel();
        leida.setIdNotification(1L);
        leida.setUserId(10L);
        leida.setLeida(true);
        leida.setEstado("PENDIENTE");
        leida.setFechaCreacion(LocalDate.now());

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notificationModel));
        when(notificationRepository.save(any(NotificationModel.class))).thenReturn(leida);

        NotificationResponse respuesta = notificationService.marcarComoLeida(1L);

        assertThat(respuesta.getLeida()).isTrue();
        verify(notificationRepository).save(any(NotificationModel.class));
    }

    @Test
    @DisplayName("marcarComoLeida: lanza RuntimeException si ya estaba leída")
    void marcarComoLeida_yaLeida_lanzaExcepcion() {
        notificationModel.setLeida(true);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notificationModel));

        assertThatThrownBy(() -> notificationService.marcarComoLeida(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ya fue marcada como leída");

        verify(notificationRepository, never()).save(any());
    }

    // ── marcarComoEnviada ─────────────────────────────────────────────

    @Test
    @DisplayName("marcarComoEnviada: cambia estado a ENVIADO desde PENDIENTE")
    void marcarComoEnviada_estadoPendiente_actualizaEstado() {
        NotificationModel enviada = new NotificationModel();
        enviada.setIdNotification(1L);
        enviada.setEstado("ENVIADO");
        enviada.setLeida(false);
        enviada.setFechaEnvio(LocalDate.now());
        enviada.setFechaCreacion(LocalDate.now());

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notificationModel));
        when(notificationRepository.save(any(NotificationModel.class))).thenReturn(enviada);

        NotificationResponse respuesta = notificationService.marcarComoEnviada(1L);

        assertThat(respuesta.getEstado()).isEqualTo("ENVIADO");
    }

    @Test
    @DisplayName("marcarComoEnviada: lanza RuntimeException si no está en estado PENDIENTE")
    void marcarComoEnviada_estadoNoPermitido_lanzaExcepcion() {
        notificationModel.setEstado("ENVIADO");
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notificationModel));

        assertThatThrownBy(() -> notificationService.marcarComoEnviada(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Solo se puede marcar como ENVIADO");

        verify(notificationRepository, never()).save(any());
    }

    // ── marcarComoFallida ─────────────────────────────────────────────

    @Test
    @DisplayName("marcarComoFallida: lanza RuntimeException si no está en estado PENDIENTE")
    void marcarComoFallida_estadoNoPermitido_lanzaExcepcion() {
        notificationModel.setEstado("ENVIADO");
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notificationModel));

        assertThatThrownBy(() -> notificationService.marcarComoFallida(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Solo se puede marcar como FALLIDO");
    }

    // ── contarNoLeidasPorUsuario ──────────────────────────────────────

    @Test
    @DisplayName("contarNoLeidasPorUsuario: retorna conteo correcto")
    void contarNoLeidas_retornaCantidadCorrecta() {
        when(notificationRepository.contarNoLeidasPorUsuario(10L)).thenReturn(5L);

        Long total = notificationService.contarNoLeidasPorUsuario(10L);

        assertThat(total).isEqualTo(5L);
    }

    // ── eliminar ──────────────────────────────────────────────────────

    @Test
    @DisplayName("eliminar: llama deleteById cuando la notificación existe")
    void eliminar_existente_eliminaCorrectamente() {
        when(notificationRepository.existsById(1L)).thenReturn(true);

        notificationService.eliminar(1L);

        verify(notificationRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar: lanza ResourceNotFoundException si no existe")
    void eliminar_inexistente_lanzaExcepcion() {
        when(notificationRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> notificationService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Notificacion con id 99 no existe");

        verify(notificationRepository, never()).deleteById(any());
    }
}
