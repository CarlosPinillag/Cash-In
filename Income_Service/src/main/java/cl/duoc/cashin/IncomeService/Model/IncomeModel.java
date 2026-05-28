package cl.duoc.cashin.IncomeService.Model;

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
@Table(name = "Income")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncomeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idIncome;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Double monto;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private Boolean recurrente;

    @Column
    private String frecuencia;
}
