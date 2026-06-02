package gnu.project.pbl2.admin.service;

import gnu.project.pbl2.admin.dto.AdminRecipeCreateRequest;
import gnu.project.pbl2.fridge.entity.Ingredient;
import gnu.project.pbl2.fridge.repository.IngredientRepository;
import gnu.project.pbl2.admin.dto.GeminiRecipeDto;
import gnu.project.pbl2.common.entity.Category;
import gnu.project.pbl2.common.entity.Taste;
import gnu.project.pbl2.common.error.ErrorCode;
import gnu.project.pbl2.common.exception.BusinessException;
import gnu.project.pbl2.common.repository.CategoryRepository;
import gnu.project.pbl2.common.repository.TasteRepository;
import gnu.project.pbl2.recipe.entity.Recipe;
import gnu.project.pbl2.recipe.entity.RecipeStep;
import gnu.project.pbl2.recipe.repository.RecipeRepository;
import gnu.project.pbl2.recipe.repository.RecipeStepRepository;
import gnu.project.pbl2.recipeingredient.entity.RecipeIngredient;
import gnu.project.pbl2.recipeingredient.repository.RecipeIngredientRepository;
import java.net.URI;
import java.net.URISyntaxException;
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
                youtubeUrl,
                extractThumbnailUrl(youtubeUrl)
            )
        );

        dto.ingredients().forEach(ing -> {
            Ingredient ingredient = ingredientRepository.findByName(ing.name())
                .orElseGet(() -> {
                    log.info("새 재료 생성: {}", ing.name());
                    return ingredientRepository.save(Ingredient.create(ing.name(), category));
                });
            recipeIngredientRepository.save(
                RecipeIngredient.create(recipe.getId(), ingredient,
                    ing.amount(), ing.unit(), ing.isSubstitutable()));
        });

        List<RecipeStep> steps = new ArrayList<>();
        for (int i = 0; i < dto.steps().size(); i++) {
            steps.add(recipeStepRepository.save(RecipeStep.of(recipe, i + 1, dto.steps().get(i))));
        }

        recipe.addSteps(steps);

        return recipe.getId();
    }

    @Transactional
    public Long createManually(final AdminRecipeCreateRequest request) {
        final Category category = categoryRepository.findByName(request.categoryName())
            .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        final Taste taste = tasteRepository.findByName(request.tasteName())
            .orElseThrow(() -> new BusinessException(ErrorCode.TASTE_NOT_FOUND));

        String thumbnailUrl = (request.thumbnailUrl() != null && !request.thumbnailUrl().isBlank())
            ? request.thumbnailUrl()
            : extractThumbnailUrl(request.youtubeUrl());

        final Recipe recipe = recipeRepository.save(Recipe.create(
            request.title(), category, taste, request.cookTimeMin(),
            request.description(), request.youtubeUrl(), thumbnailUrl
        ));

        if (request.ingredients() != null) {
            request.ingredients().forEach(ing -> {
                Ingredient ingredient = ingredientRepository.findByName(ing.name())
                    .orElseGet(() -> ingredientRepository.save(Ingredient.create(ing.name(), category)));
                recipeIngredientRepository.save(
                    RecipeIngredient.create(recipe.getId(), ingredient,
                        ing.amount(), ing.unit(), ing.isSubstitutable()));
            });
        }

        List<RecipeStep> steps = new ArrayList<>();
        if (request.steps() != null) {
            for (int i = 0; i < request.steps().size(); i++) {
                steps.add(recipeStepRepository.save(RecipeStep.of(recipe, i + 1, request.steps().get(i))));
            }
        }
        recipe.addSteps(steps);

        return recipe.getId();
    }

    private String extractThumbnailUrl(String youtubeUrl) {
        try {
            URI uri = new URI(youtubeUrl);
            String host = uri.getHost() == null ? "" : uri.getHost();
            String path = uri.getPath() == null ? "" : uri.getPath();
            String query = uri.getQuery() == null ? "" : uri.getQuery();

            String videoId = null;

            if (host.contains("youtu.be")) {
                // https://youtu.be/VIDEO_ID
                videoId = path.replaceFirst("^/", "");
            } else if (path.startsWith("/shorts/")) {
                // https://www.youtube.com/shorts/VIDEO_ID
                videoId = path.substring("/shorts/".length());
            } else {
                // https://www.youtube.com/watch?v=VIDEO_ID
                for (String param : query.split("&")) {
                    if (param.startsWith("v=")) {
                        videoId = param.substring(2);
                        break;
                    }
                }
            }

            if (videoId == null || videoId.isBlank()) {
                return null;
            }
            return "https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg";
        } catch (URISyntaxException e) {
            return null;
        }
    }
}