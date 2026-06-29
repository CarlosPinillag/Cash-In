package cl.duoc.cashin.IncomeService;

import cl.duoc.cashin.IncomeService.Client.UserServiceClient;
import cl.duoc.cashin.IncomeService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.IncomeService.dto.Response.UserRemoteResponse;
import cl.duoc.cashin.IncomeService.Model.IncomeModel;
import cl.duoc.cashin.IncomeService.Repository.IncomeRepository;
import cl.duoc.cashin.IncomeService.Service.IncomeService;
import cl.duoc.cashin.IncomeService.dto.Request.IncomeRequest;
import cl.duoc.cashin.IncomeService.dto.Request.IncomeUpdateRequest;
import cl.duoc.cashin.IncomeService.dto.Response.IncomeResponse;

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
@DisplayName("IncomeService – pruebas unitarias de lógica de negocio")
class IncomeServiceApplicationTests {

    @Mock private IncomeRepository incomeRepository;
    @Mock private UserServiceClient userServiceClient;

    @InjectMocks
    private IncomeService incomeService;

    private IncomeModel incomeModel;
    private IncomeRequest incomeRequest;

    @BeforeEach
    void setUp() {
        incomeModel = new IncomeModel();
        incomeModel.setIdIncome(1L);
        incomeModel.setUserId(10L);
        incomeModel.setMonto(500000.0);
        incomeModel.setDescripcion("Salario mensual");
        incomeModel.setCategoria("SALARIO");
        incomeModel.setFecha(LocalDate.now());
        incomeModel.setRecurrente(true);
        incomeModel.setFrecuencia("MENSUAL");

        incomeRequest = new IncomeRequest();
        incomeRequest.setUserId(10L);
        incomeRequest.setMonto(500000.0);
        incomeRequest.setDescripcion("Salario mensual");
        incomeRequest.setCategoria("Salario");
        incomeRequest.setFecha(LocalDate.now());
        incomeRequest.setRecurrente(true);
        incomeRequest.setFrecuencia("MENSUAL");
    }

    // ── crear ────────────────────────────────────────────────────────

