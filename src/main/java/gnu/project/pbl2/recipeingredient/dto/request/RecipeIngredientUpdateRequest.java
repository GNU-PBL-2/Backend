package gnu.project.pbl2.recipeingredient.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 레시피 재료 수정 요청 */
public record RecipeIngredientUpdateRequest(
    @NotBlank(message = "재료 양은 필수입니다.")
    @Size(max = 50, message = "재료 양은 50자 이하여야 합니다.")
    String amount,

    @NotBlank(message = "단위는 필수입니다.")
    @Size(max = 20, message = "단위는 20자 이하여야 합니다.")
    String unit,

    @NotNull(message = "대체 가능 여부는 필수입니다.")
    Boolean isSubstitutable
) {
}
