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

        public AlertRemoteResponse crearAlerta(Long userId, Long budgetId, String tipo, String mensaje, String authHeader) {

                Map<String, Object> body = Map.of(
                                "userId", userId,
                                "budgetId", budgetId,
                                "tipo", tipo,
                                "mensaje", mensaje);

                return webClient.post()
                                .uri("/api/v1/alerts")
                                .header("Authorization", authHeader)
                                .bodyValue(body)
                                .retrieve()
                                .onStatus(
                                                status -> status.is5xxServerError(),
                                                response -> Mono.error(
                                                                new RuntimeException(
                                                                                "Error al comunicarse con alert-service al crear alerta")))
                                .bodyToMono(AlertRemoteResponse.class)

                                .block();

        }
}
