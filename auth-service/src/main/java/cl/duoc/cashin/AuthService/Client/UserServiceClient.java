package cl.duoc.cashin.AuthService.Client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import cl.duoc.cashin.AuthService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.AuthService.dto.Response.UserRemoteResponse;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
// Esta clase es un bean

@RequiredArgsConstructor

public class UserServiceClient {

        private final WebClient webClient;
        // Se inyecta el WebClient configurado en WebClientConfig

        public UserRemoteResponse obtenerUsuarioPorEmail(String email) {
                return webClient.get()

                                // devuelve passwordHash para verificar credenciales
                                .uri("/api/v1/users/internal/email/{email}", email)
                                .retrieve()
                                .onStatus(
                                                // Si retorna 404, el usuario no existe
                                                status -> status.value() == 404,
                                                response -> Mono.error(
                                                                new ResourceNotFoundException(
                                                                                "Usuario con email " + email
                                                                                                + " no existe en el sistema")))
                                .onStatus(
                                                // Si retorna cualquier otro error (500 o similar)
                                                status -> status.is5xxServerError(),
                                                response -> Mono.error(
                                                                new RuntimeException(
                                                                                "Error al comunicarse con user-service")))
                                .bodyToMono(UserRemoteResponse.class)
                                .block();

        }
}
