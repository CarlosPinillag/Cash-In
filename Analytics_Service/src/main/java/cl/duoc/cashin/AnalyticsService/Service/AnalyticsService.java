package cl.duoc.cashin.AnalyticsService.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cl.duoc.cashin.AnalyticsService.Client.BudgetServiceClient;
import cl.duoc.cashin.AnalyticsService.Client.ExpenseServiceClient;
import cl.duoc.cashin.AnalyticsService.Client.IncomeServiceClient;
import cl.duoc.cashin.AnalyticsService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.AnalyticsService.Model.AnalyticsModel;
import cl.duoc.cashin.AnalyticsService.Repository.AnalyticsRepository;
import cl.duoc.cashin.AnalyticsService.dto.Request.AnalyticsRequest;
import cl.duoc.cashin.AnalyticsService.dto.Response.AnalyticsResponse;
import cl.duoc.cashin.AnalyticsService.dto.Response.BudgetRemoteResponse;
import cl.duoc.cashin.AnalyticsService.dto.Response.ResumenFinancieroResponse;
import lombok.RequiredArgsConstructor;

@Service // registra esta clase como Bean de lógica de negocio
@RequiredArgsConstructor // Lombok genera constructor con los atributos 'final'

public class AnalyticsService {

    // Logger SLF4J — siempre usar esto, NUNCA System.out.println
    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final AnalyticsRepository analyticsRepository;
    private final ExpenseServiceClient expenseServiceClient;
    private final IncomeServiceClient incomeServiceClient;
    private final BudgetServiceClient budgetServiceClient;

    // ── MAPPER privado: AnalyticsModel → AnalyticsResponse ──────────
    // Convierte la entidad JPA en el DTO que se devuelve al cliente
    // Es privado porque solo el service lo usa
    private AnalyticsResponse mapToResponse(AnalyticsModel model) {
        AnalyticsResponse response = new AnalyticsResponse();
        response.setIdAnalytics(model.getIdAnalytics());
        response.setUserId(model.getUserId());
        response.setTotalIngresos(model.getTotalIngresos());
        response.setTotalGastos(model.getTotalGastos());
        response.setBalance(model.getBalance());
        response.setTasaAhorro(model.getTasaAhorro());
        response.setEstadoBalance(model.getEstadoBalance());
        response.setFechaGeneracion(model.getFechaGeneracion());
        return response;
    }

    // ── HELPER privado: calcular estado del balance ──────────────────
    // POSITIVO: ingresos > gastos
    // NEGATIVO: ingresos < gastos
    // EQUILIBRADO: ingresos = gastos (balance exactamente en 0)
    private String calcularEstadoBalance(Double balance) {
        if (balance > 0) return "POSITIVO";
        if (balance < 0) return "NEGATIVO";
        return "EQUILIBRADO";
    }

    // ── HELPER privado: generar recomendación según estado ──────────
    // Genera un mensaje interpretativo según la tasa de ahorro del usuario
    private String generarRecomendacion(String estadoBalance, Double tasaAhorro) {
        if ("NEGATIVO".equals(estadoBalance)) {
            return "Tus gastos superan tus ingresos. Revisa tus presupuestos y reduce gastos no esenciales.";
        }
        if ("EQUILIBRADO".equals(estadoBalance)) {
            return "Tus ingresos y gastos están equilibrados. Considera crear un fondo de ahorro.";
        }
        // POSITIVO — evaluar si el ahorro es suficiente
        if (tasaAhorro >= 20) {
            return "Excelente gestión financiera. Tu tasa de ahorro supera el 20%.";
        }
        if (tasaAhorro >= 10) {
            return "Buena gestión financiera. Intenta aumentar tu tasa de ahorro por encima del 20%.";
        }
        return "Tienes balance positivo pero tu tasa de ahorro es baja. Considera reducir gastos variables.";
    }

    // ── GENERAR ANÁLISIS ─────────────────────────────────────────────
    // Consulta expense-service e income-service, calcula métricas y persiste un snapshot
    public AnalyticsResponse generarAnalisis(AnalyticsRequest request) {
        log.info("Generando análisis financiero para userId: {}", request.getUserId());

        // Paso 1: Obtener total de ingresos desde income-service
        Double totalIngresos = incomeServiceClient.obtenerTotalIngresosPorUsuario(request.getUserId());
        log.info("Total ingresos para userId {}: {}", request.getUserId(), totalIngresos);

        // Paso 2: Obtener total de gastos desde expense-service
        Double totalGastos = expenseServiceClient.obtenerTotalGastadoPorUsuario(request.getUserId());
        log.info("Total gastos para userId {}: {}", request.getUserId(), totalGastos);

        // Paso 3: Calcular balance y tasa de ahorro
        // Balance = ingresos - gastos (puede ser negativo)
        Double balance = totalIngresos - totalGastos;

        // Tasa de ahorro = ((ingresos - gastos) / ingresos) * 100
        // Si totalIngresos = 0 → no se puede calcular → se guarda 0.0
        Double tasaAhorro = totalIngresos > 0
                ? ((totalIngresos - totalGastos) / totalIngresos) * 100
                : 0.0;

        // Paso 4: Determinar estado del balance
        String estadoBalance = calcularEstadoBalance(balance);
        log.info("Balance para userId {}: {} — Estado: {}", request.getUserId(), balance, estadoBalance);

        // Paso 5: Construir y persistir el snapshot del análisis
        AnalyticsModel model = new AnalyticsModel();
        model.setUserId(request.getUserId());
        model.setTotalIngresos(totalIngresos);
        model.setTotalGastos(totalGastos);
        model.setBalance(balance);
        model.setTasaAhorro(tasaAhorro);
        model.setEstadoBalance(estadoBalance);
        model.setFechaGeneracion(LocalDate.now());

        AnalyticsModel guardado = analyticsRepository.save(model);
        log.info("Análisis financiero guardado con id: {} para userId: {}",
                guardado.getIdAnalytics(), guardado.getUserId());

        return mapToResponse(guardado);
    }

