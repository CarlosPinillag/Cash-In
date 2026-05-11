package cl.duoc.cashin.UserService.Client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import cl.duoc.cashin.UserService.dto.Response.UserResponse;
import cl.duoc.cashin.UserService.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component // Le dice a Spring: "Esta clase es un bean, créala automáticamente y
           // adminístrala
@RequiredArgsConstructor // Lombok genera automáticamente un constructor con los atributos final
public class UserServiceClient {

    private final WebClient webcliente;

    public UserResponse obtenerUserPorId(Long id) {
        return webcliente.get() // Indica que la petición HTTP es tipo GET (puede ser post, put, etc...)
                .uri("/api/v1/fiestas/{id}", id) // Construye la URL
                .retrieve() // Ejecuta la petición HTTP
                .onStatus(
                        status -> status.value() == 404,
                        reponse -> Mono.error(new ResourceNotFoundException("Id del Usuario no existe")))
                .bodyToMono(UserResponse.class) // Convierte el JSON de respuesta en un objeto Java
                .block(); // Convierte el flujo reactivo en bloqueante osea Espera la respuesta
    }

}
