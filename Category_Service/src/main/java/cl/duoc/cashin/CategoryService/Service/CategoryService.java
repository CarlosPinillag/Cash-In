package cl.duoc.cashin.CategoryService.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cl.duoc.cashin.CategoryService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.CategoryService.Model.CategoryModel;
import cl.duoc.cashin.CategoryService.Repository.CategoryRepository;
import cl.duoc.cashin.CategoryService.dto.Request.CategoryRequest;
import cl.duoc.cashin.CategoryService.dto.Request.CategoryUpdateRequest;
import cl.duoc.cashin.CategoryService.dto.Response.CategoryResponse;
import lombok.RequiredArgsConstructor;

@Service // registra esta clase como Bean de lógica de negocio
@RequiredArgsConstructor // Lombok genera constructor con los atributos 'final'

public class CategoryService {

    // Logger SLF4J — siempre usar esto, NUNCA System.out.println
    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;

    // ── MAPPER privado: CategoryModel → CategoryResponse ──────────────
    // Convierte la entidad JPA en el DTO que se devuelve al cliente
    // Es privado porque solo el service lo usa
    private CategoryResponse mapToResponse(CategoryModel model) {
        CategoryResponse response = new CategoryResponse();
        response.setIdCategory(model.getIdCategory());
        response.setNombre(model.getNombre());
        response.setDescripcion(model.getDescripcion());
        response.setTipo(model.getTipo());
        response.setActivo(model.getActivo());
        return response;
    }

    // ── CREAR ──────────────────────────────────────────────────────────
    public CategoryResponse crear(CategoryRequest request) {
        log.info("Creando categoria con nombre: {} y tipo: {}", request.getNombre(), request.getTipo());

        // Regla 1: No puede existir otra categoría con el mismo nombre (case-insensitive)
        categoryRepository.findByNombre(request.getNombre()).ifPresent(existente -> {
            throw new RuntimeException(
                    "Ya existe una categoria con el nombre: " + request.getNombre());
        });

        // Construir entidad con activo=true por defecto al crear
        CategoryModel model = new CategoryModel();
        model.setNombre(request.getNombre());
        model.setDescripcion(request.getDescripcion());
        model.setTipo(request.getTipo());
        model.setActivo(true); // toda categoría nueva nace activa

        CategoryModel guardado = categoryRepository.save(model);
        log.info("Categoria creada con id: {}", guardado.getIdCategory());
        return mapToResponse(guardado);
    }

    // ── OBTENER POR ID ─────────────────────────────────────────────────
    public CategoryResponse obtenerPorId(Long id) {
        log.info("Buscando categoria id: {}", id);

        CategoryModel model = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoria con id " + id + " no encontrada"));
        // findById retorna Optional<CategoryModel>
        // orElseThrow: si el Optional está vacío, lanza la excepción
        // GlobalExceptionHandler la captura → HTTP 404

        return mapToResponse(model);
    }

    // ── LISTAR TODAS ───────────────────────────────────────────────────
    public List<CategoryResponse> listarTodas() {
        log.info("Listando todas las categorias");

        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── LISTAR POR ESTADO (activo/inactivo) ────────────────────────────
    public List<CategoryResponse> listarPorActivo(Boolean activo) {
        log.info("Listando categorias con activo: {}", activo);

        return categoryRepository.findByActivo(activo)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── LISTAR POR TIPO ────────────────────────────────────────────────
    // Endpoint consumido por expense-service e income-service para mostrar
    // solo las categorías disponibles para su tipo de operación
    public List<CategoryResponse> listarPorTipo(String tipo) {
        log.info("Listando categorias de tipo: {}", tipo);

        // Regla: el tipo debe ser GASTO o INGRESO
        if (!tipo.equals("GASTO") && !tipo.equals("INGRESO")) {
            throw new RuntimeException("El tipo debe ser GASTO o INGRESO");
        }

        return categoryRepository.findByTipoAndActivo(tipo, true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── ACTUALIZAR ─────────────────────────────────────────────────────
    public CategoryResponse actualizar(Long id, CategoryUpdateRequest request) {
        log.info("Actualizando categoria id: {}", id);

        CategoryModel model = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoria con id " + id + " no encontrada"));

        // Si viene nuevo nombre, verificar que no esté ya en uso por otra categoría
        if (request.getNombre() != null) {
            categoryRepository.findByNombre(request.getNombre()).ifPresent(existente -> {
                // Solo es conflicto si es una categoría distinta a la actual
                if (!existente.getIdCategory().equals(id)) {
                    throw new RuntimeException(
                            "Ya existe una categoria con el nombre: " + request.getNombre());
                }
            });
            model.setNombre(request.getNombre());
        }

        // ── ACTUALIZAR SOLO CAMPOS NO NULL ──────────────────────────────
        if (request.getDescripcion() != null) {
            model.setDescripcion(request.getDescripcion());
        }
        if (request.getTipo() != null) {
            model.setTipo(request.getTipo());
        }
        if (request.getActivo() != null) {
            model.setActivo(request.getActivo());
            log.info("Categoria id: {} cambia estado activo a: {}", id, request.getActivo());
        }

        CategoryModel actualizado = categoryRepository.save(model);
        log.info("Categoria id: {} actualizada exitosamente", id);
        return mapToResponse(actualizado);
    }

    // ── DESACTIVAR (soft-delete) ───────────────────────────────────────
    // No se elimina físicamente: los registros de expense/income mantienen
    // su categoryId y nombreCategoria válidos gracias a la desnormalización
    public void desactivar(Long id) {
        log.info("Desactivando categoria id: {}", id);

        CategoryModel model = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoria con id " + id + " no existe"));

        // Regla: no se puede desactivar una categoría ya inactiva
        if (!model.getActivo()) {
            throw new RuntimeException("La categoria con id " + id + " ya esta desactivada");
        }

        model.setActivo(false);
        categoryRepository.save(model);
        log.info("Categoria id: {} desactivada exitosamente", id);
    }

    // ── ELIMINAR ────────────────────────────────────────────────────────
    // Eliminación física — solo para casos donde no hay dependencias externas
    public void eliminar(Long id) {
        log.info("Eliminando categoria id: {}", id);

        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoria con id " + id + " no existe");
        }

        categoryRepository.deleteById(id);
        log.info("Categoria id: {} eliminada exitosamente", id);
    }
}
