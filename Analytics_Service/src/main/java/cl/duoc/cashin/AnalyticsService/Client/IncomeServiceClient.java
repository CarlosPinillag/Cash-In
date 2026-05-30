package cl.duoc.cashin.AnalyticsService.Client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import cl.duoc.cashin.AnalyticsService.Exception.ResourceNotFoundException;
import reactor.core.publisher.Mono;

@Component
public class IncomeServiceClient {

        private final WebClient webClient;

        public IncomeServiceClient(@Qualifier("incomeWebClient") WebClient webClient) {
                this.webClient = webClient;
        }

        public Double obtenerTotalIngresosPorUsuario(Long userId, String authHeader) {
                return webClient.get()
                                .uri("/api/v1/incomes/user/{userId}/total", userId)
                                .header("Authorization", authHeader)
                                .retrieve()
                                .onStatus(
                                                status -> status.value() == 404,
                                                response -> Mono.error(
                                                                new ResourceNotFoundException(
                                                                                "No se encontraron ingresos para el usuario id "
                                                                                                + userId)))
                                .onStatus(
                                                status -> status.is5xxServerError(),
                                                response -> Mono.error(
                                                                new RuntimeException(
                                                                                "Error al comunicarse con income-service")))
                                .bodyToMono(Double.class)
                                .defaultIfEmpty(0.0)
                                .block();
        }
}
