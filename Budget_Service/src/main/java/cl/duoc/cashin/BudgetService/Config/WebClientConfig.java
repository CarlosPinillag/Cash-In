package cl.duoc.cashin.BudgetService.Config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration

public class WebClientConfig {

    @Bean
    @Qualifier("expenseWebClient")
    public WebClient expenseWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8082")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    @Qualifier("alertWebClient")
    public WebClient alertWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8087")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
