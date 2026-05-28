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

@RequiredArgsConstructor

public class AuthTokenService {

    private static final Logger log = LoggerFactory.getLogger(AuthTokenService.class);

    private final AuthTokenRepository authTokenRepository;
    private final UserServiceClient userServiceClient;

    private AuthTokenResponse mapToResponse(AuthTokenModel model) {
        AuthTokenResponse response = new AuthTokenResponse();
        response.setToken(model.getToken());
        response.setUsername(model.getUsername());
        response.setExpiresAt(model.getExpiresAt());
        return response;
    }

    // LOGIN
    public AuthTokenResponse login(AuthTokenRequest request) {
        log.info("Intentando login para username: {}", request.getUsername());

        UserRemoteResponse usuario = userServiceClient.obtenerUsuarioPorEmail(request.getUsername());
        log.info("Usuario encontrado en user-service: {}", usuario.getEmail());

        if (!request.getPassword().equals(usuario.getPasswordHash())) {
            log.warn("Password incorrecta para username: {}", request.getUsername());
            throw new RuntimeException("Credenciales invalidas — password incorrecta");

        }

        authTokenRepository.findByUsernameAndActivoTrue(request.getUsername())
                .ifPresent(tokenExistente -> {
                    log.info("Invalidando token anterior para username: {}", request.getUsername());
                    tokenExistente.setActivo(false);
                    authTokenRepository.save(tokenExistente);
                });

        String nuevoToken = UUID.randomUUID().toString();

        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiresAt = issuedAt.plusHours(24);

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

    // LOGOUT
    public void logout(String token) {
        log.info("Intentando logout para token: {}", token);

        AuthTokenModel modelo = authTokenRepository.findByTokenAndActivoTrue(token)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Token no encontrado o ya fue invalidado"));

        modelo.setActivo(false);
        authTokenRepository.save(modelo);
        log.info("Logout exitoso para username: {}", modelo.getUsername());
    }

    public boolean validateToken(String token) {
        log.info("Validando token: {}", token);

        return authTokenRepository.findByTokenAndActivoTrue(token)
                .map(modelo -> {

                    boolean vigente = LocalDateTime.now().isBefore(modelo.getExpiresAt());

                    if (!vigente) {

                        log.warn("Token vencido para username: {} | vencio: {}",
                                modelo.getUsername(), modelo.getExpiresAt());
                        modelo.setActivo(false);
                        authTokenRepository.save(modelo);
                    } else {
                        log.info("Token valido para username: {}", modelo.getUsername());
                    }

                    return vigente;
                })

                .orElse(false);
    }

    // OBTENER POR ID
    public AuthTokenResponse obtenerPorId(Long id) {
        log.info("Buscando token con id: {}", id);

        AuthTokenModel modelo = authTokenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Token con id " + id + " no encontrado"));

        return mapToResponse(modelo);
    }

    // ELIMINAR
    public void eliminar(Long id) {
        log.info("Eliminando token con id: {}", id);

        if (!authTokenRepository.existsById(id)) {
            throw new ResourceNotFoundException("Token con id " + id + " no existe");
        }

        authTokenRepository.deleteById(id);
        log.info("Token id: {} eliminado exitosamente", id);
    }
}
