package cl.duoc.cashin.AnalyticsService.Model;

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
@Table(name = "Analytics")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAnalytics;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Double totalIngresos;

    @Column(nullable = false)
    private Double totalGastos;

    @Column(nullable = false)
    private Double balance;

    @Column(nullable = false)
    private Double tasaAhorro;

    @Column(nullable = false)
    private String estadoBalance;

    @Column(nullable = false)
    private LocalDate fechaGeneracion;
}
