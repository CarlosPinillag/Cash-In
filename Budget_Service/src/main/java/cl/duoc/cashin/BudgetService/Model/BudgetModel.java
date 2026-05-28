package cl.duoc.cashin.BudgetService.Model;

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
@Table(name = "Budget")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long idBudget;

    @Column(nullable = false)

    private Long userId;

    @Column

    private Long categoryId;

    @Column(nullable = false)

    private Double montoLimite;

    @Column(nullable = false)

    private String periodo;

    @Column(nullable = false)

    private Boolean activo;

    @Column

    private Double porcentajeUso;

    @Column(nullable = false)

    private LocalDate fechaInicio;
}
