package cl.duoc.cashin.UserService.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import cl.duoc.cashin.UserService.Model.UserModel;

public interface UserRepository extends JpaRepository<UserModel, Long> {

}
