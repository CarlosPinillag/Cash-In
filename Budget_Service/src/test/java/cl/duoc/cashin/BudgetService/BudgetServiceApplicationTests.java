package cl.duoc.cashin.BudgetService;

// ══════════════════════════════════════════════════════════════════
// AGREGADO: Pruebas unitarias reales para BudgetService (IE 3.1.1)
// Motivo: Budget_Service no tenía ningún archivo de test; esta clase
//         cubre la lógica central con JUnit 5 + Mockito, incluyendo
//         las reglas de duplicados y los umbrales de alerta (80%/100%).
// Ubicación destino:
//   Budget_Service/src/test/java/cl/duoc/cashin/BudgetService/BudgetServiceApplicationTests.java
// ══════════════════════════════════════════════════════════════════

import cl.duoc.cashin.BudgetService.Client.AlertServiceClient;
import cl.duoc.cashin.BudgetService.Client.ExpenseServiceClient;
import cl.duoc.cashin.BudgetService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.BudgetService.Model.BudgetModel;
import cl.duoc.cashin.BudgetService.Repository.BudgetRepository;
import cl.duoc.cashin.BudgetService.Service.BudgetService;
import cl.duoc.cashin.BudgetService.dto.Request.BudgetRequest;
import cl.duoc.cashin.BudgetService.dto.Response.BudgetResponse;

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
@DisplayName("BudgetService – pruebas unitarias de lógica de negocio")
class BudgetServiceApplicationTests {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ExpenseServiceClient expenseServiceClient;

    @Mock
    private AlertServiceClient alertServiceClient;

    @InjectMocks
    private BudgetService budgetService;

    private BudgetModel budgetModel;
    private BudgetRequest budgetRequest;

    @BeforeEach
    void setUp() {
        budgetModel = new BudgetModel();
        budgetModel.setIdBudget(1L);
        budgetModel.setUserId(10L);
        budgetModel.setCategoryId(5L);
        budgetModel.setMontoLimite(100000.0);
        budgetModel.setPeriodo("MENSUAL");
        budgetModel.setActivo(true);
        budgetModel.setPorcentajeUso(0.0);
        budgetModel.setFechaInicio(LocalDate.now());

        budgetRequest = new BudgetRequest();
        budgetRequest.setUserId(10L);
        budgetRequest.setCategoryId(5L);
        budgetRequest.setMontoLimite(100000.0);
        budgetRequest.setPeriodo("MENSUAL");
        budgetRequest.setFechaInicio(LocalDate.now());
    }

    // ── crear ────────────────────────────────────────────────────────

    @Test
    @DisplayName("crear: crea presupuesto correctamente cuando no hay duplicado activo")
    void crear_sinDuplicado_retornaPresupuestoCreado() {
        when(budgetRepository.findByUserIdAndCategoryIdAndPeriodoAndActivoTrue(10L, 5L, "MENSUAL"))
                .thenReturn(Optional.empty());
        when(budgetRepository.save(any(BudgetModel.class))).thenReturn(budgetModel);

        BudgetResponse respuesta = budgetService.crear(budgetRequest);

        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getIdBudget()).isEqualTo(1L);
        assertThat(respuesta.getActivo()).isTrue();
        assertThat(respuesta.getPorcentajeUso()).isEqualTo(0.0);

