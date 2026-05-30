package cl.duoc.cashin.AnalyticsService.Client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import cl.duoc.cashin.AnalyticsService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.AnalyticsService.dto.Response.UserRemoteResponse;
import reactor.core.publisher.Mono;

@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(@Qualifier("userWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public UserRemoteResponse obtenerUsuarioPorId(Long userId, String authHeader) {
        return webClient.get()
                .uri("/api/v1/users/{id}", userId)
                .header("Authorization", authHeader)
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        response -> Mono.error(
                                new ResourceNotFoundException(
                                        "Usuario con id " + userId + " no existe en el sistema")))
                .onStatus(
                        status -> status.is5xxServerError(),
                        response -> Mono.error(
                                new RuntimeException(
                                        "Error al comunicarse con user-service")))
                .bodyToMono(UserRemoteResponse.class)
                .block();
    }
}