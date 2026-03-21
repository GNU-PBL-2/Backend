package gnu.project.pbl2.user.repository;

import gnu.project.pbl2.user.entity.UserAllergy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAllergyRepository extends JpaRepository<UserAllergy,Long> {

    void deleteByUserId(Long id);
}
