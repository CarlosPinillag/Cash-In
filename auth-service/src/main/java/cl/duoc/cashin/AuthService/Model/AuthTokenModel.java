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

    private Long idToken;

    @Column(nullable = false)

    private String username;

    @Column(nullable = false, length = 512)

    private String token;

    @Column(nullable = false)

    private LocalDateTime issuedAt;

    @Column(nullable = false)

    private LocalDateTime expiresAt;

    @Column(nullable = false)

    private Boolean activo;
}