    @Test
    @DisplayName("crear: crea ingreso recurrente con frecuencia MENSUAL correctamente")
    void crear_recurrenteConFrecuencia_retornaIngreso() {
        when(userServiceClient.obtenerUsuarioPorId(anyLong(), anyString())).thenReturn(new UserRemoteResponse());
        when(incomeRepository.save(any(IncomeModel.class))).thenReturn(incomeModel);

        IncomeResponse respuesta = incomeService.crear(incomeRequest, "Bearer token");

        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getIdIncome()).isEqualTo(1L);
        assertThat(respuesta.getRecurrente()).isTrue();
        assertThat(respuesta.getFrecuencia()).isEqualTo("MENSUAL");
        verify(incomeRepository).save(any(IncomeModel.class));
    }

    @Test
    @DisplayName("crear: ingreso no recurrente no requiere frecuencia")
    void crear_noRecurrente_retornaIngresoSinFrecuencia() {
        IncomeModel noRecurrente = new IncomeModel();
        noRecurrente.setIdIncome(2L);
        noRecurrente.setUserId(10L);
        noRecurrente.setMonto(100000.0);
        noRecurrente.setDescripcion("Bono puntual");
        noRecurrente.setCategoria("BONO");
        noRecurrente.setFecha(LocalDate.now());
        noRecurrente.setRecurrente(false);
        noRecurrente.setFrecuencia(null);

        incomeRequest.setRecurrente(false);
        incomeRequest.setFrecuencia(null);

        when(userServiceClient.obtenerUsuarioPorId(anyLong(), anyString())).thenReturn(new UserRemoteResponse());
        when(incomeRepository.save(any(IncomeModel.class))).thenReturn(noRecurrente);

        IncomeResponse respuesta = incomeService.crear(incomeRequest, "Bearer token");

        assertThat(respuesta.getRecurrente()).isFalse();
        assertThat(respuesta.getFrecuencia()).isNull();
    }

    @Test
    @DisplayName("crear: lanza RuntimeException si recurrente=true pero sin frecuencia")
    void crear_recurrenteSinFrecuencia_lanzaExcepcion() {
        incomeRequest.setFrecuencia(null);

        when(userServiceClient.obtenerUsuarioPorId(anyLong(), anyString())).thenReturn(new UserRemoteResponse());

        assertThatThrownBy(() -> incomeService.crear(incomeRequest, "Bearer token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("FRECUENCIA ES OBLIGATORIA");

        verify(incomeRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear: lanza RuntimeException si la frecuencia no es válida")
    void crear_frecuenciaInvalida_lanzaExcepcion() {
        incomeRequest.setFrecuencia("DIARIO");

        when(userServiceClient.obtenerUsuarioPorId(anyLong(), anyString())).thenReturn(new UserRemoteResponse());

        assertThatThrownBy(() -> incomeService.crear(incomeRequest, "Bearer token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("FRECUENCIA INVALIDA");

        verify(incomeRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear: acepta frecuencia SEMANAL y QUINCENAL")
    void crear_frecuenciasValidas_creaCorrectamente() {
        for (String freq : List.of("SEMANAL", "QUINCENAL")) {
            incomeRequest.setFrecuencia(freq);
            incomeModel.setFrecuencia(freq);

            when(userServiceClient.obtenerUsuarioPorId(anyLong(), anyString())).thenReturn(new UserRemoteResponse());
            when(incomeRepository.save(any(IncomeModel.class))).thenReturn(incomeModel);

            assertThatNoException().isThrownBy(
                    () -> incomeService.crear(incomeRequest, "Bearer token"));
        }
    }

    // ── obtenerPorId ─────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerPorId: retorna ingreso cuando existe")
    void obtenerPorId_existente_retornaIngreso() {
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(incomeModel));

        IncomeResponse respuesta = incomeService.obtenerPorId(1L);

        assertThat(respuesta.getIdIncome()).isEqualTo(1L);
        assertThat(respuesta.getMonto()).isEqualTo(500000.0);
    }

    @Test
    @DisplayName("obtenerPorId: lanza ResourceNotFoundException si no existe")
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(incomeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ingreso con id 99 no encontrado");
    }

    // ── listarPorUsuario ──────────────────────────────────────────────

    @Test
    @DisplayName("listarPorUsuario: retorna lista de ingresos del usuario")
    void listarPorUsuario_conIngresos_retornaLista() {
        when(incomeRepository.findByUserId(10L)).thenReturn(List.of(incomeModel));

        List<IncomeResponse> resultado = incomeService.listarPorUsuario(10L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getUserId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("listarPorUsuario: retorna lista vacía si no hay ingresos")
    void listarPorUsuario_sinIngresos_retornaListaVacia() {
        when(incomeRepository.findByUserId(99L)).thenReturn(Collections.emptyList());

        assertThat(incomeService.listarPorUsuario(99L)).isEmpty();
    }

    // ── obtenerTotalPorUsuario ────────────────────────────────────────

    @Test
    @DisplayName("obtenerTotalPorUsuario: retorna suma de ingresos")
    void obtenerTotal_conIngresos_retornaTotal() {
        when(incomeRepository.sumMontoByUserId(10L)).thenReturn(500000.0);

        assertThat(incomeService.obtenerTotalPorUsuario(10L)).isEqualTo(500000.0);
    }

    @Test
    @DisplayName("obtenerTotalPorUsuario: retorna 0.0 cuando la BD retorna null")
    void obtenerTotal_sinIngresos_retornaCero() {
        when(incomeRepository.sumMontoByUserId(99L)).thenReturn(null);

        assertThat(incomeService.obtenerTotalPorUsuario(99L)).isEqualTo(0.0);
    }

    // ── actualizar ────────────────────────────────────────────────────

    @Test
    @DisplayName("actualizar: lanza RuntimeException si recurrente=true y frecuencia queda vacía")
    void actualizar_recurrenteSinFrecuencia_lanzaExcepcion() {
        incomeModel.setRecurrente(true);
        incomeModel.setFrecuencia(null);

        IncomeUpdateRequest updateRequest = new IncomeUpdateRequest();
        updateRequest.setRecurrente(true);

        when(incomeRepository.findById(1L)).thenReturn(Optional.of(incomeModel));

        assertThatThrownBy(() -> incomeService.actualizar(1L, updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("FRECUENCIA ES OBLIGATORIA");

        verify(incomeRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar: lanza ResourceNotFoundException si el ingreso no existe")
    void actualizar_inexistente_lanzaExcepcion() {
        when(incomeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.actualizar(99L, new IncomeUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ingreso con id 99 no encontrado");
    }

    // ── eliminar ──────────────────────────────────────────────────────

    @Test
    @DisplayName("eliminar: llama deleteById cuando el ingreso existe")
    void eliminar_existente_eliminaCorrectamente() {
        when(incomeRepository.existsById(1L)).thenReturn(true);

        incomeService.eliminar(1L);

        verify(incomeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar: lanza ResourceNotFoundException si no existe")
    void eliminar_inexistente_lanzaExcepcion() {
        when(incomeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> incomeService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ingreso con id 99 no existe");

        verify(incomeRepository, never()).deleteById(any());
    }
}
