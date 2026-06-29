package cl.duoc.cashin.AnalyticsService;

import cl.duoc.cashin.AnalyticsService.Client.BudgetServiceClient;
import cl.duoc.cashin.AnalyticsService.Client.ExpenseServiceClient;
import cl.duoc.cashin.AnalyticsService.Client.IncomeServiceClient;
import cl.duoc.cashin.AnalyticsService.Client.UserServiceClient;
import cl.duoc.cashin.AnalyticsService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.AnalyticsService.dto.Response.UserRemoteResponse;
import cl.duoc.cashin.AnalyticsService.Model.AnalyticsModel;
import cl.duoc.cashin.AnalyticsService.Repository.AnalyticsRepository;
import cl.duoc.cashin.AnalyticsService.Service.AnalyticsService;
import cl.duoc.cashin.AnalyticsService.dto.Request.AnalyticsRequest;
import cl.duoc.cashin.AnalyticsService.dto.Response.AnalyticsResponse;
import cl.duoc.cashin.AnalyticsService.dto.Response.BudgetRemoteResponse;
import cl.duoc.cashin.AnalyticsService.dto.Response.ResumenFinancieroResponse;

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
@DisplayName("AnalyticsService – pruebas unitarias de lógica de negocio")
class AnalyticsServiceApplicationTests {

    @Mock private AnalyticsRepository analyticsRepository;
    @Mock private ExpenseServiceClient expenseServiceClient;
    @Mock private IncomeServiceClient incomeServiceClient;
    @Mock private BudgetServiceClient budgetServiceClient;
    @Mock private UserServiceClient userServiceClient;

    @InjectMocks
    private AnalyticsService analyticsService;

    private AnalyticsModel analyticsModel;
    private AnalyticsRequest analyticsRequest;

    @BeforeEach
    void setUp() {
        analyticsModel = new AnalyticsModel();
        analyticsModel.setIdAnalytics(1L);
        analyticsModel.setUserId(10L);
        analyticsModel.setTotalIngresos(200000.0);
        analyticsModel.setTotalGastos(100000.0);
        analyticsModel.setBalance(100000.0);
        analyticsModel.setTasaAhorro(50.0);
        analyticsModel.setEstadoBalance("POSITIVO");
        analyticsModel.setFechaGeneracion(LocalDate.now());

        analyticsRequest = new AnalyticsRequest();
        analyticsRequest.setUserId(10L);
    }

    // ── generarAnalisis ──────────────────────────────────────────────

