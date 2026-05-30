package cl.duoc.cashin.ExpenseService.Config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebClientConfig implements WebMvcConfigurer {

    private final JwtFilter jwtFilter;

    public WebClientConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtFilter)
                .addPathPatterns("/api/v1/**");
    }

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