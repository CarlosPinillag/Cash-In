package cl.duoc.cashin.ExpenseService.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cl.duoc.cashin.ExpenseService.Client.CategoryServiceClient;
import cl.duoc.cashin.ExpenseService.Client.UserServiceClient;
import cl.duoc.cashin.ExpenseService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.ExpenseService.Model.ExpenseModel;
import cl.duoc.cashin.ExpenseService.Repository.ExpenseRepository;
import cl.duoc.cashin.ExpenseService.dto.Request.ExpenseRequest;
import cl.duoc.cashin.ExpenseService.dto.Request.ExpenseUpdateRequest;
import cl.duoc.cashin.ExpenseService.dto.Response.CategoryRemoteResponse;
import cl.duoc.cashin.ExpenseService.dto.Response.ExpenseResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class ExpenseService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseService.class);

    private final ExpenseRepository expenseRepository;
    private final UserServiceClient userServiceClient;
    private final CategoryServiceClient categoryServiceClient;

    private ExpenseResponse mapToResponse(ExpenseModel model) {
        ExpenseResponse response = new ExpenseResponse();
        response.setIdExpense(model.getIdExpense());
        response.setUserId(model.getUserId());
        response.setCategoryId(model.getCategoryId());
        response.setNombreCategoria(model.getNombreCategoria());
        response.setMonto(model.getMonto());
        response.setDescripcion(model.getDescripcion());
        response.setFecha(model.getFecha());
        response.setTipo(model.getTipo());
        return response;
    }

    // CREAR
    public ExpenseResponse crear(ExpenseRequest request) {
        log.info("Creando gasto para userId: {} y categoryId: {}",
                request.getUserId(), request.getCategoryId());

        // Regla 1:
        userServiceClient.obtenerUsuarioPorId(request.getUserId());
        log.info("Usuario id: {} validado en user-service", request.getUserId());

        // Regla 2:
        CategoryRemoteResponse categoria = categoryServiceClient.obtenerCategoriaPorId(request.getCategoryId());
        log.info("Categoria id: {} validada en category-service: {}", request.getCategoryId(), categoria.getNombre());

        // Regla 3:
        if (!categoria.getActivo()) {
            throw new RuntimeException(
                    "La categoria con id " + request.getCategoryId() + " no esta activa");
        }

        // Construir la entidad con todos los campos
        ExpenseModel model = new ExpenseModel();
        model.setUserId(request.getUserId());
        model.setCategoryId(request.getCategoryId());
        model.setNombreCategoria(categoria.getNombre()); // se guarda para evitar consultas futuras
        model.setMonto(request.getMonto());
        model.setDescripcion(request.getDescripcion());
        model.setFecha(request.getFecha());
        model.setTipo(request.getTipo());

        ExpenseModel guardado = expenseRepository.save(model);
        log.info("Gasto creado con id: {}", guardado.getIdExpense());
        return mapToResponse(guardado);
    }

    // OBTENER POR ID
    public ExpenseResponse obtenerPorId(Long id) {
        log.info("Buscando gasto id: {}", id);

        ExpenseModel model = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Gasto con id " + id + " no encontrado"));

        return mapToResponse(model);
    }

    // LISTAR POR USUARIO
    public List<ExpenseResponse> listarPorUsuario(Long userId) {
        log.info("Listando gastos del usuario id: {}", userId);

        return expenseRepository.findByUserId(userId)
                .stream() // convierte List en Stream
                .map(this::mapToResponse) // aplica mapToResponse a cada elemento
                .collect(Collectors.toList()); // vuelve a convertir en List
    }

    // TOTAL GASTADO POR USUARIO
    // Endpoint usado por budget-service y analytics-service
    public Double obtenerTotalPorUsuario(Long userId) {
        log.info("Calculando total gastado para userId: {}", userId);

        Double total = expenseRepository.sumMontoByUserId(userId);

        return total != null ? total : 0.0;
    }

    // ACTUALIZAR
    public ExpenseResponse actualizar(Long id, ExpenseUpdateRequest request) {
        log.info("Actualizando gasto id: {}", id);

        ExpenseModel model = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Gasto con id " + id + " no encontrado"));

        if (request.getCategoryId() != null) {
            CategoryRemoteResponse categoria = categoryServiceClient
                    .obtenerCategoriaPorId(request.getCategoryId());

            if (!categoria.getActivo()) {
                throw new RuntimeException(
                        "La categoria con id " + request.getCategoryId() + " no esta activa");
            }

            model.setCategoryId(request.getCategoryId());
            model.setNombreCategoria(categoria.getNombre());
        }

        // ACTUALIZAR SOLO CAMPOS NO NULL
        if (request.getMonto() != null) {
            model.setMonto(request.getMonto());
        }
        if (request.getDescripcion() != null) {
            model.setDescripcion(request.getDescripcion());
        }
        if (request.getFecha() != null) {
            model.setFecha(request.getFecha());
        }
        if (request.getTipo() != null) {
            model.setTipo(request.getTipo());
        }

        ExpenseModel actualizado = expenseRepository.save(model);
        log.info("Gasto id: {} actualizado exitosamente", id);
        return mapToResponse(actualizado);
    }

    // ELIMINAR

    public void eliminar(Long id) {
        log.info("Eliminando gasto id: {}", id);

        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Gasto con id " + id + " no existe");
        }

        expenseRepository.deleteById(id);
        log.info("Gasto id: {} eliminado exitosamente", id);
    }
}
