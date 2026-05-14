package cl.duoc.cashin.UserService.Model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

@Entity
@Table(name = "User")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY = MySQL AUTO_INCREMENT. La BD asigna el ID, no tú.
    // Long (objeto) y no long (primitivo) porque puede ser null antes del primer
    // save()
    private Long idUser;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    // unique = true crea un índice UNIQUE en MySQL → no pueden existir dos emails
    // iguales
    private String email;

    @Column(nullable = false)
    // Nunca guardar password en texto plano. Siempre hash.
    private String passwordHash;

    @Column(nullable = false)
    private String telefono;

    @Column(nullable = false)
    private LocalDate fechaRegistro; // solo fecha, sin hora

    @Column(nullable = false)
    private Boolean activo; // ← CORREGIDO: Boolean objeto, no boolean primitivo

    @Column(nullable = false)
    private Double presupuestoMensual; // ← CORREGIDO: Double objeto, no double primitivo
}