    @Test
    @DisplayName("generarAnalisis: calcula balance positivo y tasa de ahorro correctamente")
    void generarAnalisis_balancePositivo_calculaCorrectamente() {
        when(userServiceClient.obtenerUsuarioPorId(anyLong(), anyString())).thenReturn(new UserRemoteResponse());
        when(incomeServiceClient.obtenerTotalIngresosPorUsuario(10L, "Bearer token")).thenReturn(200000.0);
        when(expenseServiceClient.obtenerTotalGastadoPorUsuario(10L, "Bearer token")).thenReturn(100000.0);
        when(analyticsRepository.save(any(AnalyticsModel.class))).thenReturn(analyticsModel);

        AnalyticsResponse respuesta = analyticsService.generarAnalisis(analyticsRequest, "Bearer token");

        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getEstadoBalance()).isEqualTo("POSITIVO");
        assertThat(respuesta.getBalance()).isEqualTo(100000.0);
        assertThat(respuesta.getTasaAhorro()).isEqualTo(50.0);
        verify(analyticsRepository).save(any(AnalyticsModel.class));
    }

    @Test
    @DisplayName("generarAnalisis: estado NEGATIVO cuando gastos superan ingresos")
    void generarAnalisis_gastosSuperiores_estadoNegativo() {
        AnalyticsModel negativo = new AnalyticsModel();
        negativo.setIdAnalytics(2L);
        negativo.setUserId(10L);
        negativo.setTotalIngresos(50000.0);
        negativo.setTotalGastos(80000.0);
        negativo.setBalance(-30000.0);
        negativo.setTasaAhorro(-60.0);
        negativo.setEstadoBalance("NEGATIVO");
        negativo.setFechaGeneracion(LocalDate.now());

        when(userServiceClient.obtenerUsuarioPorId(anyLong(), anyString())).thenReturn(new UserRemoteResponse());
        when(incomeServiceClient.obtenerTotalIngresosPorUsuario(10L, "Bearer token")).thenReturn(50000.0);
        when(expenseServiceClient.obtenerTotalGastadoPorUsuario(10L, "Bearer token")).thenReturn(80000.0);
        when(analyticsRepository.save(any(AnalyticsModel.class))).thenReturn(negativo);

        AnalyticsResponse respuesta = analyticsService.generarAnalisis(analyticsRequest, "Bearer token");

        assertThat(respuesta.getEstadoBalance()).isEqualTo("NEGATIVO");
        assertThat(respuesta.getBalance()).isNegative();
    }

    @Test
    @DisplayName("generarAnalisis: tasa de ahorro es 0 cuando no hay ingresos")
    void generarAnalisis_sinIngresos_tasaAhorroEsCero() {
        AnalyticsModel sinIngresos = new AnalyticsModel();
        sinIngresos.setIdAnalytics(3L);
        sinIngresos.setUserId(10L);
        sinIngresos.setTotalIngresos(0.0);
        sinIngresos.setTotalGastos(0.0);
        sinIngresos.setBalance(0.0);
        sinIngresos.setTasaAhorro(0.0);
        sinIngresos.setEstadoBalance("EQUILIBRADO");
        sinIngresos.setFechaGeneracion(LocalDate.now());

        when(userServiceClient.obtenerUsuarioPorId(anyLong(), anyString())).thenReturn(new UserRemoteResponse());
        when(incomeServiceClient.obtenerTotalIngresosPorUsuario(10L, "Bearer token")).thenReturn(0.0);
        when(expenseServiceClient.obtenerTotalGastadoPorUsuario(10L, "Bearer token")).thenReturn(0.0);
        when(analyticsRepository.save(any(AnalyticsModel.class))).thenReturn(sinIngresos);

        AnalyticsResponse respuesta = analyticsService.generarAnalisis(analyticsRequest, "Bearer token");

        assertThat(respuesta.getTasaAhorro()).isEqualTo(0.0);
        assertThat(respuesta.getEstadoBalance()).isEqualTo("EQUILIBRADO");
    }

    // ── obtenerPorId ─────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerPorId: retorna análisis cuando existe")
    void obtenerPorId_existente_retornaAnalisis() {
        when(analyticsRepository.findById(1L)).thenReturn(Optional.of(analyticsModel));

        AnalyticsResponse respuesta = analyticsService.obtenerPorId(1L);

        assertThat(respuesta.getIdAnalytics()).isEqualTo(1L);
        assertThat(respuesta.getUserId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("obtenerPorId: lanza ResourceNotFoundException si no existe")
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(analyticsRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> analyticsService.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Análisis con id 99 no encontrado");
    }

    // ── obtenerHistorialPorUsuario ────────────────────────────────────

    @Test
    @DisplayName("obtenerHistorialPorUsuario: retorna lista de análisis del usuario")
    void obtenerHistorial_conRegistros_retornaLista() {
        when(analyticsRepository.findByUserIdOrderByFechaGeneracionDesc(10L))
                .thenReturn(List.of(analyticsModel));

        List<AnalyticsResponse> resultado = analyticsService.obtenerHistorialPorUsuario(10L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getUserId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("obtenerHistorialPorUsuario: retorna lista vacía si no hay registros")
    void obtenerHistorial_sinRegistros_retornaListaVacia() {
        when(analyticsRepository.findByUserIdOrderByFechaGeneracionDesc(99L))
                .thenReturn(Collections.emptyList());

        List<AnalyticsResponse> resultado = analyticsService.obtenerHistorialPorUsuario(99L);

        assertThat(resultado).isEmpty();
    }

    // ── obtenerResumenFinanciero ──────────────────────────────────────

    @Test
    @DisplayName("obtenerResumenFinanciero: genera resumen con presupuestos activos")
    void obtenerResumen_conPresupuestosActivos_retornaResumen() {
        BudgetRemoteResponse presupuesto = new BudgetRemoteResponse();
        presupuesto.setActivo(true);

        when(userServiceClient.obtenerUsuarioPorId(anyLong(), anyString())).thenReturn(new UserRemoteResponse());
        when(incomeServiceClient.obtenerTotalIngresosPorUsuario(10L, "Bearer token")).thenReturn(200000.0);
        when(expenseServiceClient.obtenerTotalGastadoPorUsuario(10L, "Bearer token")).thenReturn(100000.0);
        when(budgetServiceClient.obtenerPresupuestosPorUsuario(10L, "Bearer token"))
                .thenReturn(List.of(presupuesto));
        when(analyticsRepository.promedioTasaAhorroPorUsuario(10L)).thenReturn(45.0);
        when(analyticsRepository.contarBalancesNegativos(10L)).thenReturn(0L);

        ResumenFinancieroResponse resumen = analyticsService.obtenerResumenFinanciero(10L, "Bearer token");

        assertThat(resumen).isNotNull();
        assertThat(resumen.getUserId()).isEqualTo(10L);
        assertThat(resumen.getBalance()).isEqualTo(100000.0);
        assertThat(resumen.getPresupuestosActivos()).hasSize(1);
        assertThat(resumen.getRecomendacion()).isNotBlank();
    }

    @Test
    @DisplayName("obtenerResumenFinanciero: continúa si budget-service falla (error absorbido)")
    void obtenerResumen_fallaBudgetService_noPropagarError() {
        when(userServiceClient.obtenerUsuarioPorId(anyLong(), anyString())).thenReturn(new UserRemoteResponse());
        when(incomeServiceClient.obtenerTotalIngresosPorUsuario(10L, "Bearer token")).thenReturn(100000.0);
        when(expenseServiceClient.obtenerTotalGastadoPorUsuario(10L, "Bearer token")).thenReturn(50000.0);
        when(budgetServiceClient.obtenerPresupuestosPorUsuario(10L, "Bearer token"))
                .thenThrow(new RuntimeException("budget-service no disponible"));
        when(analyticsRepository.promedioTasaAhorroPorUsuario(10L)).thenReturn(null);
        when(analyticsRepository.contarBalancesNegativos(10L)).thenReturn(0L);

        assertThatNoException().isThrownBy(
                () -> analyticsService.obtenerResumenFinanciero(10L, "Bearer token"));
    }

    // ── eliminar ──────────────────────────────────────────────────────

    @Test
    @DisplayName("eliminar: llama deleteById cuando el análisis existe")
    void eliminar_existente_eliminaCorrectamente() {
        when(analyticsRepository.existsById(1L)).thenReturn(true);

        analyticsService.eliminar(1L);

        verify(analyticsRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar: lanza ResourceNotFoundException si no existe")
    void eliminar_inexistente_lanzaExcepcion() {
        when(analyticsRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> analyticsService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Análisis con id 99 no existe");

        verify(analyticsRepository, never()).deleteById(any());
    }
}
