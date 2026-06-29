package cl.duoc.cashin.ExpenseService;

import cl.duoc.cashin.ExpenseService.Client.CategoryServiceClient;
import cl.duoc.cashin.ExpenseService.Client.UserServiceClient;
import cl.duoc.cashin.ExpenseService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.ExpenseService.dto.Response.UserRemoteResponse;
import cl.duoc.cashin.ExpenseService.Model.ExpenseModel;
import cl.duoc.cashin.ExpenseService.Repository.ExpenseRepository;
import cl.duoc.cashin.ExpenseService.Service.ExpenseService;
import cl.duoc.cashin.ExpenseService.dto.Request.ExpenseRequest;
import cl.duoc.cashin.ExpenseService.dto.Request.ExpenseUpdateRequest;
import cl.duoc.cashin.ExpenseService.dto.Response.CategoryRemoteResponse;
import cl.duoc.cashin.ExpenseService.dto.Response.ExpenseResponse;

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
@DisplayName("ExpenseService – pruebas unitarias de lógica de negocio")
class ExpenseServiceApplicationTests {

    @Mock private ExpenseRepository expenseRepository;
    @Mock private UserServiceClient userServiceClient;
    @Mock private CategoryServiceClient categoryServiceClient;

    @InjectMocks
    private ExpenseService expenseService;

    private ExpenseModel expenseModel;
    private ExpenseRequest expenseRequest;
    private CategoryRemoteResponse categoriaActiva;

    @BeforeEach
    void setUp() {
        expenseModel = new ExpenseModel();
        expenseModel.setIdExpense(1L);
        expenseModel.setUserId(10L);
        expenseModel.setCategoryId(5L);
        expenseModel.setNombreCategoria("Alimentación");
        expenseModel.setMonto(25000.0);
        expenseModel.setDescripcion("Supermercado");
        expenseModel.setFecha(LocalDate.now());
        expenseModel.setTipo("VARIABLE");

        categoriaActiva = new CategoryRemoteResponse();
        categoriaActiva.setIdCategory(5L);
        categoriaActiva.setNombre("Alimentación");
        categoriaActiva.setActivo(true);

        expenseRequest = new ExpenseRequest();
        expenseRequest.setUserId(10L);
        expenseRequest.setCategoryId(5L);
        expenseRequest.setMonto(25000.0);
        expenseRequest.setDescripcion("Supermercado");
        expenseRequest.setFecha(LocalDate.now());
        expenseRequest.setTipo("VARIABLE");
    }

    // ── crear ────────────────────────────────────────────────────────

