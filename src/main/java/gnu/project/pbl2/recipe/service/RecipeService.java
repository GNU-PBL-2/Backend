package gnu.project.pbl2.recipe.service;

import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.common.error.ErrorCode;
import gnu.project.pbl2.common.exception.BusinessException;
import gnu.project.pbl2.fridge.repository.FridgeRepository;
import gnu.project.pbl2.recipe.dto.request.RecipeSearchRequest;
import gnu.project.pbl2.recipe.dto.response.RecipeResponseDto;
import gnu.project.pbl2.recipe.dto.response.RecipeSearchResponse;
import gnu.project.pbl2.recipe.entity.Recipe;
import gnu.project.pbl2.recipe.entity.RecipeStep;
import gnu.project.pbl2.recipe.repository.RecipeRepository;
import java.time.LocalDate;
import java.util.Comparator;
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
    private final FridgeRepository fridgeRepository;

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

    @Transactional(readOnly = true)
    public RecipeResponseDto getRecipeDetail(
        final Long recipeId,
        final Accessor accessor
    ) {
        final Long userId = accessor.getUserId();

        final Recipe foundRecipe = recipeRepository.findDetailById(recipeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RECIPE_NOT_FOUND));

        boolean isFavorite = recipeRepository.isFavorite(recipeId, userId);

        Set<Long> myIngredientIds = fridgeRepository.findIngredientIdsByUserId(userId);

        Set<Long> expiringIngredientIds = fridgeRepository
            .findExpiringIngredientIds(userId, LocalDate.now().plusDays(3));

        List<RecipeResponseDto.RecipeIngredientDetail> ingredients =
            foundRecipe.getIngredients().stream()
                .map(ri -> new RecipeResponseDto.RecipeIngredientDetail(
                    ri.getIngredient().getId(),
                    ri.getIngredient().getName(),
                    ri.getAmount(),
                    ri.getUnit(),
                    ri.isSubstitutable(),
                    resolveFridgeStatus(
                        ri.getIngredient().getId(),
                        myIngredientIds,
                        expiringIngredientIds
                    )
                ))
                .toList();

        List<RecipeResponseDto.RecipeStepDetail> steps =
            foundRecipe.getSteps().stream()
                .sorted(Comparator.comparingInt(RecipeStep::getStepOrder))
                .map(s -> new RecipeResponseDto.RecipeStepDetail(
                    s.getStepOrder(),
                    s.getDescription()
                ))
                .toList();

        return new RecipeResponseDto(
            foundRecipe.getId(),
            foundRecipe.getTitle(),
            foundRecipe.getThumbnailUrl(),
            foundRecipe.getCategory().getName(),
            foundRecipe.getCookTimeMin(),
            foundRecipe.getYoutubeUrl(),
            isFavorite,
            ingredients,
            steps
        );
    }

    private RecipeResponseDto.FridgeStatus resolveFridgeStatus(
        final Long ingredientId,
        final Set<Long> myIngredientIds,
        final Set<Long> expiringIngredientIds
    ) {
        if (!myIngredientIds.contains(ingredientId)) {
            return RecipeResponseDto.FridgeStatus.NONE;
        }
        if (expiringIngredientIds.contains(ingredientId)) {
            return RecipeResponseDto.FridgeStatus.EXPIRING;
        }
        return RecipeResponseDto.FridgeStatus.ENOUGH;
    }
}
