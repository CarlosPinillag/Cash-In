package cl.duoc.cashin.PromotionService.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.duoc.cashin.PromotionService.Model.PromotionModel;

public interface PromotionRepository extends JpaRepository<PromotionModel, Long> {

    Optional<PromotionModel> findByCodigo(String codigo);

    List<PromotionModel> findByCategoryId(Long categoryId);

    List<PromotionModel> findByActivoTrue();

    List<PromotionModel> findByActivoTrueAndCategoryId(Long categoryId);

    @Query("SELECT p FROM PromotionModel p WHERE p.codigo = :codigo AND p.activo = true " +
            "AND p.fechaInicio <= :hoy AND p.fechaFin >= :hoy")
    Optional<PromotionModel> findActivaVigentePorCodigo(
            @Param("codigo") String codigo,
            @Param("hoy") LocalDate hoy);

    @Query("SELECT COUNT(p) FROM PromotionModel p WHERE p.categoryId = :categoryId AND p.activo = true")
    Long contarActivasPorCategoria(@Param("categoryId") Long categoryId);

    boolean existsByCodigo(String codigo);
}