        verify(budgetRepository).save(any(BudgetModel.class));
    }

    @Test
    @DisplayName("crear: lanza RuntimeException si existe presupuesto activo con misma categoría y periodo")
    void crear_duplicadoConCategoria_lanzaExcepcion() {
        when(budgetRepository.findByUserIdAndCategoryIdAndPeriodoAndActivoTrue(10L, 5L, "MENSUAL"))
                .thenReturn(Optional.of(budgetModel));

        assertThatThrownBy(() -> budgetService.crear(budgetRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya existe un presupuesto activo");

        verify(budgetRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear: lanza RuntimeException si existe presupuesto global activo para el mismo periodo")
    void crear_duplicadoGlobal_lanzaExcepcion() {
        budgetRequest.setCategoryId(null); // presupuesto global

        when(budgetRepository.findByUserIdAndCategoryIdIsNullAndPeriodoAndActivoTrue(10L, "MENSUAL"))
                .thenReturn(Optional.of(budgetModel));

        assertThatThrownBy(() -> budgetService.crear(budgetRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya existe un presupuesto global activo");

        verify(budgetRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear: nuevo presupuesto siempre inicia con activo=true y porcentajeUso=0")
    void crear_estadoInicialCorrecto() {
        when(budgetRepository.findByUserIdAndCategoryIdAndPeriodoAndActivoTrue(anyLong(), anyLong(), anyString()))
                .thenReturn(Optional.empty());
        when(budgetRepository.save(any(BudgetModel.class))).thenAnswer(inv -> {
            BudgetModel m = inv.getArgument(0);
            assertThat(m.getActivo()).isTrue();
            assertThat(m.getPorcentajeUso()).isEqualTo(0.0);
            return budgetModel;
        });

        budgetService.crear(budgetRequest);
    }

    // ── obtenerPorId ─────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerPorId: retorna presupuesto cuando existe")
    void obtenerPorId_existente_retornaPresupuesto() {
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budgetModel));

        BudgetResponse respuesta = budgetService.obtenerPorId(1L);

        assertThat(respuesta.getIdBudget()).isEqualTo(1L);
        assertThat(respuesta.getMontoLimite()).isEqualTo(100000.0);
    }

    @Test
    @DisplayName("obtenerPorId: lanza ResourceNotFoundException si no existe")
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(budgetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Presupuesto con id 99 no encontrado");
    }

    // ── listarPorUsuario ──────────────────────────────────────────────

    @Test
    @DisplayName("listarPorUsuario: retorna lista de presupuestos del usuario")
    void listarPorUsuario_conPresupuestos_retornaLista() {
        when(budgetRepository.findByUserId(10L)).thenReturn(List.of(budgetModel));

        List<BudgetResponse> resultado = budgetService.listarPorUsuario(10L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getUserId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("listarPorUsuario: retorna lista vacía si no hay presupuestos")
    void listarPorUsuario_sinPresupuestos_retornaListaVacia() {
        when(budgetRepository.findByUserId(99L)).thenReturn(Collections.emptyList());

        List<BudgetResponse> resultado = budgetService.listarPorUsuario(99L);

        assertThat(resultado).isEmpty();
    }

    // ── obtenerSeguimiento (umbral 80%) ──────────────────────────────

    @Test
    @DisplayName("obtenerSeguimiento: calcula porcentaje y dispara alerta ALERTA_80 cuando supera 80%")
    void obtenerSeguimiento_superaOchentaPorciento_creaAlerta80() {
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budgetModel));
        when(expenseServiceClient.obtenerTotalGastadoPorUsuario(10L, "Bearer token"))
                .thenReturn(85000.0); // 85% de 100000
        when(budgetRepository.save(any(BudgetModel.class))).thenReturn(budgetModel);

        budgetService.obtenerSeguimiento(1L, "Bearer token");

        verify(alertServiceClient).crearAlerta(eq(10L), eq(1L), eq("ALERTA_80"), anyString(), eq("Bearer token"));
        verify(alertServiceClient, never()).crearAlerta(eq(10L), eq(1L), eq("ALERTA_100"), anyString(), anyString());
    }

    @Test
    @DisplayName("obtenerSeguimiento: dispara alerta ALERTA_100 cuando supera 100%")
    void obtenerSeguimiento_superaCienPorciento_creaAlerta100() {
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budgetModel));
        when(expenseServiceClient.obtenerTotalGastadoPorUsuario(10L, "Bearer token"))
                .thenReturn(120000.0); // 120%
        when(budgetRepository.save(any(BudgetModel.class))).thenReturn(budgetModel);

        budgetService.obtenerSeguimiento(1L, "Bearer token");

        verify(alertServiceClient).crearAlerta(eq(10L), eq(1L), eq("ALERTA_100"), anyString(), eq("Bearer token"));
        verify(alertServiceClient, never()).crearAlerta(eq(10L), eq(1L), eq("ALERTA_80"), anyString(), anyString());
    }

    @Test
    @DisplayName("obtenerSeguimiento: no dispara alerta cuando el gasto está por debajo del 80%")
    void obtenerSeguimiento_menosDe80Porciento_noDisparaAlerta() {
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budgetModel));
        when(expenseServiceClient.obtenerTotalGastadoPorUsuario(10L, "Bearer token"))
                .thenReturn(50000.0); // 50%
        when(budgetRepository.save(any(BudgetModel.class))).thenReturn(budgetModel);

        budgetService.obtenerSeguimiento(1L, "Bearer token");

        verify(alertServiceClient, never()).crearAlerta(anyLong(), anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("obtenerSeguimiento: lanza RuntimeException si el presupuesto no está activo")
    void obtenerSeguimiento_presupuestoInactivo_lanzaExcepcion() {
        budgetModel.setActivo(false);
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budgetModel));

        assertThatThrownBy(() -> budgetService.obtenerSeguimiento(1L, "Bearer token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no esta activo");
    }

    @Test
    @DisplayName("obtenerSeguimiento: continúa aunque la creación de alerta falle (error absorbido)")
    void obtenerSeguimiento_fallaAlerta_noPropagraError() {
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budgetModel));
        when(expenseServiceClient.obtenerTotalGastadoPorUsuario(10L, "Bearer token"))
                .thenReturn(85000.0);
        doThrow(new RuntimeException("Alert service no disponible"))
                .when(alertServiceClient).crearAlerta(anyLong(), anyLong(), anyString(), anyString(), anyString());
        when(budgetRepository.save(any(BudgetModel.class))).thenReturn(budgetModel);

        // No debe propagarse la excepción del AlertService
        assertThatNoException().isThrownBy(
                () -> budgetService.obtenerSeguimiento(1L, "Bearer token"));
    }

    // ── eliminar ──────────────────────────────────────────────────────

    @Test
    @DisplayName("eliminar: llama deleteById cuando el presupuesto existe")
    void eliminar_existente_eliminaCorrectamente() {
        when(budgetRepository.existsById(1L)).thenReturn(true);

        budgetService.eliminar(1L);

        verify(budgetRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar: lanza ResourceNotFoundException si no existe")
    void eliminar_inexistente_lanzaExcepcion() {
        when(budgetRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> budgetService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Presupuesto con id 99 no existe");

        verify(budgetRepository, never()).deleteById(any());
    }
}
