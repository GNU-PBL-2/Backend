package gnu.project.pbl2.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CartItemAddRequest(
    Long ingredientId,       // 레시피 재료 ID (직접 입력 시 null 허용)
    @NotBlank String name,
    @NotNull @Min(1) Integer quantity,
    String unit
) {}
