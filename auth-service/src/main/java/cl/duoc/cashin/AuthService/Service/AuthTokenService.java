package cl.duoc.cashin.AuthService.Service;

import java.time.LocalDateTime;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import cl.duoc.cashin.AuthService.Client.UserServiceClient;
import cl.duoc.cashin.AuthService.Config.JwtProperties;
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
    private final JwtProperties jwtProperties;

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(jwtProperties.getSecret());
    }

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
            throw new RuntimeException("Credenciales invalidas - password incorrecta");
        }

        authTokenRepository.findByUsernameAndActivoTrue(request.getUsername())
                .ifPresent(tokenExistente -> {
                    log.info("Invalidando token anterior para username: {}", request.getUsername());
                    tokenExistente.setActivo(false);
                    authTokenRepository.save(tokenExistente);
                });

        // Generar JWT con com.auth0
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + jwtProperties.getExpiration());

        String nuevoToken = JWT.create()
                .withSubject(request.getUsername())
                .withIssuer("auth-service")
                .withIssuedAt(issuedAt)
                .withExpiresAt(expiresAt)
                .sign(getAlgorithm());

        // Guardar en BD para soporte de logout
        AuthTokenModel modelo = new AuthTokenModel();
        modelo.setUsername(request.getUsername());
        modelo.setToken(nuevoToken);
        modelo.setIssuedAt(LocalDateTime.now());
        modelo.setExpiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getExpiration() / 1000));
        modelo.setActivo(true);

        AuthTokenModel guardado = authTokenRepository.save(modelo);
        log.info("Token JWT generado para username: {} | expira: {}",
                guardado.getUsername(), guardado.getExpiresAt());

        return mapToResponse(guardado);
    }

    // LOGOUT
    public void logout(String token) {
        log.info("Intentando logout");

        AuthTokenModel modelo = authTokenRepository.findByTokenAndActivoTrue(token)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Token no encontrado o ya fue invalidado"));

        modelo.setActivo(false);
        authTokenRepository.save(modelo);
        log.info("Logout exitoso para username: {}", modelo.getUsername());
    }

    // VALIDAR — verifica firma JWT + que no esté revocado en BD
    public boolean validateToken(String token) {
        log.info("Validando token JWT");

        // Verificar firma y expiración
        try {
            JWT.require(getAlgorithm())
                    .withIssuer("auth-service")
                    .build()
                    .verify(token);
        } catch (JWTVerificationException e) {
            log.warn("Token JWT invalido o expirado: {}", e.getMessage());
            return false;
        }

        // Verificar que no haya sido revocado por logout
        boolean activoEnBD = authTokenRepository
                .findByTokenAndActivoTrue(token)
                .isPresent();

        if (!activoEnBD) {
            log.warn("Token valido pero revocado por logout");
        }

        return activoEnBD;
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
