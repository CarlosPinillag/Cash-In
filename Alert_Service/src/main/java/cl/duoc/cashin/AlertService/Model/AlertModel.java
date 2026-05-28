package cl.duoc.cashin.AlertService.Model;

import java.time.LocalDate;

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
@Table(name = "Alert")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAlert;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long budgetId;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(nullable = false)
    private Boolean leida;

    @Column(nullable = false)
    private LocalDate fechaCreacion;
}
