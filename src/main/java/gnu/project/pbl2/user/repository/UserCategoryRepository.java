package gnu.project.pbl2.user.repository;

import gnu.project.pbl2.user.entity.UserCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCategoryRepository extends JpaRepository<UserCategory,Long> {

    void deleteByUserId(Long id);
}
