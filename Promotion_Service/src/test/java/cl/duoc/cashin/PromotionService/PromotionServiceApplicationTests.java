package cl.duoc.cashin.PromotionService;

import cl.duoc.cashin.PromotionService.Client.CategoryServiceClient;
import cl.duoc.cashin.PromotionService.Client.UserServiceClient;
import cl.duoc.cashin.PromotionService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.PromotionService.Model.PromotionModel;
import cl.duoc.cashin.PromotionService.Repository.PromotionRepository;
import cl.duoc.cashin.PromotionService.Service.PromotionService;
import cl.duoc.cashin.PromotionService.dto.Request.PromotionAplicarRequest;
import cl.duoc.cashin.PromotionService.dto.Request.PromotionRequest;
import cl.duoc.cashin.PromotionService.dto.Response.CategoryRemoteResponse;
import cl.duoc.cashin.PromotionService.dto.Response.PromotionAplicarResponse;
import cl.duoc.cashin.PromotionService.dto.Response.PromotionResponse;
import cl.duoc.cashin.PromotionService.dto.Response.UserRemoteResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PromotionService – pruebas unitarias de lógica de negocio")
class PromotionServiceApplicationTests {

    @Mock private PromotionRepository promotionRepository;
    @Mock private CategoryServiceClient categoryServiceClient;
    @Mock private UserServiceClient userServiceClient;

    @InjectMocks
    private PromotionService promotionService;

    private PromotionModel promotionModel;
    private PromotionRequest promotionRequest;
    private CategoryRemoteResponse categoriaActiva;
    private UserRemoteResponse usuarioActivo;

    @BeforeEach
    void setUp() {
        promotionModel = new PromotionModel();
        promotionModel.setIdPromotion(1L);
        promotionModel.setCategoryId(5L);
        promotionModel.setNombreCategoria("Alimentación");
        promotionModel.setCodigo("PROMO10");
        promotionModel.setDescripcion("10% de descuento en alimentación");
        promotionModel.setTipoDescuento("PORCENTAJE");
        promotionModel.setValorDescuento(10.0);
        promotionModel.setFechaInicio(LocalDate.now().minusDays(1));
        promotionModel.setFechaFin(LocalDate.now().plusDays(30));
        promotionModel.setUsoMaximo(100);
        promotionModel.setUsosActuales(0);
        promotionModel.setActivo(true);

        categoriaActiva = new CategoryRemoteResponse();
        categoriaActiva.setIdCategory(5L);
        categoriaActiva.setNombre("Alimentación");
        categoriaActiva.setActivo(true);

        usuarioActivo = new UserRemoteResponse();
        usuarioActivo.setIdUser(10L);
        usuarioActivo.setActivo(true);

        promotionRequest = new PromotionRequest();
        promotionRequest.setCategoryId(5L);
        promotionRequest.setCodigo("PROMO10");
        promotionRequest.setDescripcion("10% de descuento en alimentación");
        promotionRequest.setTipoDescuento("PORCENTAJE");
        promotionRequest.setValorDescuento(10.0);
        promotionRequest.setFechaInicio(LocalDate.now().minusDays(1));
        promotionRequest.setFechaFin(LocalDate.now().plusDays(30));
        promotionRequest.setUsoMaximo(100);
    }

    // ── crear ────────────────────────────────────────────────────────