    @Test
    @DisplayName("crear: crea gasto correctamente con categoría activa")
    void crear_categoriaActiva_retornaGastoCreado() {
        when(userServiceClient.obtenerUsuarioPorId(anyLong(), anyString())).thenReturn(new UserRemoteResponse());
        when(categoryServiceClient.obtenerCategoriaPorId(5L, "Bearer token")).thenReturn(categoriaActiva);
        when(expenseRepository.save(any(ExpenseModel.class))).thenReturn(expenseModel);

        ExpenseResponse respuesta = expenseService.crear(expenseRequest, "Bearer token");

        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getIdExpense()).isEqualTo(1L);
        assertThat(respuesta.getNombreCategoria()).isEqualTo("Alimentación");
        assertThat(respuesta.getMonto()).isEqualTo(25000.0);
        verify(expenseRepository).save(any(ExpenseModel.class));
    }

    @Test
    @DisplayName("crear: lanza RuntimeException si la categoría está inactiva")
    void crear_categoriaInactiva_lanzaExcepcion() {
        categoriaActiva.setActivo(false);

        when(userServiceClient.obtenerUsuarioPorId(anyLong(), anyString())).thenReturn(new UserRemoteResponse());
        when(categoryServiceClient.obtenerCategoriaPorId(5L, "Bearer token")).thenReturn(categoriaActiva);

        assertThatThrownBy(() -> expenseService.crear(expenseRequest, "Bearer token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no esta activa");

        verify(expenseRepository, never()).save(any());
    }

    // ── obtenerPorId ─────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerPorId: retorna gasto cuando existe")
    void obtenerPorId_existente_retornaGasto() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expenseModel));

        ExpenseResponse respuesta = expenseService.obtenerPorId(1L);

        assertThat(respuesta.getIdExpense()).isEqualTo(1L);
        assertThat(respuesta.getMonto()).isEqualTo(25000.0);
    }

    @Test
    @DisplayName("obtenerPorId: lanza ResourceNotFoundException si no existe")
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Gasto con id 99 no encontrado");
    }

    // ── listarPorUsuario ──────────────────────────────────────────────

    @Test
    @DisplayName("listarPorUsuario: retorna lista de gastos del usuario")
    void listarPorUsuario_conGastos_retornaLista() {
        when(expenseRepository.findByUserId(10L)).thenReturn(List.of(expenseModel));

        List<ExpenseResponse> resultado = expenseService.listarPorUsuario(10L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getUserId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("listarPorUsuario: retorna lista vacía si no hay gastos")
    void listarPorUsuario_sinGastos_retornaListaVacia() {
        when(expenseRepository.findByUserId(99L)).thenReturn(Collections.emptyList());

        List<ExpenseResponse> resultado = expenseService.listarPorUsuario(99L);

        assertThat(resultado).isEmpty();
    }

    // ── obtenerTotalPorUsuario ────────────────────────────────────────

    @Test
    @DisplayName("obtenerTotalPorUsuario: retorna suma de montos")
    void obtenerTotal_conGastos_retornaTotal() {
        when(expenseRepository.sumMontoByUserId(10L)).thenReturn(75000.0);

        Double total = expenseService.obtenerTotalPorUsuario(10L);

        assertThat(total).isEqualTo(75000.0);
    }

    @Test
    @DisplayName("obtenerTotalPorUsuario: retorna 0.0 cuando no hay gastos (null de la BD)")
    void obtenerTotal_sinGastos_retornaCero() {
        when(expenseRepository.sumMontoByUserId(99L)).thenReturn(null);

        Double total = expenseService.obtenerTotalPorUsuario(99L);

        assertThat(total).isEqualTo(0.0);
    }

    // ── actualizar ────────────────────────────────────────────────────

    @Test
    @DisplayName("actualizar: actualiza monto y descripción correctamente")
    void actualizar_camposValidos_actualizaGasto() {
        ExpenseUpdateRequest updateRequest = new ExpenseUpdateRequest();
        updateRequest.setMonto(30000.0);
        updateRequest.setDescripcion("Supermercado actualizado");

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expenseModel));
        when(expenseRepository.save(any(ExpenseModel.class))).thenReturn(expenseModel);

        ExpenseResponse respuesta = expenseService.actualizar(1L, updateRequest, "Bearer token");

        assertThat(respuesta).isNotNull();
        verify(expenseRepository).save(any(ExpenseModel.class));
    }

    @Test
    @DisplayName("actualizar: lanza ResourceNotFoundException si el gasto no existe")
    void actualizar_inexistente_lanzaExcepcion() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.actualizar(99L, new ExpenseUpdateRequest(), "Bearer token"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Gasto con id 99 no encontrado");
    }

    @Test
    @DisplayName("actualizar: lanza RuntimeException si la nueva categoría está inactiva")
    void actualizar_categoriaInactiva_lanzaExcepcion() {
        categoriaActiva.setActivo(false);
        ExpenseUpdateRequest updateRequest = new ExpenseUpdateRequest();
        updateRequest.setCategoryId(5L);

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expenseModel));
        when(categoryServiceClient.obtenerCategoriaPorId(5L, "Bearer token")).thenReturn(categoriaActiva);

        assertThatThrownBy(() -> expenseService.actualizar(1L, updateRequest, "Bearer token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no esta activa");

        verify(expenseRepository, never()).save(any());
    }

    // ── eliminar ──────────────────────────────────────────────────────

    @Test
    @DisplayName("eliminar: llama deleteById cuando el gasto existe")
    void eliminar_existente_eliminaCorrectamente() {
        when(expenseRepository.existsById(1L)).thenReturn(true);

        expenseService.eliminar(1L);

        verify(expenseRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar: lanza ResourceNotFoundException si no existe")
    void eliminar_inexistente_lanzaExcepcion() {
        when(expenseRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> expenseService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Gasto con id 99 no existe");

        verify(expenseRepository, never()).deleteById(any());
    }
}
