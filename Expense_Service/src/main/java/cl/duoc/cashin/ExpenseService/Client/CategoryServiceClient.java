package cl.duoc.cashin.ExpenseService.Client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import cl.duoc.cashin.ExpenseService.Exception.ResourceNotFoundException;
import cl.duoc.cashin.ExpenseService.dto.Response.CategoryRemoteResponse;
import reactor.core.publisher.Mono;

@Component

public class CategoryServiceClient {

        private final WebClient webClient;

        public CategoryServiceClient(@Qualifier("categoryWebClient") WebClient webClient) {
                this.webClient = webClient;
        }

        public CategoryRemoteResponse obtenerCategoriaPorId(Long categoryId) {
                return webClient.get()
                                .uri("/api/v1/categories/{id}", categoryId)
                                .retrieve()
                                .onStatus(

                                                status -> status.value() == 404,
                                                response -> Mono.error(
                                                                new ResourceNotFoundException(
                                                                                "Categoria con id " + categoryId
                                                                                                + " no existe en el sistema")))
                                .onStatus(

                                                status -> status.is5xxServerError(),
                                                response -> Mono.error(
                                                                new RuntimeException(
                                                                                "Error al comunicarse con category-service")))
                                .bodyToMono(CategoryRemoteResponse.class)

                                .block();

        }
}
