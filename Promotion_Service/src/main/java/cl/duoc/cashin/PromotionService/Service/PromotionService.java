package cl.duoc.cashin.PromotionService.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cl.duoc.cashin.PromotionService.Client.CategoryServiceClient;
import cl.duoc.cashin.PromotionService.Client.UserServiceClient;
import cl.duoc.cashin.PromotionService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.PromotionService.Model.PromotionModel;
import cl.duoc.cashin.PromotionService.Repository.PromotionRepository;
import cl.duoc.cashin.PromotionService.dto.Request.PromotionAplicarRequest;
import cl.duoc.cashin.PromotionService.dto.Request.PromotionRequest;
import cl.duoc.cashin.PromotionService.dto.Request.PromotionUpdateRequest;
import cl.duoc.cashin.PromotionService.dto.Response.CategoryRemoteResponse;
import cl.duoc.cashin.PromotionService.dto.Response.PromotionAplicarResponse;
import cl.duoc.cashin.PromotionService.dto.Response.PromotionResponse;
import cl.duoc.cashin.PromotionService.dto.Response.UserRemoteResponse;
import lombok.RequiredArgsConstructor;

@Service

@RequiredArgsConstructor

public class PromotionService {

    
    private static final Logger log = LoggerFactory.getLogger(PromotionService.class);

