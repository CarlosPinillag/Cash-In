package cl.duoc.cashin.AnalyticsService.Client;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import cl.duoc.cashin.AnalyticsService.dto.Response.BudgetRemoteResponse;
import reactor.core.publisher.Mono;

@Component
public class BudgetServiceClient {

    private final WebClient webClient;

    public BudgetServiceClient(@Qualifier("budgetWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public List<BudgetRemoteResponse> obtenerPresupuestosPorUsuario(Long userId, String authHeader) {
        return webClient.get()
                .uri("/api/v1/budgets/user/{userId}", userId)
                .header("Authorization", authHeader)
                .retrieve()
                .onStatus(
                        status -> status.is5xxServerError(),
                        response -> Mono.error(
                                new RuntimeException(
                                        "Error al comunicarse con budget-service")))
                .bodyToMono(new ParameterizedTypeReference<List<BudgetRemoteResponse>>() {
                })
                .defaultIfEmpty(List.of())
                .block();
    }
}
