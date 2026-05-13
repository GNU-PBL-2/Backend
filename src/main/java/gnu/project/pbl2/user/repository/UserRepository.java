package gnu.project.pbl2.user.repository;

import gnu.project.pbl2.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByOauthInfo_SocialId(final String socialId);

    @Query("select u from User u where u.id = :id and u.isDeleted = false ")
    Optional<User> findActiveById(@Param("id") Long id);

    boolean existsByOauthInfo_SocialId(String socialId);

    @Query("SELECT uc.category.id FROM UserCategory uc WHERE uc.user.id = :userId")
    List<Long> findCategoryIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT ut.taste.id FROM UserTaste ut WHERE ut.user.id = :userId")
    List<Long> findTasteIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT ua.allergy.id FROM UserAllergy ua WHERE ua.user.id = :userId")
    List<Long> findAllergyIdsByUserId(@Param("userId") Long userId);
}
