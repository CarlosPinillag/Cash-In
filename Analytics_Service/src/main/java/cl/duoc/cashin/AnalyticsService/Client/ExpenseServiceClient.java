package cl.duoc.cashin.AnalyticsService.Client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import cl.duoc.cashin.AnalyticsService.Exception.ResourceNotFoundException;
import reactor.core.publisher.Mono;

@Component
public class ExpenseServiceClient {

        private final WebClient webClient;

        public ExpenseServiceClient(@Qualifier("expenseWebClient") WebClient webClient) {
                this.webClient = webClient;
        }

        public Double obtenerTotalGastadoPorUsuario(Long userId) {
                return webClient.get()
                                .uri("/api/v1/expenses/user/{userId}/total", userId)
                                .retrieve()
                                .onStatus(
                                                // Si expense-service retorna 404, el usuario no tiene gastos
                                                status -> status.value() == 404,
                                                response -> Mono.error(
                                                                new ResourceNotFoundException(
                                                                                "No se encontraron gastos para el usuario id "
                                                                                                + userId)))
                                .onStatus(
                                                // Si expense-service retorna cualquier otro error (500)
                                                status -> status.is5xxServerError(),
                                                response -> Mono.error(
                                                                new RuntimeException(
                                                                                "Error al comunicarse con expense-service")))
                                .bodyToMono(Double.class)
                                .defaultIfEmpty(0.0)
                                .block();
                // Convierte el flujo reactivo en bloqueante — espera la respuesta
        }
}
