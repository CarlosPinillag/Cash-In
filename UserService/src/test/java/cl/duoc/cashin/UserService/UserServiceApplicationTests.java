package cl.duoc.cashin.UserService;

import cl.duoc.cashin.UserService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.UserService.Model.UserModel;
import cl.duoc.cashin.UserService.Repository.UserRepository;
import cl.duoc.cashin.UserService.Service.UserService;
import cl.duoc.cashin.UserService.dto.Request.UserCreateRequest;
import cl.duoc.cashin.UserService.dto.Request.UserUpdateRequest;
import cl.duoc.cashin.UserService.dto.Response.UserResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService – pruebas unitarias de lógica de negocio")
class UserServiceApplicationTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserModel userModel;
    private UserCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        userModel = new UserModel();
        userModel.setIdUser(1L);
        userModel.setNombre("Juan Pérez");
        userModel.setEmail("juan@email.com");
        userModel.setPasswordHash("hash123");
        userModel.setTelefono("+56912345678");
        userModel.setFechaRegistro(LocalDate.now());
        userModel.setActivo(true);
        userModel.setPresupuestoMensual(500000.0);

        createRequest = new UserCreateRequest();
        createRequest.setNombre("Juan Pérez");
        createRequest.setEmail("juan@email.com");
        createRequest.setPassword("hash123");
        createRequest.setTelefono("+56912345678");
        createRequest.setPresupuestoMensual(500000.0);
    }

    // ── crearUsuario ─────────────────────────────────────────────────

    @Test
    @DisplayName("crearUsuario: crea usuario con email único y activo=true")
    void crearUsuario_emailUnico_retornaUsuarioCreado() {
        when(userRepository.existsByEmail("juan@email.com")).thenReturn(false);
        when(userRepository.save(any(UserModel.class))).thenReturn(userModel);

        UserResponse respuesta = userService.crearUsuario(createRequest);

        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getEmail()).isEqualTo("juan@email.com");
        assertThat(respuesta.getActivo()).isTrue();
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    @DisplayName("crearUsuario: lanza RuntimeException si el email ya existe")
    void crearUsuario_emailDuplicado_lanzaExcepcion() {
        when(userRepository.existsByEmail("juan@email.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.crearUsuario(createRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya existe un usuario con el email");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearUsuario: el nuevo usuario siempre inicia con activo=true")
    void crearUsuario_estadoInicialCorrecto() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(UserModel.class))).thenAnswer(inv -> {
            UserModel m = inv.getArgument(0);
            assertThat(m.getActivo()).isTrue();
            return userModel;
        });

        userService.crearUsuario(createRequest);
    }

    // ── obtenerPorId ─────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerPorId: retorna usuario cuando existe")
    void obtenerPorId_existente_retornaUsuario() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userModel));

        UserResponse respuesta = userService.obtenerPorId(1L);

        assertThat(respuesta.getIdUser()).isEqualTo(1L);
        assertThat(respuesta.getNombre()).isEqualTo("Juan Pérez");
    }

    @Test
    @DisplayName("obtenerPorId: lanza ResourceNotFoundException si no existe")
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario con id 99 no encontrado");
    }

    // ── obtenerPorEmail ───────────────────────────────────────────────

    @Test
    @DisplayName("obtenerPorEmail: retorna usuario cuando el email existe")
    void obtenerPorEmail_existente_retornaUsuario() {
        when(userRepository.findByEmail("juan@email.com")).thenReturn(Optional.of(userModel));

        UserResponse respuesta = userService.obtenerPorEmail("juan@email.com");

        assertThat(respuesta.getEmail()).isEqualTo("juan@email.com");
    }

    @Test
    @DisplayName("obtenerPorEmail: lanza ResourceNotFoundException si el email no existe")
    void obtenerPorEmail_inexistente_lanzaExcepcion() {
        when(userRepository.findByEmail("noexiste@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.obtenerPorEmail("noexiste@email.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario con email noexiste@email.com no encontrado");
    }

    // ── listarTodos ───────────────────────────────────────────────────

    @Test
    @DisplayName("listarTodos: retorna solo usuarios activos")
    void listarTodos_conUsuariosActivos_retornaLista() {
        when(userRepository.findByActivoTrue()).thenReturn(List.of(userModel));

        List<UserResponse> resultado = userService.listarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getActivo()).isTrue();
    }

    @Test
    @DisplayName("listarTodos: retorna lista vacía si no hay usuarios activos")
    void listarTodos_sinUsuariosActivos_retornaListaVacia() {
        when(userRepository.findByActivoTrue()).thenReturn(Collections.emptyList());

        assertThat(userService.listarTodos()).isEmpty();
    }

    // ── actualizar ────────────────────────────────────────────────────

    @Test
    @DisplayName("actualizar: actualiza nombre y teléfono correctamente")
    void actualizar_camposValidos_actualizaUsuario() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setNombre("Carlos Pérez");
        updateRequest.setTelefono("+56998765432");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userModel));
        when(userRepository.save(any(UserModel.class))).thenReturn(userModel);

        UserResponse respuesta = userService.actualizar(1L, updateRequest);

        assertThat(respuesta).isNotNull();
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    @DisplayName("actualizar: lanza RuntimeException si el nuevo email ya está en uso")
    void actualizar_emailEnUso_lanzaExcepcion() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setEmail("otro@email.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userModel));
        when(userRepository.existsByEmail("otro@email.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.actualizar(1L, updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ya está en uso");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar: lanza ResourceNotFoundException si el usuario no existe")
    void actualizar_inexistente_lanzaExcepcion() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.actualizar(99L, new UserUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario con id 99 no encontrado");
    }

    // ── eliminar (soft delete) ────────────────────────────────────────

    @Test
    @DisplayName("eliminar: desactiva el usuario (soft delete) en lugar de borrar")
    void eliminar_existente_desactivaUsuario() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userModel));
        when(userRepository.save(any(UserModel.class))).thenAnswer(inv -> {
            UserModel m = inv.getArgument(0);
            assertThat(m.getActivo()).isFalse();
            return m;
        });

        userService.eliminar(1L);

        verify(userRepository).save(any(UserModel.class));
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("eliminar: lanza ResourceNotFoundException si el usuario no existe")
    void eliminar_inexistente_lanzaExcepcion() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario con id 99 no encontrado");
    }
}
