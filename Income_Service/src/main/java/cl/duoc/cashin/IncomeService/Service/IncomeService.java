package cl.duoc.cashin.IncomeService.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cl.duoc.cashin.IncomeService.Client.UserServiceClient;
import cl.duoc.cashin.IncomeService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.IncomeService.Model.IncomeModel;
import cl.duoc.cashin.IncomeService.Repository.IncomeRepository;
import cl.duoc.cashin.IncomeService.dto.Request.IncomeRequest;
import cl.duoc.cashin.IncomeService.dto.Request.IncomeUpdateRequest;
import cl.duoc.cashin.IncomeService.dto.Response.IncomeResponse;
import lombok.RequiredArgsConstructor;

@Service // registra esta clase como Bean de lógica de negocio
@RequiredArgsConstructor // Lombok genera constructor con los atributos 'final'

public class IncomeService {

    // Logger SLF4J — siempre usar esto, NUNCA System.out.println
    private static final Logger log = LoggerFactory.getLogger(IncomeService.class);

    private final IncomeRepository incomeRepository;
    private final UserServiceClient userServiceClient;

    // ── MAPPER privado: IncomeModel → IncomeResponse ────────────────
    // Convierte la entidad JPA en el DTO que se devuelve al cliente
    // Es privado porque solo el service lo usa
    private IncomeResponse mapToResponse(IncomeModel model) {
        IncomeResponse response = new IncomeResponse();
        response.setIdIncome(model.getIdIncome());
        response.setUserId(model.getUserId());
        response.setMonto(model.getMonto());
        response.setDescripcion(model.getDescripcion());
        response.setCategoria(model.getCategoria());
        response.setFecha(model.getFecha());
        response.setRecurrente(model.getRecurrente());
        response.setFrecuencia(model.getFrecuencia());
        return response;
    }

    // ── CREAR ───────────────────────────────────────────────────────────
    public IncomeResponse crear(IncomeRequest request) {
        log.info("Creando ingreso para userId: {} categoria: {}",
                request.getUserId(), request.getCategoria());

        // Regla 1: Llamar a user-service para verificar que el usuario existe
        // Si retorna 404 → ResourceNotFoundException propagada automáticamente
        userServiceClient.obtenerUsuarioPorId(request.getUserId());
        log.info("Usuario id: {} validado en user-service", request.getUserId());

        // Regla 2: Si el ingreso es recurrente, la frecuencia es obligatoria
        if (Boolean.TRUE.equals(request.getRecurrente())) {
            if (request.getFrecuencia() == null || request.getFrecuencia().isBlank()) {
                throw new RuntimeException(
                        "LA FRECUENCIA ES OBLIGATORIA CUANDO EL INGRESO ES RECURRENTE. " +
                        "Valores aceptados: MENSUAL, SEMANAL, QUINCENAL");
            }
            // Regla 3: Validar que la frecuencia sea un valor permitido
            String frecuencia = request.getFrecuencia().toUpperCase();
            if (!frecuencia.equals("MENSUAL") && !frecuencia.equals("SEMANAL") && !frecuencia.equals("QUINCENAL")) {
                throw new RuntimeException(
                        "FRECUENCIA INVALIDA: " + request.getFrecuencia() +
                        ". Valores aceptados: MENSUAL, SEMANAL, QUINCENAL");
            }
        }

        // Construir la entidad con todos los campos
        IncomeModel model = new IncomeModel();
        model.setUserId(request.getUserId());
        model.setMonto(request.getMonto());
        model.setDescripcion(request.getDescripcion());
        model.setCategoria(request.getCategoria().toUpperCase());
        model.setFecha(request.getFecha());
        model.setRecurrente(request.getRecurrente());
        // Si no es recurrente, guardar null en frecuencia
        model.setFrecuencia(Boolean.TRUE.equals(request.getRecurrente())
                ? request.getFrecuencia().toUpperCase()
                : null);

        IncomeModel guardado = incomeRepository.save(model);
        log.info("Ingreso creado con id: {}", guardado.getIdIncome());
        return mapToResponse(guardado);
    }

    // ── OBTENER POR ID ───────────────────────────────────────────────────
    public IncomeResponse obtenerPorId(Long id) {
        log.info("Buscando ingreso id: {}", id);

        IncomeModel model = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingreso con id " + id + " no encontrado"));
        // findById retorna Optional<IncomeModel>
        // orElseThrow: si el Optional está vacío, lanza la excepción
        // GlobalExceptionHandler la captura → HTTP 404

        return mapToResponse(model);
    }

    // ── LISTAR POR USUARIO ───────────────────────────────────────────────
    public List<IncomeResponse> listarPorUsuario(Long userId) {
        log.info("Listando ingresos del usuario id: {}", userId);

        return incomeRepository.findByUserId(userId)
                .stream()                       // convierte List en Stream
                .map(this::mapToResponse)       // aplica mapToResponse a cada elemento
                .collect(Collectors.toList());  // vuelve a convertir en List
    }

    // ── TOTAL DE INGRESOS POR USUARIO ────────────────────────────────────
    // Endpoint consumido por analytics-service para calcular el balance financiero
    public Double obtenerTotalPorUsuario(Long userId) {
        log.info("Calculando total de ingresos para userId: {}", userId);

        Double total = incomeRepository.sumMontoByUserId(userId);
        // Si no hay ingresos registrados, sumMontoByUserId retorna null → devolver 0.0
        return total != null ? total : 0.0;
    }

    // ── ACTUALIZAR ───────────────────────────────────────────────────────
    public IncomeResponse actualizar(Long id, IncomeUpdateRequest request) {
        log.info("Actualizando ingreso id: {}", id);

        IncomeModel model = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingreso con id " + id + " no encontrado"));

        // ── ACTUALIZAR SOLO CAMPOS NO NULL ──────────────────────────────
        if (request.getMonto() != null) {
            model.setMonto(request.getMonto());
        }
        if (request.getDescripcion() != null) {
            model.setDescripcion(request.getDescripcion());
        }
        if (request.getCategoria() != null) {
            model.setCategoria(request.getCategoria().toUpperCase());
        }
        if (request.getFecha() != null) {
            model.setFecha(request.getFecha());
        }
        if (request.getRecurrente() != null) {
            model.setRecurrente(request.getRecurrente());
        }
        if (request.getFrecuencia() != null) {
            model.setFrecuencia(request.getFrecuencia().toUpperCase());
        }

        // Regla: Si después del update recurrente=true y frecuencia es null → error
        if (Boolean.TRUE.equals(model.getRecurrente()) &&
                (model.getFrecuencia() == null || model.getFrecuencia().isBlank())) {
            throw new RuntimeException(
                    "LA FRECUENCIA ES OBLIGATORIA CUANDO EL INGRESO ES RECURRENTE");
        }

        // Si se cambia recurrente a false, limpiar frecuencia
        if (Boolean.FALSE.equals(model.getRecurrente())) {
            model.setFrecuencia(null);
        }

        IncomeModel actualizado = incomeRepository.save(model);
        log.info("Ingreso id: {} actualizado exitosamente", id);
        return mapToResponse(actualizado);
    }

    // ── ELIMINAR ─────────────────────────────────────────────────────────
    // Eliminación física: los ingresos sí se pueden borrar
    public void eliminar(Long id) {
        log.info("Eliminando ingreso id: {}", id);

        if (!incomeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ingreso con id " + id + " no existe");
        }

        incomeRepository.deleteById(id);
        log.info("Ingreso id: {} eliminado exitosamente", id);
    }
}
