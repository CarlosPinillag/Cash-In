package cl.duoc.cashin.ExpenseService.Model;

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
@Table(name = "Expense")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long idExpense;

    @Column(nullable = false)

    private Long userId;

    @Column(nullable = false)

    private Long categoryId;

    @Column(nullable = false)

    private String nombreCategoria;

    @Column(nullable = false)

    private Double monto;

    @Column(nullable = false)

    private String descripcion;

    @Column(nullable = false)

    private LocalDate fecha;

    @Column(nullable = false)

    private String tipo;
}
