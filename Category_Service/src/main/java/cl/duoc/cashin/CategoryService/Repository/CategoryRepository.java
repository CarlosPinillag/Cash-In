package cl.duoc.cashin.CategoryService.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.cashin.CategoryService.Model.CategoryModel;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryModel, Long> {

    List<CategoryModel> findByActivo(Boolean activo);

    List<CategoryModel> findByTipo(String tipo);

    List<CategoryModel> findByTipoAndActivo(String tipo, Boolean activo);

    Optional<CategoryModel> findByNombre(String nombre);
}
