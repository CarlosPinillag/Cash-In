package cl.duoc.cashin.UserService.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cl.duoc.cashin.UserService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.UserService.Model.UserModel;
import cl.duoc.cashin.UserService.Repository.UserRepository;
import cl.duoc.cashin.UserService.dto.Request.UserCreateRequest;
import cl.duoc.cashin.UserService.dto.Request.UserUpdateRequest;
import cl.duoc.cashin.UserService.dto.Response.UserResponse;
import lombok.RequiredArgsConstructor;

@Service // registra esta clase como Bean de lógica de negocio
@RequiredArgsConstructor // Lombok genera constructor con los atributos 'final'

public class UserService {

    // Logger SLF4J — siempre usar esto, NUNCA System.out.println
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    // ── MAPPER privado: UserModel → UserResponse ────────────────────
    // Convierte la entidad JPA en el DTO que se devuelve al cliente
    // Es privado porque solo el service lo usa
    private UserResponse mapToResponse(UserModel model) {
        UserResponse response = new UserResponse();
        response.setIdUser(model.getIdUser());
        response.setNombre(model.getNombre());
        response.setEmail(model.getEmail());
        response.setTelefono(model.getTelefono());
        response.setFechaRegistro(model.getFechaRegistro());
        response.setActivo(model.getActivo());
        response.setPresupuestoMensual(model.getPresupuestoMensual());
        // passwordHash NO se mapea — nunca se devuelve al cliente
        return response;
    }

    // ── CREAR ───────────────────────────────────────────────────────
    public UserResponse crearUsuario(UserCreateRequest request) {
        log.info("Creando usuario con email: {}", request.getEmail());

        // Regla de negocio: el email debe ser único en el sistema
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException(
                    "Ya existe un usuario con el email: " + request.getEmail());
            // GlobalExceptionHandler captura RuntimeException → HTTP 409
        }

        UserModel model = new UserModel();
        model.setNombre(request.getNombre());
        model.setEmail(request.getEmail());
        model.setPasswordHash(request.getPassword());
        // En producción real usar BCryptPasswordEncoder:
        // model.setPasswordHash(encoder.encode(request.getPasswordHash()));
        model.setTelefono(request.getTelefono());
        model.setFechaRegistro(LocalDate.now()); // fecha actual automática
        model.setActivo(true); // siempre activo al crear
        model.setPresupuestoMensual(request.getPresupuestoMensual());

        UserModel guardado = userRepository.save(model);
        log.info("Usuario creado con id: {}", guardado.getIdUser());
        return mapToResponse(guardado);
    }

    // OBTENER POR EMAIL
    public UserResponse obtenerPorEmail(String email) {
        UserModel modelo = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario con email " + email + " no encontrado"));
        return mapToResponse(modelo);
    }

    // OBTENER HASH POR EMAIL
    public UserModel obtenerModeloPorEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario con email " + email + " no encontrado"));
    }

    // ── OBTENER POR ID ───────────────────────────────────────────────
    public UserResponse obtenerPorId(Long id) {
        log.info("Buscando usuario id: {}", id);

        UserModel model = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario con id " + id + " no encontrado"));
        // findById retorna Optional<UserModel>
        // orElseThrow: si el Optional está vacío, lanza la excepción
        // GlobalExceptionHandler la captura → HTTP 404

        return mapToResponse(model);
    }

    // ── LISTAR TODOS ACTIVOS ─────────────────────────────────────────
    public List<UserResponse> listarTodos() {
        log.info("Listando todos los usuarios activos");

        return userRepository.findByActivoTrue() // solo activos
                .stream() // convierte List en Stream
                .map(this::mapToResponse) // aplica mapToResponse a cada elemento
                .collect(Collectors.toList()); // vuelve a convertir en List
    }

    // ── ACTUALIZAR ───────────────────────────────────────────────────
    public UserResponse actualizar(Long id, UserUpdateRequest request) {

        log.info("Actualizando usuario id: {}", id);

        UserModel model = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario con id " + id + " no encontrado"));

        // ── VALIDAR EMAIL SOLO SI VIENE EN EL REQUEST ───────────────────
        if (request.getEmail() != null) {

            // verificar si el nuevo email ya pertenece a otro usuario
            if (!model.getEmail().equals(request.getEmail()) &&
                    userRepository.existsByEmail(request.getEmail())) {

                throw new RuntimeException(
                        "El email " + request.getEmail() + " ya está en uso");
            }

            model.setEmail(request.getEmail());
        }

        // ── ACTUALIZAR SOLO CAMPOS NO NULL ──────────────────────────────

        if (request.getNombre() != null) {
            model.setNombre(request.getNombre());
        }

        if (request.getPassword() != null) {
            model.setPasswordHash(request.getPassword());

            // en producción:
            // model.setPasswordHash(encoder.encode(request.getPassword()));
        }

        if (request.getTelefono() != null) {
            model.setTelefono(request.getTelefono());
        }

        if (request.getPresupuestoMensual() != null) {
            model.setPresupuestoMensual(request.getPresupuestoMensual());
        }

        UserModel actualizado = userRepository.save(model);

        log.info("Usuario id: {} actualizado exitosamente", id);

        return mapToResponse(actualizado);
    }

    // ── ELIMINAR (soft delete) ───────────────────────────────────────
    // No se borra el registro. Se pone activo=false.
    // Así se conserva el historial de gastos e ingresos del usuario.
    public void eliminar(Long id) {
        log.info("Desactivando usuario id: {}", id);

        UserModel model = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario con id " + id + " no encontrado"));

        model.setActivo(false); // soft delete
        userRepository.save(model);
        log.info("Usuario id: {} desactivado", id);
    }
}
