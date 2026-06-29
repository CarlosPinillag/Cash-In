package cl.duoc.cashin.CategoryService;

// ══════════════════════════════════════════════════════════════════
// AGREGADO: Pruebas unitarias reales para CategoryService (IE 3.1.1)
// Motivo: el archivo original sólo tenía contextLoads() vacío;
//         esta clase cubre la lógica de negocio con JUnit 5 + Mockito.
// Ubicación destino:
//   Category_Service/src/test/java/cl/duoc/cashin/CategoryService/CategoryServiceApplicationTests.java
// ══════════════════════════════════════════════════════════════════

import cl.duoc.cashin.CategoryService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.CategoryService.Model.CategoryModel;
import cl.duoc.cashin.CategoryService.Repository.CategoryRepository;
import cl.duoc.cashin.CategoryService.Service.CategoryService;
import cl.duoc.cashin.CategoryService.dto.Request.CategoryRequest;
import cl.duoc.cashin.CategoryService.dto.Request.CategoryUpdateRequest;
import cl.duoc.cashin.CategoryService.dto.Response.CategoryResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService – pruebas unitarias de lógica de negocio")
class CategoryServiceApplicationTests {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private CategoryModel categoryModel;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        categoryModel = new CategoryModel();
        categoryModel.setIdCategory(1L);
        categoryModel.setNombre("Alimentación");
        categoryModel.setDescripcion("Gastos de alimentación diaria");
        categoryModel.setTipo("GASTO");
        categoryModel.setActivo(true);

