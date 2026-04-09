package gnu.project.pbl2.Fridge.repository;

import gnu.project.pbl2.Fridge.entity.Fridge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** 냉장고 저장소 */
public interface FridgeRepository extends JpaRepository<Fridge, Long> {

    /** 회원별 냉장고 목록 조회 */
    List<Fridge> findAllByMember_Id(Long memberId);

    /** 재료 사용 여부 확인 */
    boolean existsByIngredient_IngredientId(Long ingredientId);
}
