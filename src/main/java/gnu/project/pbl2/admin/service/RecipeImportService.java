package gnu.project.pbl2.admin.service;

import gnu.project.pbl2.Fridge.entity.Ingredient;
import gnu.project.pbl2.Fridge.repository.IngredientRepository;
import gnu.project.pbl2.admin.dto.GeminiRecipeDto;
import gnu.project.pbl2.common.entity.Category;
import gnu.project.pbl2.common.entity.Taste;
import gnu.project.pbl2.common.error.ErrorCode;
import gnu.project.pbl2.common.exception.BusinessException;
import gnu.project.pbl2.common.repository.CategoryRepository;
import gnu.project.pbl2.common.repository.TasteRepository;
import gnu.project.pbl2.recipe.entity.Recipe;
import gnu.project.pbl2.recipe.entity.RecipeIngredient;
import gnu.project.pbl2.recipe.entity.RecipeStep;
import gnu.project.pbl2.recipe.repository.RecipeIngredientRepository;
import gnu.project.pbl2.recipe.repository.RecipeRepository;
import gnu.project.pbl2.recipe.repository.RecipeStepRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeImportService {

    private final GeminiService geminiService;
    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;
    private final TasteRepository tasteRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeStepRepository recipeStepRepository;

    @Transactional
    public Long importFromYoutube(final String youtubeUrl) {
        if (recipeRepository.existsByYoutubeUrl(youtubeUrl)) {
            throw new BusinessException(ErrorCode.RECIPE_ALREADY_EXISTS);
        }

        log.info("Gemini 영상 분석 시작: {}", youtubeUrl);
        final GeminiRecipeDto dto = geminiService.extractRecipeFromYoutube(youtubeUrl);
        log.info("Gemini 분석 완료: {}", dto.title());
        final Category category = categoryRepository.findByName(dto.categoryName())
            .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        final Taste taste = tasteRepository.findByName(dto.tasteName())
            .orElseThrow(() -> new BusinessException(ErrorCode.TASTE_NOT_FOUND));

        final Recipe recipe = recipeRepository.save(Recipe.create(
                dto.title(),
                category,
                taste,
                dto.cookTimeMin(),
                dto.description(),
                youtubeUrl
            )
        );

        List<RecipeIngredient> recipeIngredients = dto.ingredients().stream()
            .map(ing -> {
                Ingredient ingredient = ingredientRepository.findByName(ing.name())
                    .orElseGet(() -> {
                        log.info("새 재료 생성: {}", ing.name());
                        return ingredientRepository.save(Ingredient.create(ing.name(), category));
                    });
                return recipeIngredientRepository.save(
                    RecipeIngredient.of(recipe, ingredient, ing.amount(), ing.unit(),
                        ing.isSubstitutable()));
            })
            .toList();

        List<RecipeStep> steps = new ArrayList<>();
        for (int i = 0; i < dto.steps().size(); i++) {
            steps.add(recipeStepRepository.save(RecipeStep.of(recipe, i + 1, dto.steps().get(i))));
        }

        recipe.addIngredients(recipeIngredients);
        recipe.addSteps(steps);

        return recipe.getId();
    }
}
