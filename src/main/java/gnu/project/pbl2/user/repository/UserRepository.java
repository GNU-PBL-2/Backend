package gnu.project.pbl2.user.repository;

import gnu.project.pbl2.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByOauthInfo_SocialId(final String socialId);
    Optional<User> findById(final Long id);

}
