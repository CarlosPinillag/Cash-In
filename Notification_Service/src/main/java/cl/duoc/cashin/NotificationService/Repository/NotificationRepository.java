package cl.duoc.cashin.NotificationService.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.duoc.cashin.NotificationService.Model.NotificationModel;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationModel, Long> {

    List<NotificationModel> findByUserIdOrderByFechaCreacionDesc(Long userId);

    List<NotificationModel> findByUserIdAndLeidaFalse(Long userId);

    List<NotificationModel> findByUserIdAndTipo(Long userId, String tipo);

    List<NotificationModel> findByEstado(String estado);

    @Query("SELECT COUNT(n) FROM NotificationModel n WHERE n.userId = :userId AND n.leida = false")
    Long contarNoLeidasPorUsuario(@Param("userId") Long userId);
}
