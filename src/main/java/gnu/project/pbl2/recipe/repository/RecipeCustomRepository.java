package gnu.project.pbl2.recipe.repository;

import gnu.project.pbl2.recipe.dto.request.RecipeSearchRequest;
import gnu.project.pbl2.recipe.dto.response.RecipeSearchResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;

public interface RecipeCustomRepository {
    Page<RecipeSearchResponse> searchRecipes(
        final RecipeSearchRequest request,
        final Long userId
    );
    // 탭별 레시피 목록 조회

    // 조리 가능한 레시피 ID 집합
    Set<Long> findCookableRecipeIds(List<Long> recipeIds, Long userId);

    // 레시피별 임박 재료 개수 맵 (recipeId → count)
    Map<Long, Long> findExpiringCountMap(List<Long> recipeIds, Long userId);

    // 즐겨찾기한 레시피 ID 집합
    Set<Long> findFavoriteRecipeIds(List<Long> recipeIds, Long userId);

}
