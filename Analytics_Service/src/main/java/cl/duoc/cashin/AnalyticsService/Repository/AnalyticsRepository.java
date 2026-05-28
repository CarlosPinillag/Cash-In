package cl.duoc.cashin.AnalyticsService.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.duoc.cashin.AnalyticsService.Model.AnalyticsModel;

public interface AnalyticsRepository extends JpaRepository<AnalyticsModel, Long> {

    List<AnalyticsModel> findByUserIdOrderByFechaGeneracionDesc(Long userId);

    Optional<AnalyticsModel> findTopByUserIdOrderByFechaGeneracionDesc(Long userId);

    @Query("SELECT AVG(a.tasaAhorro) FROM AnalyticsModel a WHERE a.userId = :userId")
    Double promedioTasaAhorroPorUsuario(@Param("userId") Long userId);

    @Query("SELECT COUNT(a) FROM AnalyticsModel a WHERE a.userId = :userId AND a.estadoBalance = 'NEGATIVO'")
    Long contarBalancesNegativos(@Param("userId") Long userId);
}
