package cl.duoc.cashin.ExpenseService.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.duoc.cashin.ExpenseService.Model.ExpenseModel;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<ExpenseModel, Long> {

    List<ExpenseModel> findByUserId(Long userId);

    List<ExpenseModel> findByUserIdAndCategoryId(Long userId, Long categoryId);

    @Query("SELECT SUM(e.monto) FROM ExpenseModel e WHERE e.userId = :userId")
    Double sumMontoByUserId(@Param("userId") Long userId);
}
