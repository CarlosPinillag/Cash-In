package cl.duoc.cashin.BudgetService.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.cashin.BudgetService.Model.BudgetModel;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<BudgetModel, Long> {

        // Usado para listar todos los presupuestos de un usuario (activos e inactivos)
        List<BudgetModel> findByUserId(Long userId);

        // Usado para listar solo los presupuestos vigentes de un usuario
        List<BudgetModel> findByUserIdAndActivoTrue(Long userId);

        // Usado para verificar duplicados: no puede haber dos presupuestos activos del
        // mismo usuario+categoría+periodo
        Optional<BudgetModel> findByUserIdAndCategoryIdAndPeriodoAndActivoTrue(
                        Long userId, Long categoryId, String periodo);

        // Caso especial: presupuesto GLOBAL (categoryId = null)

        Optional<BudgetModel> findByUserIdAndCategoryIdIsNullAndPeriodoAndActivoTrue(
                        Long userId, String periodo);
}
