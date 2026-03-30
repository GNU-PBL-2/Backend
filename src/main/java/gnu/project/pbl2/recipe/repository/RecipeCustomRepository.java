package gnu.project.pbl2.recipe.repository;

import gnu.project.pbl2.recipe.dto.request.RecipeSearchRequest;
import gnu.project.pbl2.recipe.dto.response.RecipeSearchResponse;
import gnu.project.pbl2.recipe.entity.Recipe;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;

public interface RecipeCustomRepository {
    Page<RecipeSearchResponse> searchRecipes(
        final RecipeSearchRequest request,
        final Long userId
    );
    Set<Long> findCookableRecipeIds(List<Long> recipeIds, Long userId);

    Map<Long, Long> findExpiringCountMap(List<Long> recipeIds, Long userId);

    Set<Long> findFavoriteRecipeIds(List<Long> recipeIds, Long userId);

    // 상세 조회 추가
    Optional<Recipe> findDetailById(Long recipeId);
    boolean isFavorite(Long recipeId, Long userId);

}
