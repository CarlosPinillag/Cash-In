package cl.duoc.cashin.AlertService.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.duoc.cashin.AlertService.Model.AlertModel;

public interface AlertRepository extends JpaRepository<AlertModel, Long> {

    // Spring genera: SELECT * FROM Alert WHERE userId = ? ORDER BY fechaCreacion DESC
    // Usado para listar todas las alertas de un usuario, más recientes primero
    List<AlertModel> findByUserIdOrderByFechaCreacionDesc(Long userId);

    // Spring genera: SELECT * FROM Alert WHERE userId = ? AND leida = false
    // Usado para listar solo las alertas pendientes de lectura del usuario
    List<AlertModel> findByUserIdAndLeidaFalse(Long userId);

    // Spring genera: SELECT * FROM Alert WHERE budgetId = ?
    // Usado para consultar todas las alertas asociadas a un presupuesto
    List<AlertModel> findByBudgetId(Long budgetId);

    // JPQL: cuenta las alertas no leídas de un usuario
    // Útil para mostrar el badge de notificaciones pendientes
    @Query("SELECT COUNT(a) FROM AlertModel a WHERE a.userId = :userId AND a.leida = false")
    Long contarNoLeidasPorUsuario(@Param("userId") Long userId);
}
