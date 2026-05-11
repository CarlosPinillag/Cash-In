package cl.duoc.cashin.UserService.Config;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // Esta clase contiene configuración del contexto (beans)

public class WebClientConfig {

    @Bean // Ejecuta este método y guarda su resultado como un objeto administrado. La
          // deja disponible para inyección (@Autowired o constructor)
    public WebClient webClient() {
        return WebClient.builder() // Crea un builder para configurar el cliente HTTP
                .baseUrl("http://localhost:8080") // Define la URL base del microservicio de fiestas
                .defaultHeader("Content-Type", "application/json") // Agrega un header HTTP por defecto a TODAS las
                                                                   // solicitudes
                .build(); // Construye el objeto final WebClient

    }
}
