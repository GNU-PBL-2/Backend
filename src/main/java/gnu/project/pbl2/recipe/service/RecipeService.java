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
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    @Transactional(readOnly = true)
    public Page<RecipeSearchResponse> getRecipes(
        final RecipeSearchRequest request,
        final Accessor accessor
    ) {
        Long userId = accessor.getUserId();

        Page<RecipeSearchResponse> page = recipeRepository.searchRecipes(request, userId);
        List<Long> recipeIds = page.getContent().stream()
            .map(RecipeSearchResponse::id)
            .toList();

        if (recipeIds.isEmpty()) {
            return page;
        }

        Set<Long> cookableIds = recipeRepository.findCookableRecipeIds(recipeIds, userId);
        Map<Long, Long> expiringMap = recipeRepository.findExpiringCountMap(recipeIds, userId);
        Set<Long> favoriteIds = recipeRepository.findFavoriteRecipeIds(recipeIds, userId);

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
