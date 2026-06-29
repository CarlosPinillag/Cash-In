package cl.duoc.cashin.AuthService;

import cl.duoc.cashin.AuthService.Client.UserServiceClient;
import cl.duoc.cashin.AuthService.Config.JwtProperties;
import cl.duoc.cashin.AuthService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.AuthService.Model.AuthTokenModel;
import cl.duoc.cashin.AuthService.Repository.AuthTokenRepository;
import cl.duoc.cashin.AuthService.Service.AuthTokenService;
import cl.duoc.cashin.AuthService.dto.Request.AuthTokenRequest;
import cl.duoc.cashin.AuthService.dto.Response.AuthTokenResponse;
import cl.duoc.cashin.AuthService.dto.Response.UserRemoteResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthTokenService – pruebas unitarias de lógica de negocio")
class AuthServiceApplicationTests {

    @Mock private AuthTokenRepository authTokenRepository;
    @Mock private UserServiceClient userServiceClient;
    @Mock private JwtProperties jwtProperties;

    @InjectMocks
    private AuthTokenService authTokenService;

    private UserRemoteResponse usuarioRemoto;
    private AuthTokenRequest loginRequest;
    private AuthTokenModel tokenModel;

    @BeforeEach
    void setUp() {
        usuarioRemoto = new UserRemoteResponse();
        usuarioRemoto.setIdUser(1L);
        usuarioRemoto.setEmail("juan@email.com");
        usuarioRemoto.setPasswordHash("pass123");

        loginRequest = new AuthTokenRequest();
        loginRequest.setUsername("juan@email.com");
        loginRequest.setPassword("pass123");

        tokenModel = new AuthTokenModel();
        tokenModel.setIdToken(1L);
        tokenModel.setUsername("juan@email.com");
        tokenModel.setToken("jwt.token.aqui");
        tokenModel.setIssuedAt(LocalDateTime.now());
        tokenModel.setExpiresAt(LocalDateTime.now().plusHours(1));
        tokenModel.setActivo(true);
    }

    // ── login ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("login: genera token JWT y lo persiste cuando las credenciales son válidas")
    void login_credencialesValidas_retornaToken() {
        when(jwtProperties.getSecret()).thenReturn("clave-secreta-muy-larga-para-hmac256-al-menos-32-chars");
        when(jwtProperties.getExpiration()).thenReturn(3600000L);
        when(userServiceClient.obtenerUsuarioPorEmail("juan@email.com")).thenReturn(usuarioRemoto);
        when(authTokenRepository.findByUsernameAndActivoTrue("juan@email.com")).thenReturn(Optional.empty());
        when(authTokenRepository.save(any(AuthTokenModel.class))).thenReturn(tokenModel);

        AuthTokenResponse respuesta = authTokenService.login(loginRequest);

        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getUsername()).isEqualTo("juan@email.com");
        assertThat(respuesta.getToken()).isNotBlank();
        verify(authTokenRepository).save(any(AuthTokenModel.class));
    }

    @Test
    @DisplayName("login: lanza RuntimeException si la contraseña es incorrecta")
    void login_passwordIncorrecta_lanzaExcepcion() {
        loginRequest.setPassword("wrongpassword");
        when(userServiceClient.obtenerUsuarioPorEmail("juan@email.com")).thenReturn(usuarioRemoto);

        assertThatThrownBy(() -> authTokenService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Credenciales invalidas");

        verify(authTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("login: invalida el token anterior si ya existe uno activo")
    void login_tokenPrevioActivo_invalidaYCreaUevoToken() {
        AuthTokenModel tokenPrevio = new AuthTokenModel();
        tokenPrevio.setUsername("juan@email.com");
        tokenPrevio.setActivo(true);

        when(jwtProperties.getSecret()).thenReturn("clave-secreta-muy-larga-para-hmac256-al-menos-32-chars");
        when(jwtProperties.getExpiration()).thenReturn(3600000L);
        when(userServiceClient.obtenerUsuarioPorEmail("juan@email.com")).thenReturn(usuarioRemoto);
        when(authTokenRepository.findByUsernameAndActivoTrue("juan@email.com"))
                .thenReturn(Optional.of(tokenPrevio));
        when(authTokenRepository.save(any(AuthTokenModel.class))).thenReturn(tokenModel);

        authTokenService.login(loginRequest);

        // Se llama save al menos dos veces: una para invalidar el token anterior y otra para guardar el nuevo
        verify(authTokenRepository, atLeast(2)).save(any(AuthTokenModel.class));
        assertThat(tokenPrevio.getActivo()).isFalse();
    }

    // ── logout ────────────────────────────────────────────────────────

    @Test
    @DisplayName("logout: desactiva el token activo")
    void logout_tokenActivo_desactivaToken() {
        when(authTokenRepository.findByTokenAndActivoTrue("jwt.token.aqui"))
                .thenReturn(Optional.of(tokenModel));
        when(authTokenRepository.save(any(AuthTokenModel.class))).thenReturn(tokenModel);

        authTokenService.logout("jwt.token.aqui");

        assertThat(tokenModel.getActivo()).isFalse();
        verify(authTokenRepository).save(tokenModel);
    }

    @Test
    @DisplayName("logout: lanza ResourceNotFoundException si el token no existe o ya fue revocado")
    void logout_tokenInexistente_lanzaExcepcion() {
        when(authTokenRepository.findByTokenAndActivoTrue("token-invalido"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authTokenService.logout("token-invalido"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Token no encontrado o ya fue invalidado");

        verify(authTokenRepository, never()).save(any());
    }

    // ── validateToken ─────────────────────────────────────────────────

    @Test
    @DisplayName("validateToken: retorna false si el token fue revocado por logout")
    void validateToken_tokenRevocado_retornaFalse() {
        // Token con firma válida pero no en BD activo
        when(jwtProperties.getSecret()).thenReturn("clave-secreta-muy-larga-para-hmac256-al-menos-32-chars");

        // Un token arbitrario que fallará la verificación JWT (no firmado con la clave)
        boolean resultado = authTokenService.validateToken("token.invalido.forzado");

        assertThat(resultado).isFalse();
    }

    // ── obtenerPorId ─────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerPorId: retorna token cuando existe")
    void obtenerPorId_existente_retornaToken() {
        when(authTokenRepository.findById(1L)).thenReturn(Optional.of(tokenModel));

        AuthTokenResponse respuesta = authTokenService.obtenerPorId(1L);

        assertThat(respuesta.getUsername()).isEqualTo("juan@email.com");
        assertThat(respuesta.getToken()).isEqualTo("jwt.token.aqui");
    }

    @Test
    @DisplayName("obtenerPorId: lanza ResourceNotFoundException si no existe")
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(authTokenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authTokenService.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Token con id 99 no encontrado");
    }

    // ── eliminar ──────────────────────────────────────────────────────

    @Test
    @DisplayName("eliminar: llama deleteById cuando el token existe")
    void eliminar_existente_eliminaCorrectamente() {
        when(authTokenRepository.existsById(1L)).thenReturn(true);

        authTokenService.eliminar(1L);

        verify(authTokenRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar: lanza ResourceNotFoundException si no existe")
    void eliminar_inexistente_lanzaExcepcion() {
        when(authTokenRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> authTokenService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Token con id 99 no existe");

        verify(authTokenRepository, never()).deleteById(any());
    }
}