    @Test
    @DisplayName("crear: crea promoción correctamente con código único")
    void crear_codigoUnico_retornaPromocionCreada() {
        when(promotionRepository.existsByCodigo("PROMO10")).thenReturn(false);
        when(categoryServiceClient.obtenerCategoriaPorId(5L, "Bearer token")).thenReturn(categoriaActiva);
        when(promotionRepository.save(any(PromotionModel.class))).thenReturn(promotionModel);

        PromotionResponse respuesta = promotionService.crear(promotionRequest, "Bearer token");

        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getCodigo()).isEqualTo("PROMO10");
        assertThat(respuesta.getUsosActuales()).isEqualTo(0);
        assertThat(respuesta.getActivo()).isTrue();
        verify(promotionRepository).save(any(PromotionModel.class));
    }

    @Test
    @DisplayName("crear: lanza RuntimeException si el código ya existe")
    void crear_codigoDuplicado_lanzaExcepcion() {
        when(promotionRepository.existsByCodigo("PROMO10")).thenReturn(true);

        assertThatThrownBy(() -> promotionService.crear(promotionRequest, "Bearer token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya existe una promocion con el codigo");

        verify(promotionRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear: lanza RuntimeException si fecha inicio es posterior a fecha fin")
    void crear_fechaInicioPostFechaFin_lanzaExcepcion() {
        promotionRequest.setFechaInicio(LocalDate.now().plusDays(10));
        promotionRequest.setFechaFin(LocalDate.now());
        when(promotionRepository.existsByCodigo("PROMO10")).thenReturn(false);

        assertThatThrownBy(() -> promotionService.crear(promotionRequest, "Bearer token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("fecha de inicio no puede ser posterior");

        verify(promotionRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear: lanza RuntimeException si porcentaje supera 100%")
    void crear_porcentajeSuperaCien_lanzaExcepcion() {
        promotionRequest.setValorDescuento(110.0);
        when(promotionRepository.existsByCodigo("PROMO10")).thenReturn(false);

        assertThatThrownBy(() -> promotionService.crear(promotionRequest, "Bearer token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no puede superar el 100%");

        verify(promotionRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear: lanza RuntimeException si la categoría está inactiva")
    void crear_categoriaInactiva_lanzaExcepcion() {
        categoriaActiva.setActivo(false);
        when(promotionRepository.existsByCodigo("PROMO10")).thenReturn(false);
        when(categoryServiceClient.obtenerCategoriaPorId(5L, "Bearer token")).thenReturn(categoriaActiva);

        assertThatThrownBy(() -> promotionService.crear(promotionRequest, "Bearer token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no esta activa");

        verify(promotionRepository, never()).save(any());
    }

    // ── obtenerPorId ─────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerPorId: retorna promoción cuando existe")
    void obtenerPorId_existente_retornaPromocion() {
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotionModel));

        PromotionResponse respuesta = promotionService.obtenerPorId(1L);

        assertThat(respuesta.getIdPromotion()).isEqualTo(1L);
        assertThat(respuesta.getCodigo()).isEqualTo("PROMO10");
    }

    @Test
    @DisplayName("obtenerPorId: lanza ResourceNotFoundException si no existe")
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(promotionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> promotionService.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Promocion con id 99 no encontrada");
    }

    // ── aplicar ───────────────────────────────────────────────────────

    @Test
    @DisplayName("aplicar: calcula descuento PORCENTAJE correctamente")
    void aplicar_descuentoPorcentaje_calculaMontoFinal() {
        PromotionAplicarRequest request = new PromotionAplicarRequest();
        request.setCodigo("PROMO10");
        request.setUserId(10L);
        request.setMontoOriginal(100000.0);

        when(userServiceClient.obtenerUsuarioPorId(10L, "Bearer token")).thenReturn(usuarioActivo);
        when(promotionRepository.findActivaVigentePorCodigo(eq("PROMO10"), any(LocalDate.class)))
                .thenReturn(Optional.of(promotionModel));
        when(promotionRepository.save(any(PromotionModel.class))).thenReturn(promotionModel);

        PromotionAplicarResponse respuesta = promotionService.aplicar(request, "Bearer token");

        assertThat(respuesta.getDescuentoAplicado()).isEqualTo(10000.0); // 10% de 100000
        assertThat(respuesta.getMontoFinal()).isEqualTo(90000.0);
        assertThat(respuesta.getUsosRestantes()).isEqualTo(99);
    }

    @Test
    @DisplayName("aplicar: descuento FIJO no puede resultar en monto negativo")
    void aplicar_descuentoFijoMayorMonto_retornaCero() {
        promotionModel.setTipoDescuento("FIJO");
        promotionModel.setValorDescuento(200000.0);

        PromotionAplicarRequest request = new PromotionAplicarRequest();
        request.setCodigo("PROMO10");
        request.setUserId(10L);
        request.setMontoOriginal(100000.0);

        when(userServiceClient.obtenerUsuarioPorId(10L, "Bearer token")).thenReturn(usuarioActivo);
        when(promotionRepository.findActivaVigentePorCodigo(eq("PROMO10"), any(LocalDate.class)))
                .thenReturn(Optional.of(promotionModel));
        when(promotionRepository.save(any(PromotionModel.class))).thenReturn(promotionModel);

        PromotionAplicarResponse respuesta = promotionService.aplicar(request, "Bearer token");

        assertThat(respuesta.getMontoFinal()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("aplicar: lanza RuntimeException si la promoción está agotada")
    void aplicar_promocionAgotada_lanzaExcepcion() {
        promotionModel.setUsosActuales(100);
        promotionModel.setUsoMaximo(100);

        PromotionAplicarRequest request = new PromotionAplicarRequest();
        request.setCodigo("PROMO10");
        request.setUserId(10L);
        request.setMontoOriginal(100000.0);

        when(userServiceClient.obtenerUsuarioPorId(10L, "Bearer token")).thenReturn(usuarioActivo);
        when(promotionRepository.findActivaVigentePorCodigo(eq("PROMO10"), any(LocalDate.class)))
                .thenReturn(Optional.of(promotionModel));

        assertThatThrownBy(() -> promotionService.aplicar(request, "Bearer token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("agotado todos sus usos");
    }

    @Test
    @DisplayName("aplicar: lanza RuntimeException si el usuario está inactivo")
    void aplicar_usuarioInactivo_lanzaExcepcion() {
        usuarioActivo.setActivo(false);
        PromotionAplicarRequest request = new PromotionAplicarRequest();
        request.setCodigo("PROMO10");
        request.setUserId(10L);
        request.setMontoOriginal(100000.0);

        when(userServiceClient.obtenerUsuarioPorId(10L, "Bearer token")).thenReturn(usuarioActivo);

        assertThatThrownBy(() -> promotionService.aplicar(request, "Bearer token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no esta activo");
    }

    // ── desactivar ────────────────────────────────────────────────────

    @Test
    @DisplayName("desactivar: cambia activo a false")
    void desactivar_activa_desactivaCorrectamente() {
        PromotionModel desactivada = new PromotionModel();
        desactivada.setIdPromotion(1L);
        desactivada.setActivo(false);
        desactivada.setCodigo("PROMO10");
        desactivada.setFechaInicio(LocalDate.now());
        desactivada.setFechaFin(LocalDate.now().plusDays(30));

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotionModel));
        when(promotionRepository.save(any(PromotionModel.class))).thenReturn(desactivada);

        PromotionResponse respuesta = promotionService.desactivar(1L);

        assertThat(respuesta.getActivo()).isFalse();
    }

    @Test
    @DisplayName("desactivar: lanza RuntimeException si ya está desactivada")
    void desactivar_yaDesactivada_lanzaExcepcion() {
        promotionModel.setActivo(false);
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotionModel));

        assertThatThrownBy(() -> promotionService.desactivar(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ya esta desactivada");

        verify(promotionRepository, never()).save(any());
    }

    // ── eliminar ──────────────────────────────────────────────────────

    @Test
    @DisplayName("eliminar: llama deleteById cuando la promoción existe")
    void eliminar_existente_eliminaCorrectamente() {
        when(promotionRepository.existsById(1L)).thenReturn(true);

        promotionService.eliminar(1L);

        verify(promotionRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar: lanza ResourceNotFoundException si no existe")
    void eliminar_inexistente_lanzaExcepcion() {
        when(promotionRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> promotionService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Promocion con id 99 no existe");

        verify(promotionRepository, never()).deleteById(any());
    }
}