    private final PromotionRepository promotionRepository;
    private final CategoryServiceClient categoryServiceClient;
    private final UserServiceClient userServiceClient;

    
    
    
    private PromotionResponse mapToResponse(PromotionModel model) {
        PromotionResponse response = new PromotionResponse();
        response.setIdPromotion(model.getIdPromotion());
        response.setCategoryId(model.getCategoryId());
        response.setNombreCategoria(model.getNombreCategoria());
        response.setCodigo(model.getCodigo());
        response.setDescripcion(model.getDescripcion());
        response.setTipoDescuento(model.getTipoDescuento());
        response.setValorDescuento(model.getValorDescuento());
        response.setFechaInicio(model.getFechaInicio());
        response.setFechaFin(model.getFechaFin());
        response.setUsoMaximo(model.getUsoMaximo());
        response.setUsosActuales(model.getUsosActuales());
        response.setActivo(model.getActivo());
        return response;
    }

    
    public PromotionResponse crear(PromotionRequest request, String authHeader) {
        log.info("Creando promocion con codigo: {}", request.getCodigo());

        
        if (promotionRepository.existsByCodigo(request.getCodigo())) {
            log.warn("Intento de crear promocion con codigo duplicado: {}", request.getCodigo());
            throw new RuntimeException(
                    "Ya existe una promocion con el codigo: " + request.getCodigo());
        }

        
        if (request.getFechaInicio().isAfter(request.getFechaFin())) {
            log.warn("Fecha de inicio {} es posterior a fecha de fin {}", request.getFechaInicio(), request.getFechaFin());
            throw new RuntimeException(
                    "La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        
        if ("PORCENTAJE".equals(request.getTipoDescuento()) && request.getValorDescuento() > 100.0) {
            log.warn("Porcentaje de descuento invalido: {}", request.getValorDescuento());
            throw new RuntimeException(
                    "El porcentaje de descuento no puede superar el 100%");
        }

        
        String nombreCategoria = null;

        
        if (request.getCategoryId() != null) {
            CategoryRemoteResponse categoria = categoryServiceClient.obtenerCategoriaPorId(request.getCategoryId(), authHeader);
            log.info("Categoria id: {} ({}) validada en category-service", request.getCategoryId(), categoria.getNombre());

            
            if (!categoria.getActivo()) {
                throw new RuntimeException(
                        "La categoria con id " + request.getCategoryId() + " no esta activa");
            }
            
            nombreCategoria = categoria.getNombre();
        }

        
        PromotionModel model = new PromotionModel();
        model.setCategoryId(request.getCategoryId());
        model.setNombreCategoria(nombreCategoria);
        model.setCodigo(request.getCodigo());
        model.setDescripcion(request.getDescripcion());
        model.setTipoDescuento(request.getTipoDescuento());
        model.setValorDescuento(request.getValorDescuento());
        model.setFechaInicio(request.getFechaInicio());
        model.setFechaFin(request.getFechaFin());
        model.setUsoMaximo(request.getUsoMaximo());
        model.setUsosActuales(0); 
        model.setActivo(true);    

        PromotionModel guardada = promotionRepository.save(model);
        log.info("Promocion creada con id: {} — codigo: {} — tipo: {}",
                guardada.getIdPromotion(), guardada.getCodigo(), guardada.getTipoDescuento());

        return mapToResponse(guardada);
    }

    
    public PromotionResponse obtenerPorId(Long id) {
        log.info("Buscando promocion id: {}", id);

        PromotionModel model = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Promocion con id " + id + " no encontrada"));

        return mapToResponse(model);
    }

    
    public PromotionResponse obtenerPorCodigo(String codigo) {
        log.info("Buscando promocion con codigo: {}", codigo);

        PromotionModel model = promotionRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Promocion con codigo '" + codigo + "' no encontrada"));

        return mapToResponse(model);
    }

    
    public List<PromotionResponse> listarTodas() {
        log.info("Listando todas las promociones");

        return promotionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    
    
    public List<PromotionResponse> listarActivas() {
        log.info("Listando promociones activas");

        return promotionRepository.findByActivoTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    
    
    public List<PromotionResponse> listarActivasPorCategoria(Long categoryId) {
        log.info("Listando promociones activas para categoryId: {}", categoryId);

        return promotionRepository.findByActivoTrueAndCategoryId(categoryId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    
    
    public PromotionAplicarResponse aplicar(PromotionAplicarRequest request, String authHeader) {
        log.info("Aplicando promocion codigo: {} para userId: {} sobre monto: {}",
                request.getCodigo(), request.getUserId(), request.getMontoOriginal());

        
        UserRemoteResponse usuario = userServiceClient.obtenerUsuarioPorId(request.getUserId(), authHeader);
        log.info("Usuario id: {} validado en user-service", request.getUserId());

        if (!usuario.getActivo()) {
            throw new RuntimeException(
                    "El usuario con id " + request.getUserId() + " no esta activo. No puede canjear promociones.");
        }

        
        PromotionModel promo = promotionRepository
                .findActivaVigentePorCodigo(request.getCodigo(), LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La promocion con codigo '" + request.getCodigo() + "' no existe, esta inactiva o ha expirado"));

        log.info("Promocion encontrada: id={} codigo={} tipo={} valor={}",
                promo.getIdPromotion(), promo.getCodigo(), promo.getTipoDescuento(), promo.getValorDescuento());

        
        if (promo.getUsosActuales() >= promo.getUsoMaximo()) {
            log.warn("Promocion codigo: {} agotada — usos: {}/{}", promo.getCodigo(), promo.getUsosActuales(), promo.getUsoMaximo());
            throw new RuntimeException(
                    "La promocion '" + request.getCodigo() + "' ha agotado todos sus usos disponibles (" + promo.getUsoMaximo() + ")");
        }

        
        double descuentoAplicado;
        String mensajeDescuento;

        if ("PORCENTAJE".equals(promo.getTipoDescuento())) {
            
            descuentoAplicado = request.getMontoOriginal() * (promo.getValorDescuento() / 100.0);
            mensajeDescuento = String.format("Descuento del %.0f%% aplicado sobre $%.2f",
                    promo.getValorDescuento(), request.getMontoOriginal());
        } else {
            
            descuentoAplicado = Math.min(promo.getValorDescuento(), request.getMontoOriginal());
            mensajeDescuento = String.format("Descuento fijo de $%.2f aplicado sobre $%.2f",
                    promo.getValorDescuento(), request.getMontoOriginal());
        }

        
        double montoFinal = Math.max(0.0, request.getMontoOriginal() - descuentoAplicado);

        
        promo.setUsosActuales(promo.getUsosActuales() + 1);

        
        if (promo.getUsosActuales() >= promo.getUsoMaximo()) {
            promo.setActivo(false);
            log.info("Promocion codigo: {} desactivada automaticamente por alcanzar usoMaximo: {}",
                    promo.getCodigo(), promo.getUsoMaximo());
        }

        promotionRepository.save(promo);
        log.info("Promocion codigo: {} aplicada exitosamente — descuento: {} — montoFinal: {}",
                promo.getCodigo(), descuentoAplicado, montoFinal);

        
        PromotionAplicarResponse response = new PromotionAplicarResponse();
        response.setCodigo(promo.getCodigo());
        response.setMontoOriginal(request.getMontoOriginal());
        response.setDescuentoAplicado(descuentoAplicado);
        response.setMontoFinal(montoFinal);
        response.setTipoDescuento(promo.getTipoDescuento());
        response.setMensaje(mensajeDescuento);
        response.setUsosRestantes(promo.getUsoMaximo() - promo.getUsosActuales());

        return response;
    }

    
    
    public PromotionResponse actualizar(Long id, PromotionUpdateRequest request) {
        log.info("Actualizando promocion id: {}", id);

        PromotionModel model = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Promocion con id " + id + " no encontrada"));

        
        if (request.getDescripcion() != null) {
            model.setDescripcion(request.getDescripcion());
        }
        if (request.getValorDescuento() != null) {
            
            if ("PORCENTAJE".equals(model.getTipoDescuento()) && request.getValorDescuento() > 100.0) {
                throw new RuntimeException("El porcentaje de descuento no puede superar el 100%");
            }
            model.setValorDescuento(request.getValorDescuento());
        }
        if (request.getFechaFin() != null) {
            model.setFechaFin(request.getFechaFin());
        }
        if (request.getUsoMaximo() != null) {
            
            if (request.getUsoMaximo() < model.getUsosActuales()) {
                throw new RuntimeException(
                        "El nuevo uso maximo (" + request.getUsoMaximo() +
                        ") no puede ser menor a los usos actuales (" + model.getUsosActuales() + ")");
            }
            model.setUsoMaximo(request.getUsoMaximo());
        }
        if (request.getActivo() != null) {
            model.setActivo(Boolean.parseBoolean(request.getActivo()));
        }

        PromotionModel actualizada = promotionRepository.save(model);
        log.info("Promocion id: {} actualizada exitosamente", id);

        return mapToResponse(actualizada);
    }

    
    
    public PromotionResponse desactivar(Long id) {
        log.info("Desactivando promocion id: {}", id);

        PromotionModel model = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Promocion con id " + id + " no encontrada"));

        
        if (!model.getActivo()) {
            throw new RuntimeException("La promocion con id " + id + " ya esta desactivada");
        }

        model.setActivo(false);
        PromotionModel actualizada = promotionRepository.save(model);
        log.info("Promocion id: {} desactivada exitosamente", id);

        return mapToResponse(actualizada);
    }

    
    
    public void eliminar(Long id) {
        log.info("Eliminando promocion id: {}", id);

        if (!promotionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Promocion con id " + id + " no existe");
        }

        promotionRepository.deleteById(id);
        log.info("Promocion id: {} eliminada exitosamente", id);
    }
}

