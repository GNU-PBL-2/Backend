package gnu.project.pbl2.recipeingredient.service;

import gnu.project.pbl2.Fridge.entity.Ingredient;
import gnu.project.pbl2.Fridge.repository.IngredientRepository;
import gnu.project.pbl2.common.error.ErrorCode;
import gnu.project.pbl2.common.exception.BusinessException;
import gnu.project.pbl2.recipeingredient.dto.request.RecipeIngredientCreateRequest;
import gnu.project.pbl2.recipeingredient.dto.request.RecipeIngredientUpdateRequest;
import gnu.project.pbl2.recipeingredient.dto.response.RecipeIngredientResponse;
import gnu.project.pbl2.recipeingredient.entity.RecipeIngredient;
import gnu.project.pbl2.recipeingredient.repository.RecipeIngredientRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 레시피 재료 비즈니스 로직 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeIngredientService {

    /** 재료 저장소 */
    private final IngredientRepository ingredientRepository;
    /** 레시피 재료 저장소 */
    private final RecipeIngredientRepository recipeIngredientRepository;

    /** 레시피 재료 목록 조회 */
    public List<RecipeIngredientResponse> getRecipeIngredients(final Long recipeId) {
        return recipeIngredientRepository.findAllById_RecipeIdOrderById_IngredientIdAsc(recipeId)
            .stream()
            .map(RecipeIngredientResponse::from)
            .toList();
    }

    /** 레시피 재료 단건 조회 */
    public RecipeIngredientResponse getRecipeIngredient(
        final Long recipeId,
        final Long ingredientId
    ) {
        final RecipeIngredient recipeIngredient = getRecipeIngredientEntity(recipeId, ingredientId);
        return RecipeIngredientResponse.from(recipeIngredient);
    }

    /** 레시피 재료 등록 */
    @Transactional
    public RecipeIngredientResponse createRecipeIngredient(
        final Long recipeId,
        final RecipeIngredientCreateRequest request
    ) {
        final Ingredient ingredient = ingredientRepository.findById(request.ingredientId())
            .orElseThrow(() -> new BusinessException(ErrorCode.INGREDIENT_NOT_FOUND));

        if (recipeIngredientRepository.existsById_RecipeIdAndId_IngredientId(
            recipeId, request.ingredientId())) {
            throw new IllegalArgumentException("이미 등록된 레시피 재료입니다.");
        }

        final RecipeIngredient recipeIngredient = recipeIngredientRepository.save(
            RecipeIngredient.create(
                recipeId,
                ingredient,
                request.amount(),
                request.unit(),
                request.isSubstitutable()
            )
        );

        return RecipeIngredientResponse.from(recipeIngredient);
    }

    /** 레시피 재료 수정 */
    @Transactional
    public RecipeIngredientResponse updateRecipeIngredient(
        final Long recipeId,
        final Long ingredientId,
        final RecipeIngredientUpdateRequest request
    ) {
        final RecipeIngredient recipeIngredient = getRecipeIngredientEntity(recipeId, ingredientId);

        recipeIngredient.update(
            request.amount(),
            request.unit(),
            request.isSubstitutable()
        );

        return RecipeIngredientResponse.from(recipeIngredient);
    }

    /** 레시피 재료 삭제 */
    @Transactional
    public void deleteRecipeIngredient(final Long recipeId, final Long ingredientId) {
        final RecipeIngredient recipeIngredient = getRecipeIngredientEntity(recipeId, ingredientId);
        recipeIngredientRepository.delete(recipeIngredient);
    }

    /** 레시피 재료 조회 */
    private RecipeIngredient getRecipeIngredientEntity(
        final Long recipeId,
        final Long ingredientId
    ) {
        return recipeIngredientRepository.findById_RecipeIdAndId_IngredientId(
                recipeId,
                ingredientId
            )
            .orElseThrow(() -> new BusinessException(ErrorCode.RECIPE_INGREDIENT_NOT_FOUND));
    }
}
