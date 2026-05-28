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

@Service
@RequiredArgsConstructor

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    private UserResponse mapToResponse(UserModel model) {
        UserResponse response = new UserResponse();
        response.setIdUser(model.getIdUser());
        response.setNombre(model.getNombre());
        response.setEmail(model.getEmail());
        response.setTelefono(model.getTelefono());
        response.setFechaRegistro(model.getFechaRegistro());
        response.setActivo(model.getActivo());
        response.setPresupuestoMensual(model.getPresupuestoMensual());

        return response;
    }

    // CREAR
    public UserResponse crearUsuario(UserCreateRequest request) {
        log.info("Creando usuario con email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException(
                    "Ya existe un usuario con el email: " + request.getEmail());

        }

        UserModel model = new UserModel();
        model.setNombre(request.getNombre());
        model.setEmail(request.getEmail());
        model.setPasswordHash(request.getPassword());

        model.setTelefono(request.getTelefono());
        model.setFechaRegistro(LocalDate.now());
        model.setActivo(true);
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

    // OBTENER POR ID
    public UserResponse obtenerPorId(Long id) {
        log.info("Buscando usuario id: {}", id);

        UserModel model = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario con id " + id + " no encontrado"));

        return mapToResponse(model);
    }

    public List<UserResponse> listarTodos() {
        log.info("Listando todos los usuarios activos");

        return userRepository.findByActivoTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse actualizar(Long id, UserUpdateRequest request) {

        log.info("Actualizando usuario id: {}", id);

        UserModel model = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario con id " + id + " no encontrado"));

        if (request.getEmail() != null) {

            if (!model.getEmail().equals(request.getEmail()) &&
                    userRepository.existsByEmail(request.getEmail())) {

                throw new RuntimeException(
                        "El email " + request.getEmail() + " ya está en uso");
            }

            model.setEmail(request.getEmail());
        }

        // ACTUALIZAR SOLO CAMPOS NO NULL

        if (request.getNombre() != null) {
            model.setNombre(request.getNombre());
        }

        if (request.getPassword() != null) {
            model.setPasswordHash(request.getPassword());

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
