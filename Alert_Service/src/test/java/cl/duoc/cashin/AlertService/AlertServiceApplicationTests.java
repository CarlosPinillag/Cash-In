package cl.duoc.cashin.AlertService;

// ══════════════════════════════════════════════════════════════════
// AGREGADO: Pruebas unitarias reales para AlertService (IE 3.1.1)
// Motivo: el archivo original sólo tenía contextLoads() vacío;
//         esta clase cubre la lógica de negocio con JUnit 5 + Mockito.
// Ubicación destino:
//   Alert_Service/src/test/java/cl/duoc/cashin/AlertService/AlertServiceApplicationTests.java
// ══════════════════════════════════════════════════════════════════

import cl.duoc.cashin.AlertService.Client.UserServiceClient;
import cl.duoc.cashin.AlertService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.AlertService.dto.Response.UserRemoteResponse;
import cl.duoc.cashin.AlertService.Model.AlertModel;
import cl.duoc.cashin.AlertService.Repository.AlertRepository;
import cl.duoc.cashin.AlertService.Service.AlertService;
import cl.duoc.cashin.AlertService.dto.Request.AlertRequest;
import cl.duoc.cashin.AlertService.dto.Response.AlertResponse;

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
@DisplayName("AlertService – pruebas unitarias de lógica de negocio")
class AlertServiceApplicationTests {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private AlertService alertService;

    private AlertModel alertModel;
    private AlertRequest alertRequest;

    @BeforeEach
    void setUp() {
        alertModel = new AlertModel();
        alertModel.setIdAlert(1L);
        alertModel.setUserId(10L);
        alertModel.setBudgetId(5L);
        alertModel.setTipo("ALERTA_80");
        alertModel.setMensaje("Has consumido el 80% de tu presupuesto");
        alertModel.setLeida(false);
        alertModel.setFechaCreacion(LocalDate.now());

        alertRequest = new AlertRequest();
        alertRequest.setUserId(10L);
        alertRequest.setBudgetId(5L);
        alertRequest.setTipo("ALERTA_80");
        alertRequest.setMensaje("Has consumido el 80% de tu presupuesto");
    }

    // ── crear ────────────────────────────────────────────────────────

    @Test
    @DisplayName("crear: crea alerta correctamente cuando no existe duplicado activo")
    void crear_sinDuplicadoActivo_retornaAlertaCreada() {
        when(userServiceClient.obtenerUsuarioPorId(anyLong(), anyString())).thenReturn(new UserRemoteResponse());
        when(alertRepository.findByBudgetId(5L)).thenReturn(Collections.emptyList());
        when(alertRepository.save(any(AlertModel.class))).thenReturn(alertModel);

        AlertResponse respuesta = alertService.crear(alertRequest, "Bearer token123");

        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getIdAlert()).isEqualTo(1L);
        assertThat(respuesta.getTipo()).isEqualTo("ALERTA_80");
        assertThat(respuesta.getLeida()).isFalse();

