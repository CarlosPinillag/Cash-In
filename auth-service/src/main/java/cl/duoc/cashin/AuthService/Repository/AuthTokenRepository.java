package cl.duoc.cashin.AuthService.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import cl.duoc.cashin.AuthService.Model.AuthTokenModel;
import java.util.Optional;
import java.util.List;

public interface AuthTokenRepository extends JpaRepository<AuthTokenModel, Long> {

    Optional<AuthTokenModel> findByUsernameAndActivoTrue(String username);

    Optional<AuthTokenModel> findByTokenAndActivoTrue(String token);

    List<AuthTokenModel> findByUsername(String username);
}
