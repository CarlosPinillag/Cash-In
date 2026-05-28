package cl.duoc.cashin.UserService.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import cl.duoc.cashin.UserService.Model.UserModel;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<UserModel, Long> {

    Optional<UserModel> findByEmail(String email);

    boolean existsByEmail(String email);

    List<UserModel> findByActivoTrue();
}
