package cl.duoc.cashin.AnalyticsService.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class JwtFilter implements HandlerInterceptor {

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String authHeader = request.getHeader("Authorization");

        // warn si falta el token 
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Acceso denegado — token no proporcionado en {}", request.getRequestURI());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Token no proporcionado\"}");
            return false;
        }

        String token = authHeader.substring(7);

        try {
            JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer("auth-service")
                    .build()
                    .verify(token);

            // debug cuando token es válido
            log.debug("Token válido aceptado en {}", request.getRequestURI());

        } catch (JWTVerificationException e) {
            //  warn si token es inválido/expirado 
            log.warn("Acceso denegado — token inválido o expirado en {}: {}",
                    request.getRequestURI(), e.getMessage());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Token invalido o expirado\"}");
            return false;
        }

        return true;
    }
}
