package gnu.project.pbl2.recipeingredient.repository;

import gnu.project.pbl2.recipeingredient.entity.RecipeIngredient;
import gnu.project.pbl2.recipeingredient.entity.RecipeIngredientId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/** 레시피 재료 저장소 */
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, RecipeIngredientId> {

    /** 레시피별 재료 목록 조회 */
    @EntityGraph(attributePaths = "ingredient")
    List<RecipeIngredient> findAllById_RecipeIdOrderById_IngredientIdAsc(Long recipeId);

    /** 단건 조회 */
    @EntityGraph(attributePaths = "ingredient")
    Optional<RecipeIngredient> findById_RecipeIdAndId_IngredientId(
        Long recipeId,
        Long ingredientId
    );

    /** 여러 레시피의 재료 일괄 조회 */
    @EntityGraph(attributePaths = "ingredient")
    List<RecipeIngredient> findAllById_RecipeIdIn(Collection<Long> recipeIds);

    /** 중복 확인 */
    boolean existsById_RecipeIdAndId_IngredientId(Long recipeId, Long ingredientId);
}
