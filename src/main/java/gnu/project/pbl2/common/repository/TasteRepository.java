package gnu.project.pbl2.common.repository;

import gnu.project.pbl2.common.entity.Taste;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TasteRepository extends JpaRepository<Taste,Long> {

    Optional<Taste> findByName(String name);
}
