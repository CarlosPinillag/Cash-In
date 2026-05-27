package cl.duoc.cashin.AuthService.Model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "AuthToken")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthTokenModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY = MySQL AUTO_INCREMENT. La BD asigna el ID automaticamente.
    // Long (objeto) y no long (primitivo) porque puede ser null antes del primer save()
    private Long idToken;

    @Column(nullable = false)
    // Username corresponde al email del usuario autenticado
    private String username;

    @Column(nullable = false, length = 512)
    // Token UUID generado al hacer login — se invalida al hacer logout
    private String token;

    @Column(nullable = false)
    // Momento exacto en que se emitio el token
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    // Momento en que expira el token — se calcula como issuedAt + 24 horas
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    // true = token vigente, false = token invalidado por logout o por nuevo login
    private Boolean activo;
}
