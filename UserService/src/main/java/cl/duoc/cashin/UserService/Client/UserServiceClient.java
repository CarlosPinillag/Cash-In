package cl.duoc.cashin.UserService.Client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import cl.duoc.cashin.UserService.dto.Response.UserResponse;
import cl.duoc.cashin.UserService.Exception.ResourceNotFoundException;
//                                         ^ 'Exception' con E mayúscula
//                                           debe coincidir con el nombre real de la carpeta
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final WebClient webcliente;

    public UserResponse obtenerUserPorId(Long id) {
        return webcliente.get()
                .uri("/api/v1/users/{id}", id)
                // ^ CORREGIDO: era /api/v1/fiestas/{id}
                // debe apuntar al endpoint real del servicio
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        response -> Mono.error(
                                new ResourceNotFoundException("Usuario con id " + id + " no existe")))
                .bodyToMono(UserResponse.class)
                .block();
    }
}