    // ── OBTENER POR ID ───────────────────────────────────────────────
    public AnalyticsResponse obtenerPorId(Long id) {
        log.info("Buscando análisis id: {}", id);

        AnalyticsModel model = analyticsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Análisis con id " + id + " no encontrado"));
        // findById retorna Optional<AnalyticsModel>
        // orElseThrow: si el Optional está vacío, lanza la excepción
        // GlobalExceptionHandler la captura → HTTP 404

        return mapToResponse(model);
    }

    // ── HISTORIAL POR USUARIO ─────────────────────────────────────────
    // Retorna todos los snapshots de análisis de un usuario, del más reciente al más antiguo
    public List<AnalyticsResponse> obtenerHistorialPorUsuario(Long userId) {
        log.info("Obteniendo historial de análisis para userId: {}", userId);

        return analyticsRepository.findByUserIdOrderByFechaGeneracionDesc(userId)
                .stream()                       // convierte List en Stream
                .map(this::mapToResponse)       // aplica mapToResponse a cada elemento
                .collect(Collectors.toList());  // vuelve a convertir en List
    }

    // ── RESUMEN FINANCIERO COMPLETO ───────────────────────────────────
    // Endpoint enriquecido: genera análisis + consulta budget-service + métricas históricas
    // Es el endpoint más completo del microservicio (IE 2.4.1 y IE 2.4.2)
    public ResumenFinancieroResponse obtenerResumenFinanciero(Long userId) {
        log.info("Generando resumen financiero completo para userId: {}", userId);

        // Paso 1: Obtener ingresos y gastos actuales desde servicios remotos
        Double totalIngresos = incomeServiceClient.obtenerTotalIngresosPorUsuario(userId);
        Double totalGastos = expenseServiceClient.obtenerTotalGastadoPorUsuario(userId);
        log.info("Resumen — Ingresos: {}, Gastos: {} para userId: {}", totalIngresos, totalGastos, userId);

        // Paso 2: Calcular métricas del análisis actual
        Double balance = totalIngresos - totalGastos;
        Double tasaAhorro = totalIngresos > 0
                ? ((totalIngresos - totalGastos) / totalIngresos) * 100
                : 0.0;
        String estadoBalance = calcularEstadoBalance(balance);

        // Paso 3: Obtener presupuestos activos desde budget-service
        List<BudgetRemoteResponse> presupuestos;
        try {
            presupuestos = budgetServiceClient.obtenerPresupuestosPorUsuario(userId)
                    .stream()
                    // Filtrar solo los presupuestos activos para el resumen
                    .filter(b -> Boolean.TRUE.equals(b.getActivo()))
                    .collect(Collectors.toList());
            log.info("Se encontraron {} presupuestos activos para userId: {}", presupuestos.size(), userId);
        } catch (RuntimeException e) {
            // Si budget-service falla, continuar sin presupuestos (no interrumpir el resumen)
            log.warn("No se pudieron obtener presupuestos para userId {}: {}", userId, e.getMessage());
            presupuestos = List.of();
        }

        // Paso 4: Obtener métricas históricas desde la BD local
        Double promedioTasaAhorro = analyticsRepository.promedioTasaAhorroPorUsuario(userId);
        // Si no hay snapshots previos, el promedio es null → usar 0.0
        if (promedioTasaAhorro == null) promedioTasaAhorro = 0.0;

        Long balancesNegativos = analyticsRepository.contarBalancesNegativos(userId);
        log.info("Métricas históricas userId {}: promTasa={}%, balancesNegativos={}",
                userId, promedioTasaAhorro, balancesNegativos);

        // Paso 5: Generar recomendación interpretativa
        String recomendacion = generarRecomendacion(estadoBalance, tasaAhorro);

        // Paso 6: Construir y retornar el resumen completo
        ResumenFinancieroResponse resumen = new ResumenFinancieroResponse();
        resumen.setUserId(userId);
        resumen.setTotalIngresos(totalIngresos);
        resumen.setTotalGastos(totalGastos);
        resumen.setBalance(balance);
        resumen.setTasaAhorro(tasaAhorro);
        resumen.setEstadoBalance(estadoBalance);
        resumen.setPromedioTasaAhorroHistorico(promedioTasaAhorro);
        resumen.setCantidadBalancesNegativos(balancesNegativos);
        resumen.setPresupuestosActivos(presupuestos);
        resumen.setFechaGeneracion(LocalDate.now());
        resumen.setRecomendacion(recomendacion);

        log.info("Resumen financiero completo generado para userId: {}", userId);
        return resumen;
    }

    // ── ELIMINAR ──────────────────────────────────────────────────────
    // Elimina un snapshot de análisis — útil para mantenimiento o correcciones
    public void eliminar(Long id) {
        log.info("Eliminando análisis id: {}", id);

        if (!analyticsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Análisis con id " + id + " no existe");
        }

        analyticsRepository.deleteById(id);
        log.info("Análisis id: {} eliminado exitosamente", id);
    }
}
