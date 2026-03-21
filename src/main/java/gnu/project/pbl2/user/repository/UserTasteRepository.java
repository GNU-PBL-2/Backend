package gnu.project.pbl2.user.repository;

import gnu.project.pbl2.user.entity.UserTaste;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTasteRepository extends JpaRepository<UserTaste,Long> {

    void deleteByUserId(Long id);
}
