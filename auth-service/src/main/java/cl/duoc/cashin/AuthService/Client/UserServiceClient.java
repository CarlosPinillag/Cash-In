package cl.duoc.cashin.AuthService.Client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import cl.duoc.cashin.AuthService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.AuthService.dto.Response.UserRemoteResponse;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
// Le dice a Spring: "Esta clase es un bean, creala automaticamente y
// adminstrala"
@RequiredArgsConstructor
// Lombok genera automaticamente un constructor con los atributos final
public class UserServiceClient {

        private final WebClient webClient;
        // Spring inyecta el WebClient configurado en WebClientConfig

        public UserRemoteResponse obtenerUsuarioPorEmail(String email) {
                return webClient.get()
                                // Llama a: GET http://localhost:8080/api/v1/users/internal/email/{email}
                                // endpoint interno que devuelve passwordHash para verificar credenciales
                                .uri("/api/v1/users/internal/email/{email}", email)
                                .retrieve()
                                .onStatus(
                                                // Si user-service retorna 404, el usuario no existe
                                                status -> status.value() == 404,
                                                response -> Mono.error(
                                                                new ResourceNotFoundException(
                                                                                "Usuario con email " + email
                                                                                                + " no existe en el sistema")))
                                .onStatus(
                                                // Si user-service retorna cualquier otro error (500, etc.)
                                                status -> status.is5xxServerError(),
                                                response -> Mono.error(
                                                                new RuntimeException(
                                                                                "Error al comunicarse con user-service")))
                                .bodyToMono(UserRemoteResponse.class)
                                // Convierte el JSON de respuesta en un objeto UserRemoteResponse
                                .block();
                // Convierte el flujo reactivo en bloqueante — espera la respuesta
        }
}
