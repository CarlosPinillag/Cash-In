package cl.duoc.apigateway.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Filtro opcional de log para ver que las peticiones pasan por el Gateway.
// No es obligatorio para que el enrutamiento funcione.
@Component
public class GatewayLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        System.out.println("====================================");
        System.out.println("Peticion recibida en API Gateway Cash-In");
        System.out.println("Metodo: " + request.getMethod());
        System.out.println("Ruta: " + request.getRequestURI());
        System.out.println("====================================");

        filterChain.doFilter(request, response);
    }
}
