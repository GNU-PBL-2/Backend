package gnu.project.pbl2.recipe.service;

import static gnu.project.pbl2.fridge.enumerated.FridgeStatus.ENOUGH;
import static gnu.project.pbl2.fridge.enumerated.FridgeStatus.EXPIRING;
import static gnu.project.pbl2.fridge.enumerated.FridgeStatus.NONE;
import static gnu.project.pbl2.recipe.dto.response.RecipeResponseDto.*;

import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.common.entity.Category;
import gnu.project.pbl2.common.entity.Taste;
import gnu.project.pbl2.common.error.ErrorCode;
import gnu.project.pbl2.common.exception.BusinessException;
import gnu.project.pbl2.common.repository.CategoryRepository;
import gnu.project.pbl2.common.repository.TasteRepository;
import gnu.project.pbl2.fridge.enumerated.FridgeStatus;
import gnu.project.pbl2.fridge.repository.FridgeRepository;
import gnu.project.pbl2.ingredient.entity.Ingredient;
import gnu.project.pbl2.ingredient.repository.IngredientRepository;
import gnu.project.pbl2.recipe.dto.request.RecipeSearchRequest;
import gnu.project.pbl2.recipe.dto.request.RecipeUpdateRequest;
import gnu.project.pbl2.recipe.dto.response.RecipeResponseDto;
import gnu.project.pbl2.recipe.dto.response.RecipeSearchResponse;
import gnu.project.pbl2.recipe.entity.Recipe;
import gnu.project.pbl2.recipe.entity.RecipeIngredient;
import gnu.project.pbl2.recipe.entity.RecipeStep;
import gnu.project.pbl2.recipe.repository.RecipeIngredientRepository;
import gnu.project.pbl2.recipe.repository.RecipeRepository;
import gnu.project.pbl2.recipe.repository.RecipeStepRepository;
import java.time.LocalDate;
import java.util.ArrayList;
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
    private final CategoryRepository categoryRepository;
    private final TasteRepository tasteRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeStepRepository recipeStepRepository;

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

        List<RecipeIngredientDetail> ingredients =
            foundRecipe.getIngredients().stream()
                .map(ri -> new RecipeIngredientDetail(
                    ri.getIngredient().getId(),
                    ri.getIngredient().getName(),
                    ri.getAmount(),
                    ri.getUnit(),
                    ri.isSubstitutable(),
                    getFridgeStatus(
                        ri.getIngredient().getId(),
                        myIngredientIds,
                        expiringIngredientIds
                    )
                ))
                .toList();

        List<RecipeStepDetail> steps =
            foundRecipe.getSteps().stream()
                .sorted(Comparator.comparingInt(RecipeStep::getStepOrder))
                .map(s -> new RecipeStepDetail(
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

    private FridgeStatus getFridgeStatus(
        final Long ingredientId,
        final Set<Long> myIngredientIds,
        final Set<Long> expiringIngredientIds
    ) {
        if (!myIngredientIds.contains(ingredientId)) {
            return NONE;
        }
        if (expiringIngredientIds.contains(ingredientId)) {
            return EXPIRING;
        }
        return ENOUGH;
    }

    @Transactional
    public Long deleteRecipe(final Long id, final Accessor accessor) {
        final Recipe recipe = recipeRepository.findDetailById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RECIPE_NOT_FOUND));
        recipe.delete();
        return recipe.getId();
    }

    @Transactional
    public Long updateRecipe(
        final Long id,
        final RecipeUpdateRequest request,
        final Accessor accessor
    ) {
        Recipe recipe = recipeRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RECIPE_NOT_FOUND));

        Category category = categoryRepository.findByName(request.categoryName())
            .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        Taste taste = tasteRepository.findByName(request.tasteName())
            .orElseThrow(() -> new BusinessException(ErrorCode.TASTE_NOT_FOUND));

        recipe.update(
            request.title(),
            category,
            taste,
            request.cookTimeMin(),
            request.description()
        );

        recipe.clearCollections();

        List<RecipeIngredient> newIngredients = request.ingredients().stream()
            .map(ingDto -> {
                Ingredient ingredient = ingredientRepository.findByName(ingDto.name())
                    .orElseGet(() -> ingredientRepository.save(
                        Ingredient.create(ingDto.name(), category)));
                return recipeIngredientRepository.save(
                    RecipeIngredient.of(recipe, ingredient, ingDto.amount(), ingDto.unit(),
                        ingDto.isSubstitutable()));
            })
            .toList();

        List<RecipeStep> newSteps = new ArrayList<>();

        for (int i = 0; i < request.steps().size(); i++) {
            newSteps.add(
                recipeStepRepository.save(RecipeStep.of(recipe, i + 1, request.steps().get(i))));
        }

        recipe.addIngredients(newIngredients);
        recipe.addSteps(newSteps);

        return recipe.getId();
    }
}
