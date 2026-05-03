package gnu.project.pbl2.fridge.repository;

import gnu.project.pbl2.fridge.entity.Fridge;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FridgeRepository extends JpaRepository<Fridge, Long> {

    @Query("SELECT f.ingredient.id FROM Fridge f WHERE f.member.id = :userId")
    Set<Long> findIngredientIdsByUserId(@Param("userId") final Long userId);

    @Query("SELECT f.ingredient.id FROM Fridge f WHERE f.member.id = :userId AND f.expiryDate <= :threshold")
    Set<Long> findExpiringIngredientIds(
        @Param("userId") final Long userId,
        @Param("threshold") final LocalDate threshold
    );
    /** 회원별 냉장고 목록 조회 */
    List<Fridge> findAllByMember_Id(Long memberId);

    /** 재료 사용 여부 확인 */
    boolean existsByIngredient_Id(Long ingredientId);

    @EntityGraph(attributePaths = {"member", "ingredient"})
    @Query("SELECT f FROM Fridge f WHERE f.expiryDate IS NOT NULL AND f.expiryDate <= :threshold")
    List<Fridge> findAllExpiringFridgeItems(@Param("threshold") LocalDate threshold);
}
