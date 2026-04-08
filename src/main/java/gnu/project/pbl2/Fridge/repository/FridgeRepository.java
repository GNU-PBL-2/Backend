package gnu.project.pbl2.Fridge.repository;

import gnu.project.pbl2.Fridge.entity.Fridge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 냉장고 재료 엔티티를 DB에서 조회/저장하는 JPA 저장소.
 */
public interface FridgeRepository extends JpaRepository<Fridge, Long> {

    // 특정 회원이 가진 냉장고 재료 전체를 조회한다.
    List<Fridge> findAllByMember_Id(Long memberId);
}
