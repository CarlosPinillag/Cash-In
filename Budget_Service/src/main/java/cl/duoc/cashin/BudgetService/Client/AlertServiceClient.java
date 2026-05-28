package cl.duoc.cashin.BudgetService.Client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import cl.duoc.cashin.BudgetService.dto.Response.AlertRemoteResponse;
import reactor.core.publisher.Mono;

@Component

public class AlertServiceClient {

        private final WebClient webClient;

        public AlertServiceClient(@Qualifier("alertWebClient") WebClient webClient) {
                this.webClient = webClient;
        }

        // Llama a alert-service para crear una alerta de presupuesto superado

        // tipo: "ALERTA_80" (>=80%) o "ALERTA_100" (>=100%)
        public AlertRemoteResponse crearAlerta(Long userId, Long budgetId, String tipo, String mensaje) {

                Map<String, Object> body = Map.of(
                                "userId", userId,
                                "budgetId", budgetId,
                                "tipo", tipo,
                                "mensaje", mensaje);

                return webClient.post()
                                .uri("/api/v1/alerts")
                                .bodyValue(body)
                                .retrieve()
                                .onStatus(
                                                // Si alert-service retorna error de servidor
                                                status -> status.is5xxServerError(),
                                                response -> Mono.error(
                                                                new RuntimeException(
                                                                                "Error al comunicarse con alert-service al crear alerta")))
                                .bodyToMono(AlertRemoteResponse.class)

                                .block();

        }
}
