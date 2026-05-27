package cl.duoc.cashin.AuthService.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import cl.duoc.cashin.AuthService.Model.AuthTokenModel;
import java.util.Optional;
import java.util.List;

public interface AuthTokenRepository extends JpaRepository<AuthTokenModel, Long> {

    // Spring genera: SELECT * FROM AuthToken WHERE username = ? AND activo = true
    // Se usa para invalidar el token anterior antes de crear uno nuevo
    Optional<AuthTokenModel> findByUsernameAndActivoTrue(String username);

    // Spring genera: SELECT * FROM AuthToken WHERE token = ? AND activo = true
    // Se usa para validar si un token recibido es vigente
    Optional<AuthTokenModel> findByTokenAndActivoTrue(String token);

    // Spring genera: SELECT * FROM AuthToken WHERE username = ?
    // Se usa para obtener el historial de tokens de un usuario
    List<AuthTokenModel> findByUsername(String username);
}
