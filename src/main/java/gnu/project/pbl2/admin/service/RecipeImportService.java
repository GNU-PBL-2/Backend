package gnu.project.pbl2.admin.service;

import gnu.project.pbl2.admin.dto.AdminRecipeCreateRequest;
import gnu.project.pbl2.admin.dto.FastApiJobStatus;
import gnu.project.pbl2.admin.dto.GeminiRecipeDto;
import gnu.project.pbl2.admin.dto.ImportStartResponse;
import gnu.project.pbl2.admin.dto.ImportStatusResponse;
import gnu.project.pbl2.common.entity.Category;
import gnu.project.pbl2.common.entity.Taste;
import gnu.project.pbl2.common.error.ErrorCode;
import gnu.project.pbl2.common.exception.BusinessException;
import gnu.project.pbl2.common.repository.CategoryRepository;
import gnu.project.pbl2.common.repository.TasteRepository;
import gnu.project.pbl2.fridge.entity.Ingredient;
import gnu.project.pbl2.fridge.repository.IngredientRepository;
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
import java.util.concurrent.ConcurrentHashMap;
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

    // jobId → (youtubeUrl, 저장된 recipeId)
    private final ConcurrentHashMap<String, ImportJobEntry> importJobs = new ConcurrentHashMap<>();

    private record ImportJobEntry(String youtubeUrl, Long recipeId) {}

    public ImportStartResponse startImport(final String youtubeUrl) {
        if (recipeRepository.existsByYoutubeUrl(youtubeUrl)) {
            throw new BusinessException(ErrorCode.RECIPE_ALREADY_EXISTS);
        }

        String jobId = geminiService.submitRecipeJob(youtubeUrl);
        importJobs.put(jobId, new ImportJobEntry(youtubeUrl, null));
        return new ImportStartResponse(jobId);
    }

    @Transactional
    public ImportStatusResponse checkImportStatus(final String jobId) {
        ImportJobEntry entry = importJobs.get(jobId);
        if (entry == null) {
            throw new BusinessException(ErrorCode.IMPORT_JOB_NOT_FOUND);
        }

        // 이미 저장 완료된 경우 재조회 없이 반환
        if (entry.recipeId() != null) {
            return new ImportStatusResponse("completed", entry.recipeId(), null);
        }

        FastApiJobStatus jobStatus = geminiService.getJobStatus(jobId);

        if ("completed".equals(jobStatus.status())) {
            Long recipeId = saveRecipeFromDto(jobStatus.result(), entry.youtubeUrl());
            importJobs.put(jobId, new ImportJobEntry(entry.youtubeUrl(), recipeId));
            return new ImportStatusResponse("completed", recipeId, null);
        }

        if ("failed".equals(jobStatus.status())) {
            importJobs.remove(jobId);
            return new ImportStatusResponse("failed", null, jobStatus.error());
        }

        return new ImportStatusResponse(jobStatus.status(), null, null);
    }

    private Long saveRecipeFromDto(final GeminiRecipeDto dto, final String youtubeUrl) {
        log.info("레시피 DB 저장: {}", dto.title());

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
        ));

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
                videoId = path.replaceFirst("^/", "");
            } else if (path.startsWith("/shorts/")) {
                videoId = path.substring("/shorts/".length());
            } else {
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
