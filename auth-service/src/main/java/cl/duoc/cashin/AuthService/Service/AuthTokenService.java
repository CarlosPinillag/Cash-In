package cl.duoc.cashin.AuthService.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cl.duoc.cashin.AuthService.Client.UserServiceClient;
import cl.duoc.cashin.AuthService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.AuthService.Model.AuthTokenModel;
import cl.duoc.cashin.AuthService.Repository.AuthTokenRepository;
import cl.duoc.cashin.AuthService.dto.Request.AuthTokenRequest;
import cl.duoc.cashin.AuthService.dto.Response.AuthTokenResponse;
import cl.duoc.cashin.AuthService.dto.Response.UserRemoteResponse;
import lombok.RequiredArgsConstructor;

@Service
// Registra esta clase como Bean de logica de negocio
@RequiredArgsConstructor
// Lombok genera constructor con los atributos 'final'
public class AuthTokenService {

    // Logger SLF4J — siempre usar esto, NUNCA System.out.println
    private static final Logger log = LoggerFactory.getLogger(AuthTokenService.class);

    private final AuthTokenRepository authTokenRepository;
    private final UserServiceClient userServiceClient;

    // ── MAPPER privado: AuthTokenModel → AuthTokenResponse ───────────
    // Convierte la entidad JPA en el DTO que se devuelve al cliente
    // Es privado porque solo el service lo usa
    private AuthTokenResponse mapToResponse(AuthTokenModel model) {
        AuthTokenResponse response = new AuthTokenResponse();
        response.setToken(model.getToken());
        response.setUsername(model.getUsername());
        response.setExpiresAt(model.getExpiresAt());
        return response;
    }

    // ── LOGIN ─────────────────────────────────────────────────────────
    public AuthTokenResponse login(AuthTokenRequest request) {
        log.info("Intentando login para username: {}", request.getUsername());

        // Regla 1: Llamar a user-service para verificar que el usuario existe
        // Si no existe, UserServiceClient lanza ResourceNotFoundException → HTTP 404
        UserRemoteResponse usuario = userServiceClient.obtenerUsuarioPorEmail(request.getUsername());
        log.info("Usuario encontrado en user-service: {}", usuario.getEmail());

        // Regla 2: Comparar password enviada con passwordHash del user-service
        // Nota: en produccion real se usaria BCrypt.checkpw(password, hash)
        // Para este proyecto comparamos directamente el texto plano
        if (!request.getPassword().equals(usuario.getPasswordHash())) {
            log.warn("Password incorrecta para username: {}", request.getUsername());
            throw new RuntimeException("Credenciales invalidas — password incorrecta");
            // GlobalExceptionHandler captura RuntimeException → HTTP 409
        }

        // Regla 4 y 5: Si ya existe un token activo para este usuario, invalidarlo
        authTokenRepository.findByUsernameAndActivoTrue(request.getUsername())
                .ifPresent(tokenExistente -> {
                    log.info("Invalidando token anterior para username: {}", request.getUsername());
                    tokenExistente.setActivo(false);
                    authTokenRepository.save(tokenExistente);
                });

        // Regla 6: Generar nuevo token usando UUID
        String nuevoToken = UUID.randomUUID().toString();

        // Regla 7 y 8: Calcular issuedAt y expiresAt
        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiresAt = issuedAt.plusHours(24);

        // Regla 9: Construir y guardar el nuevo token con activo = true
        AuthTokenModel modelo = new AuthTokenModel();
        modelo.setUsername(request.getUsername());
        modelo.setToken(nuevoToken);
        modelo.setIssuedAt(issuedAt);
        modelo.setExpiresAt(expiresAt);
        modelo.setActivo(true);

        AuthTokenModel guardado = authTokenRepository.save(modelo);
        log.info("Token generado exitosamente para username: {} | expira: {}",
                guardado.getUsername(), guardado.getExpiresAt());

        return mapToResponse(guardado);
    }

    // ── LOGOUT ────────────────────────────────────────────────────────
    public void logout(String token) {
        log.info("Intentando logout para token: {}", token);

        // Buscar el token activo
        AuthTokenModel modelo = authTokenRepository.findByTokenAndActivoTrue(token)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Token no encontrado o ya fue invalidado"));

        // Invalidar el token
        modelo.setActivo(false);
        authTokenRepository.save(modelo);
        log.info("Logout exitoso para username: {}", modelo.getUsername());
    }

    // ── VALIDATE TOKEN ────────────────────────────────────────────────
    // Regla 10: validateToken verifica token existe + activo=true + no vencido
    public boolean validateToken(String token) {
        log.info("Validando token: {}", token);

        // Buscar el token en BD que sea activo
        return authTokenRepository.findByTokenAndActivoTrue(token)
                .map(modelo -> {
                    // Verificar que no haya vencido
                    boolean vigente = LocalDateTime.now().isBefore(modelo.getExpiresAt());

                    if (!vigente) {
                        // Si vencio, invalidarlo automaticamente
                        log.warn("Token vencido para username: {} | vencio: {}",
                                modelo.getUsername(), modelo.getExpiresAt());
                        modelo.setActivo(false);
                        authTokenRepository.save(modelo);
                    } else {
                        log.info("Token valido para username: {}", modelo.getUsername());
                    }

                    return vigente;
                })
                // Si no se encontro el token activo, retornar false
                .orElse(false);
    }

    // ── OBTENER POR ID ────────────────────────────────────────────────
    public AuthTokenResponse obtenerPorId(Long id) {
        log.info("Buscando token con id: {}", id);

        AuthTokenModel modelo = authTokenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Token con id " + id + " no encontrado"));

        return mapToResponse(modelo);
    }

    // ── ELIMINAR ──────────────────────────────────────────────────────
    public void eliminar(Long id) {
        log.info("Eliminando token con id: {}", id);

        if (!authTokenRepository.existsById(id)) {
            throw new ResourceNotFoundException("Token con id " + id + " no existe");
        }

        authTokenRepository.deleteById(id);
        log.info("Token id: {} eliminado exitosamente", id);
    }
}
