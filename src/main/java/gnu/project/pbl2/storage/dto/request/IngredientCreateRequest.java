package gnu.project.pbl2.storage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 재료 등록 요청 */
public record IngredientCreateRequest(
    @NotBlank(message = "재료명은 필수입니다.")
    @Size(max = 100, message = "재료명은 100자 이하여야 합니다.")
    String name,

    @NotNull(message = "카테고리 ID는 필수입니다.")
    Long categoryId
) {
}
