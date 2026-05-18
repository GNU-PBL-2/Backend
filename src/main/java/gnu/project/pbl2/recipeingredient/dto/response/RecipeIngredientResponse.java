package gnu.project.pbl2.recipeingredient.dto.response;

import gnu.project.pbl2.recipeingredient.entity.RecipeIngredient;
import java.math.BigDecimal;

/** 레시피 재료 응답 */
public record RecipeIngredientResponse(
    Long recipeId,
    Long ingredientId,
    String ingredientName,
    BigDecimal amount,
    String unit,
    Boolean isSubstitutable
) {

    /** 엔티티를 응답으로 변환 */
    public static RecipeIngredientResponse from(final RecipeIngredient recipeIngredient) {
        return new RecipeIngredientResponse(
            recipeIngredient.getRecipeId(),
            recipeIngredient.getIngredientId(),
            recipeIngredient.getIngredient().getName(),
            recipeIngredient.getAmount(),
            recipeIngredient.getUnit(),
            recipeIngredient.getIsSubstitutable()
        );
    }
}
