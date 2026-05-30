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

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final AnalyticsRepository analyticsRepository;
    private final ExpenseServiceClient expenseServiceClient;
    private final IncomeServiceClient incomeServiceClient;
    private final BudgetServiceClient budgetServiceClient;

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

    private String calcularEstadoBalance(Double balance) {
        if (balance > 0)
            return "POSITIVO";
        if (balance < 0)
            return "NEGATIVO";
        return "EQUILIBRADO";
    }

    private String generarRecomendacion(String estadoBalance, Double tasaAhorro) {
        if ("NEGATIVO".equals(estadoBalance)) {
            return "Tus gastos superan tus ingresos. Revisa tus presupuestos y reduce gastos no esenciales.";
        }
        if ("EQUILIBRADO".equals(estadoBalance)) {
            return "Tus ingresos y gastos están equilibrados. Considera crear un fondo de ahorro.";
        }
        if (tasaAhorro >= 20) {
            return "Excelente gestión financiera. Tu tasa de ahorro supera el 20%.";
        }
        if (tasaAhorro >= 10) {
            return "Buena gestión financiera. Intenta aumentar tu tasa de ahorro por encima del 20%.";
        }
        return "Tienes balance positivo pero tu tasa de ahorro es baja. Considera reducir gastos variables.";
    }

    // ── GENERAR ANÁLISIS
    public AnalyticsResponse generarAnalisis(AnalyticsRequest request, String authHeader) {
        log.info("Generando análisis financiero para userId: {}", request.getUserId());

        Double totalIngresos = incomeServiceClient.obtenerTotalIngresosPorUsuario(request.getUserId(), authHeader);
        log.info("Total ingresos para userId {}: {}", request.getUserId(), totalIngresos);

        Double totalGastos = expenseServiceClient.obtenerTotalGastadoPorUsuario(request.getUserId(), authHeader);
        log.info("Total gastos para userId {}: {}", request.getUserId(), totalGastos);

        Double balance = totalIngresos - totalGastos;
        Double tasaAhorro = totalIngresos > 0
                ? ((totalIngresos - totalGastos) / totalIngresos) * 100
                : 0.0;

        String estadoBalance = calcularEstadoBalance(balance);
        log.info("Balance para userId {}: {} — Estado: {}", request.getUserId(), balance, estadoBalance);

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

    // ── OBTENER POR ID
    public AnalyticsResponse obtenerPorId(Long id) {
        log.info("Buscando análisis id: {}", id);

        AnalyticsModel model = analyticsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Análisis con id " + id + " no encontrado"));

        return mapToResponse(model);
    }

    // ── HISTORIAL POR USUARIO
    public List<AnalyticsResponse> obtenerHistorialPorUsuario(Long userId) {
        log.info("Obteniendo historial de análisis para userId: {}", userId);

        return analyticsRepository.findByUserIdOrderByFechaGeneracionDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── RESUMEN FINANCIERO COMPLETO
    public ResumenFinancieroResponse obtenerResumenFinanciero(Long userId, String authHeader) {
        log.info("Generando resumen financiero completo para userId: {}", userId);

        Double totalIngresos = incomeServiceClient.obtenerTotalIngresosPorUsuario(userId, authHeader);
        Double totalGastos = expenseServiceClient.obtenerTotalGastadoPorUsuario(userId, authHeader);
        log.info("Resumen — Ingresos: {}, Gastos: {} para userId: {}", totalIngresos, totalGastos, userId);

        Double balance = totalIngresos - totalGastos;
        Double tasaAhorro = totalIngresos > 0
                ? ((totalIngresos - totalGastos) / totalIngresos) * 100
                : 0.0;
        String estadoBalance = calcularEstadoBalance(balance);

        List<BudgetRemoteResponse> presupuestos;
        try {
            presupuestos = budgetServiceClient.obtenerPresupuestosPorUsuario(userId, authHeader)
                    .stream()
                    .filter(b -> Boolean.TRUE.equals(b.getActivo()))
                    .collect(Collectors.toList());
            log.info("Se encontraron {} presupuestos activos para userId: {}", presupuestos.size(), userId);
        } catch (RuntimeException e) {
            log.warn("No se pudieron obtener presupuestos para userId {}: {}", userId, e.getMessage());
            presupuestos = List.of();
        }

        Double promedioTasaAhorro = analyticsRepository.promedioTasaAhorroPorUsuario(userId);
        if (promedioTasaAhorro == null)
            promedioTasaAhorro = 0.0;

        Long balancesNegativos = analyticsRepository.contarBalancesNegativos(userId);
        log.info("Métricas históricas userId {}: promTasa={}%, balancesNegativos={}",
                userId, promedioTasaAhorro, balancesNegativos);

        String recomendacion = generarRecomendacion(estadoBalance, tasaAhorro);

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

    // ── ELIMINAR
    public void eliminar(Long id) {
        log.info("Eliminando análisis id: {}", id);

        if (!analyticsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Análisis con id " + id + " no existe");
        }

        analyticsRepository.deleteById(id);
        log.info("Análisis id: {} eliminado exitosamente", id);
    }
}