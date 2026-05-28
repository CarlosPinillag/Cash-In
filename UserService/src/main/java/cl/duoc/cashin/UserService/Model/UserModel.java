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

    private Long idUser;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)

    private String email;

    @Column(nullable = false)

    private String passwordHash;

    @Column(nullable = false)
    private String telefono;

    @Column(nullable = false)
    private LocalDate fechaRegistro;

    @Column(nullable = false)
    private Boolean activo;

    @Column(nullable = false)
    private Double presupuestoMensual;
}