        categoryRequest = new CategoryRequest();
        categoryRequest.setNombre("Alimentación");
        categoryRequest.setDescripcion("Gastos de alimentación diaria");
        categoryRequest.setTipo("GASTO");
    }

    // ── crear ────────────────────────────────────────────────────────

    @Test
    @DisplayName("crear: crea categoría correctamente con nombre único")
    void crear_nombreUnico_retornaCategoriaCreada() {
        when(categoryRepository.findByNombre("Alimentación")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(CategoryModel.class))).thenReturn(categoryModel);

        CategoryResponse respuesta = categoryService.crear(categoryRequest);

        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getNombre()).isEqualTo("Alimentación");
        assertThat(respuesta.getTipo()).isEqualTo("GASTO");
        assertThat(respuesta.getActivo()).isTrue(); // toda categoría nace activa

        verify(categoryRepository).save(any(CategoryModel.class));
    }

    @Test
    @DisplayName("crear: lanza RuntimeException si el nombre ya existe")
    void crear_nombreDuplicado_lanzaExcepcion() {
        when(categoryRepository.findByNombre("Alimentación")).thenReturn(Optional.of(categoryModel));

        assertThatThrownBy(() -> categoryService.crear(categoryRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya existe una categoria con el nombre: Alimentación");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear: categoría nueva siempre nace con activo=true")
    void crear_siempreNaceActiva() {
        when(categoryRepository.findByNombre(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(CategoryModel.class))).thenAnswer(inv -> {
            CategoryModel m = inv.getArgument(0);
            assertThat(m.getActivo()).isTrue(); // verificación en el momento de guardar
            return categoryModel;
        });

        categoryService.crear(categoryRequest);

        verify(categoryRepository).save(any(CategoryModel.class));
    }

    // ── obtenerPorId ─────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerPorId: retorna categoría cuando existe")
    void obtenerPorId_existente_retornaCategoria() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryModel));

        CategoryResponse respuesta = categoryService.obtenerPorId(1L);

        assertThat(respuesta.getIdCategory()).isEqualTo(1L);
        assertThat(respuesta.getNombre()).isEqualTo("Alimentación");
    }

    @Test
    @DisplayName("obtenerPorId: lanza ResourceNotFoundException si no existe")
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Categoria con id 99 no encontrada");
    }

    // ── listarTodas ──────────────────────────────────────────────────

    @Test
    @DisplayName("listarTodas: retorna todas las categorías")
    void listarTodas_conCategorias_retornaLista() {
        when(categoryRepository.findAll()).thenReturn(List.of(categoryModel));

        List<CategoryResponse> resultado = categoryService.listarTodas();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Alimentación");
    }

    @Test
    @DisplayName("listarTodas: retorna lista vacía cuando no hay categorías")
    void listarTodas_sinCategorias_retornaListaVacia() {
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

        List<CategoryResponse> resultado = categoryService.listarTodas();

        assertThat(resultado).isEmpty();
    }

    // ── listarPorActivo ──────────────────────────────────────────────

    @Test
    @DisplayName("listarPorActivo: filtra sólo las categorías activas")
    void listarPorActivo_activas_retornaListaFiltrada() {
        when(categoryRepository.findByActivo(true)).thenReturn(List.of(categoryModel));

        List<CategoryResponse> resultado = categoryService.listarPorActivo(true);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getActivo()).isTrue();
    }

    // ── listarPorTipo ─────────────────────────────────────────────────

    @Test
    @DisplayName("listarPorTipo: retorna categorías de tipo GASTO correctamente")
    void listarPorTipo_tipoValido_retornaLista() {
        when(categoryRepository.findByTipoAndActivo("GASTO", true)).thenReturn(List.of(categoryModel));

        List<CategoryResponse> resultado = categoryService.listarPorTipo("GASTO");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipo()).isEqualTo("GASTO");
    }

    @Test
    @DisplayName("listarPorTipo: lanza RuntimeException para tipo inválido (no es GASTO ni INGRESO)")
    void listarPorTipo_tipoInvalido_lanzaExcepcion() {
        assertThatThrownBy(() -> categoryService.listarPorTipo("OTRO"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El tipo debe ser GASTO o INGRESO");
    }

    @Test
    @DisplayName("listarPorTipo: acepta INGRESO como tipo válido")
    void listarPorTipo_tipoIngreso_esValido() {
        when(categoryRepository.findByTipoAndActivo("INGRESO", true)).thenReturn(Collections.emptyList());

        assertThatNoException().isThrownBy(() -> categoryService.listarPorTipo("INGRESO"));
    }

    // ── actualizar ───────────────────────────────────────────────────

    @Test
    @DisplayName("actualizar: actualiza nombre, descripción y tipo correctamente")
    void actualizar_datosValidos_actualizaCategoria() {
        CategoryUpdateRequest update = new CategoryUpdateRequest();
        update.setNombre("Transporte");
        update.setDescripcion("Gastos de transporte");
        update.setTipo("GASTO");

        CategoryModel actualizado = new CategoryModel(1L, "Transporte", "Gastos de transporte", "GASTO", true);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryModel));
        when(categoryRepository.findByNombre("Transporte")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(CategoryModel.class))).thenReturn(actualizado);

        CategoryResponse respuesta = categoryService.actualizar(1L, update);

        assertThat(respuesta.getNombre()).isEqualTo("Transporte");
        verify(categoryRepository).save(any(CategoryModel.class));
    }

    @Test
    @DisplayName("actualizar: lanza RuntimeException si el nuevo nombre ya lo usa otra categoría")
    void actualizar_nombreDuplicadoEnOtra_lanzaExcepcion() {
        CategoryUpdateRequest update = new CategoryUpdateRequest();
        update.setNombre("Transporte");

        CategoryModel otra = new CategoryModel(99L, "Transporte", "desc", "GASTO", true);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryModel));
        when(categoryRepository.findByNombre("Transporte")).thenReturn(Optional.of(otra));

        assertThatThrownBy(() -> categoryService.actualizar(1L, update))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya existe una categoria con el nombre: Transporte");
    }

    @Test
    @DisplayName("actualizar: permite usar el mismo nombre si pertenece a la misma categoría")
    void actualizar_mismoNombreMismaCategoria_noLanzaExcepcion() {
        CategoryUpdateRequest update = new CategoryUpdateRequest();
        update.setNombre("Alimentación"); // mismo nombre, misma id → no conflicto

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryModel));
        when(categoryRepository.findByNombre("Alimentación")).thenReturn(Optional.of(categoryModel));
        when(categoryRepository.save(any(CategoryModel.class))).thenReturn(categoryModel);

        assertThatNoException().isThrownBy(() -> categoryService.actualizar(1L, update));
    }

    // ── desactivar ───────────────────────────────────────────────────

    @Test
    @DisplayName("desactivar: cambia activo a false si estaba activa")
    void desactivar_categoriaActiva_desactivaCorrectamente() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryModel));
        when(categoryRepository.save(any(CategoryModel.class))).thenReturn(categoryModel);

        categoryService.desactivar(1L);

        assertThat(categoryModel.getActivo()).isFalse();
        verify(categoryRepository).save(categoryModel);
    }

    @Test
    @DisplayName("desactivar: lanza RuntimeException si ya estaba desactivada")
    void desactivar_categoriaYaDesactivada_lanzaExcepcion() {
        categoryModel.setActivo(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryModel));

        assertThatThrownBy(() -> categoryService.desactivar(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ya esta desactivada");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("desactivar: lanza ResourceNotFoundException si no existe")
    void desactivar_inexistente_lanzaExcepcion() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.desactivar(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── eliminar ──────────────────────────────────────────────────────

    @Test
    @DisplayName("eliminar: llama deleteById cuando la categoría existe")
    void eliminar_existente_eliminaCorrectamente() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        categoryService.eliminar(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar: lanza ResourceNotFoundException si no existe")
    void eliminar_inexistente_lanzaExcepcion() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Categoria con id 99 no existe");

        verify(categoryRepository, never()).deleteById(any());
    }
}
