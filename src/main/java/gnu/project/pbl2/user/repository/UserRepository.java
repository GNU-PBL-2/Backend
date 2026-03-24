package gnu.project.pbl2.user.repository;

import gnu.project.pbl2.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByOauthInfo_SocialId(final String socialId);
    @Query("select u from User u where u.id = :id and u.isDeleted = false ")
    Optional<User> findActiveById(@Param("id") Long id);

    boolean existsByOauthInfo_SocialId(String socialId);
}
