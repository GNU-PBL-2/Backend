package gnu.project.pbl2.Fridge.repository;

import gnu.project.pbl2.Fridge.entity.Fridge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FridgeRepository extends JpaRepository<Fridge, Long> {

    List<Fridge> findAllByMember_Id(Long memberId);
}
