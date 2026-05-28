package cl.duoc.cashin.PromotionService.Client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import cl.duoc.cashin.PromotionService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.PromotionService.dto.Response.UserRemoteResponse;
import reactor.core.publisher.Mono;

@Component
public class UserServiceClient {

        private final WebClient webClient;

        public UserServiceClient(@Qualifier("userWebClient") WebClient webClient) {
                this.webClient = webClient;
        }

        // GET http://localhost:8080/api/v1/users/{id}
        public UserRemoteResponse obtenerUsuarioPorId(Long userId) {
                return webClient.get()
                                .uri("/api/v1/users/{id}", userId)
                                .retrieve()
                                .onStatus(
                                                // Si user-service retorna 404, el usuario no existe
                                                status -> status.value() == 404,
                                                response -> Mono.error(
                                                                new ResourceNotFoundException(
                                                                                "Usuario con id " + userId
                                                                                                + " no existe en el sistema")))
                                .onStatus(
                                                // Si user-service retorna cualquier error de servidor (500, etc.)
                                                status -> status.is5xxServerError(),
                                                response -> Mono.error(
                                                                new RuntimeException(
                                                                                "Error al comunicarse con user-service")))
                                .bodyToMono(UserRemoteResponse.class)
                                .block();
        }
}
