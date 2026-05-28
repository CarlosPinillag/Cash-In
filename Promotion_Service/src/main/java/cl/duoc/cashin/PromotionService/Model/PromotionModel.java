package cl.duoc.cashin.PromotionService.Model;

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
@Table(name = "Promotion")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPromotion;

    @Column(nullable = false)
    private Long categoryId;

    @Column
    private String nombreCategoria;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private String tipoDescuento;

    @Column(nullable = false)
    private Double valorDescuento;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false)
    private Integer usoMaximo;

    @Column(nullable = false)
    private Integer usosActuales;

    @Column(nullable = false)
    private Boolean activo;
}
