package cl.duoc.cashin.AuthService.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
// Esta clase contiene configuracion del contexto (beans)
public class WebClientConfig {

    @Bean
    // Ejecuta este metodo y guarda su resultado como un objeto administrado.
    // Lo deja disponible para inyeccion por constructor en UserServiceClient
    public WebClient webClient() {
        return WebClient.builder()
                // URL base de user-service — auth-service llama a este servicio
                // para verificar que el usuario existe y obtener su passwordHash
                .baseUrl("http://localhost:8080")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
