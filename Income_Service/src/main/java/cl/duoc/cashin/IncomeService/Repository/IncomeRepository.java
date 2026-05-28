package cl.duoc.cashin.IncomeService.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.duoc.cashin.IncomeService.Model.IncomeModel;

import java.util.List;

public interface IncomeRepository extends JpaRepository<IncomeModel, Long> {

    List<IncomeModel> findByUserId(Long userId);

    List<IncomeModel> findByUserIdAndCategoria(Long userId, String categoria);

    List<IncomeModel> findByUserIdAndRecurrente(Long userId, Boolean recurrente);

    @Query("SELECT SUM(i.monto) FROM IncomeModel i WHERE i.userId = :userId")
    Double sumMontoByUserId(@Param("userId") Long userId);
}
