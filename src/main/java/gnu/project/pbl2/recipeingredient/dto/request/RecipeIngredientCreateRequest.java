package gnu.project.pbl2.recipeingredient.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** 레시피 재료 등록 요청 */
public record RecipeIngredientCreateRequest(
    @NotNull(message = "재료 ID는 필수입니다.")
    Long ingredientId,

    @NotNull(message = "재료 양은 필수입니다.")
    BigDecimal amount,

    @NotBlank(message = "단위는 필수입니다.")
    @Size(max = 20, message = "단위는 20자 이하여야 합니다.")
    String unit,

    @NotNull(message = "대체 가능 여부는 필수입니다.")
    Boolean isSubstitutable
) {
}
