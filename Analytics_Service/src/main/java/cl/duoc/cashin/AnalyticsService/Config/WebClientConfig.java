package cl.duoc.cashin.AnalyticsService.Config;

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
    @Qualifier("incomeWebClient")
    public WebClient incomeWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8083")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    @Qualifier("budgetWebClient")
    public WebClient budgetWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8084")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
