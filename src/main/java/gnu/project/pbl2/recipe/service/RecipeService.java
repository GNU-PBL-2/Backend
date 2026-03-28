package gnu.project.pbl2.recipe.service;

import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.recipe.dto.request.RecipeSearchRequest;
import gnu.project.pbl2.recipe.dto.response.RecipeSearchResponse;
import gnu.project.pbl2.recipe.repository.RecipeRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;

    public Page<RecipeSearchResponse> getRecipes(
        final RecipeSearchRequest request,
        final Accessor accessor
    ) {
        Long userId = accessor.getUserId();

        // 1. 레시피 목록 조회 (뱃지 없이)
        Page<RecipeSearchResponse> page = recipeRepository.searchRecipes(request, userId);
        List<Long> recipeIds = page.getContent().stream()
            .map(RecipeSearchResponse::id)
            .toList();

        if (recipeIds.isEmpty()) {
            return page;
        }

        // 2. 뱃지 데이터 일괄 조회 (쿼리 3개)
        Set<Long> cookableIds = recipeRepository.findCookableRecipeIds(recipeIds, userId);
        Map<Long, Long> expiringMap = recipeRepository.findExpiringCountMap(recipeIds, userId);
        Set<Long> favoriteIds = recipeRepository.findFavoriteRecipeIds(recipeIds, userId);

        // 3. 뱃지 조립
        List<RecipeSearchResponse> assembled = page.getContent().stream()
            .map(r -> r.withBadge(
                cookableIds.contains(r.id()),
                expiringMap.getOrDefault(r.id(), 0L).intValue(),
                favoriteIds.contains(r.id())
            ))
            .toList();

        return new PageImpl<>(assembled, page.getPageable(), page.getTotalElements());
    }
}