        verify(alertRepository).save(any(AlertModel.class));
    }

    @Test
    @DisplayName("crear: lanza RuntimeException si ya existe alerta activa del mismo tipo para el mismo presupuesto")
    void crear_conDuplicadoActivo_lanzaExcepcion() {
        when(userServiceClient.obtenerUsuarioPorId(anyLong(), anyString())).thenReturn(new UserRemoteResponse());
        when(alertRepository.findByBudgetId(5L)).thenReturn(List.of(alertModel)); // leida=false, mismo tipo

        assertThatThrownBy(() -> alertService.crear(alertRequest, "Bearer token123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya existe una alerta activa de tipo ALERTA_80");

        verify(alertRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear: permite crear alerta si el tipo ya existe pero está leída")
    void crear_alertaDuplicadaPeroLeida_creaCorrectamente() {
        AlertModel leida = new AlertModel();
        leida.setTipo("ALERTA_80");
        leida.setLeida(true); // ya leída → no bloquea
        leida.setBudgetId(5L);

        when(userServiceClient.obtenerUsuarioPorId(anyLong(), anyString())).thenReturn(new UserRemoteResponse());
        when(alertRepository.findByBudgetId(5L)).thenReturn(List.of(leida));
        when(alertRepository.save(any(AlertModel.class))).thenReturn(alertModel);

        AlertResponse respuesta = alertService.crear(alertRequest, "Bearer token123");

        assertThat(respuesta).isNotNull();
        verify(alertRepository).save(any(AlertModel.class));
    }

    // ── obtenerPorId ─────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerPorId: retorna alerta cuando existe")
    void obtenerPorId_existente_retornaAlerta() {
        when(alertRepository.findById(1L)).thenReturn(Optional.of(alertModel));

        AlertResponse respuesta = alertService.obtenerPorId(1L);

        assertThat(respuesta.getIdAlert()).isEqualTo(1L);
        assertThat(respuesta.getUserId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("obtenerPorId: lanza ResourceNotFoundException si no existe")
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(alertRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertService.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Alerta con id 99 no encontrada");
    }

    // ── listarPorUsuario ──────────────────────────────────────────────

    @Test
    @DisplayName("listarPorUsuario: retorna lista de alertas del usuario")
    void listarPorUsuario_conAlertas_retornaLista() {
        when(alertRepository.findByUserIdOrderByFechaCreacionDesc(10L))
                .thenReturn(List.of(alertModel));

        List<AlertResponse> resultado = alertService.listarPorUsuario(10L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getUserId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("listarPorUsuario: retorna lista vacía si no hay alertas")
    void listarPorUsuario_sinAlertas_retornaListaVacia() {
        when(alertRepository.findByUserIdOrderByFechaCreacionDesc(99L))
                .thenReturn(Collections.emptyList());

        List<AlertResponse> resultado = alertService.listarPorUsuario(99L);

        assertThat(resultado).isEmpty();
    }

    // ── listarNoLeidasPorUsuario ──────────────────────────────────────

    @Test
    @DisplayName("listarNoLeidasPorUsuario: retorna sólo las alertas con leida=false")
    void listarNoLeidas_retornaAlertasPendientes() {
        when(alertRepository.findByUserIdAndLeidaFalse(10L)).thenReturn(List.of(alertModel));

        List<AlertResponse> resultado = alertService.listarNoLeidasPorUsuario(10L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getLeida()).isFalse();
    }

    // ── contarNoLeidasPorUsuario ──────────────────────────────────────

    @Test
    @DisplayName("contarNoLeidasPorUsuario: retorna el total de alertas sin leer")
    void contarNoLeidas_retornaCantidadCorrecta() {
        when(alertRepository.contarNoLeidasPorUsuario(10L)).thenReturn(3L);

        Long total = alertService.contarNoLeidasPorUsuario(10L);

        assertThat(total).isEqualTo(3L);
    }

    // ── marcarComoLeida ───────────────────────────────────────────────

    @Test
    @DisplayName("marcarComoLeida: cambia leida a true y persiste")
    void marcarComoLeida_alertaPendiente_actualizaEstado() {
        AlertModel actualizada = new AlertModel();
        actualizada.setIdAlert(1L);
        actualizada.setUserId(10L);
        actualizada.setBudgetId(5L);
        actualizada.setTipo("ALERTA_80");
        actualizada.setMensaje("Has consumido el 80% de tu presupuesto");
        actualizada.setLeida(true);
        actualizada.setFechaCreacion(LocalDate.now());

        when(alertRepository.findById(1L)).thenReturn(Optional.of(alertModel));
        when(alertRepository.save(any(AlertModel.class))).thenReturn(actualizada);

        AlertResponse respuesta = alertService.marcarComoLeida(1L);

        assertThat(respuesta.getLeida()).isTrue();
        verify(alertRepository).save(any(AlertModel.class));
    }

    @Test
    @DisplayName("marcarComoLeida: lanza RuntimeException si ya estaba leída")
    void marcarComoLeida_alertaYaLeida_lanzaExcepcion() {
        alertModel.setLeida(true);
        when(alertRepository.findById(1L)).thenReturn(Optional.of(alertModel));

        assertThatThrownBy(() -> alertService.marcarComoLeida(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ya fue marcada como leída");

        verify(alertRepository, never()).save(any());
    }

    @Test
    @DisplayName("marcarComoLeida: lanza ResourceNotFoundException si no existe")
    void marcarComoLeida_alertaInexistente_lanzaExcepcion() {
        when(alertRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertService.marcarComoLeida(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── eliminar ──────────────────────────────────────────────────────

    @Test
    @DisplayName("eliminar: llama deleteById cuando la alerta existe")
    void eliminar_existente_eliminaCorrectamente() {
        when(alertRepository.existsById(1L)).thenReturn(true);

        alertService.eliminar(1L);

        verify(alertRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar: lanza ResourceNotFoundException si no existe")
    void eliminar_inexistente_lanzaExcepcion() {
        when(alertRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> alertService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Alerta con id 99 no existe");

        verify(alertRepository, never()).deleteById(any());
    }
}
