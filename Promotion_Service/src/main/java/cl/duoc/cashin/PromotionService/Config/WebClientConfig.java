package cl.duoc.cashin.PromotionService.Config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @Qualifier("userWebClient")
    public WebClient userWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8080")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    @Qualifier("categoryWebClient")
    public WebClient categoryWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8085")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
