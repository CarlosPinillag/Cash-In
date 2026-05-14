package cl.duoc.cashin.UserService.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import cl.duoc.cashin.UserService.Model.UserModel;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<UserModel, Long> {

    // Spring genera: SELECT * FROM User WHERE email = ?
    // Optional porque puede no existir — evita NullPointerException
    Optional<UserModel> findByEmail(String email);

    // Spring genera: SELECT CASE WHEN COUNT(*)>0 THEN 1 ELSE 0 END FROM User WHERE
    // email = ?
    // Se usa en el service para verificar email duplicado ANTES de guardar
    boolean existsByEmail(String email);

    // Spring genera: SELECT * FROM User WHERE activo = true
    // Se usa para listar solo usuarios activos (los eliminados tienen activo =
    // false)
    List<UserModel> findByActivoTrue();
}
