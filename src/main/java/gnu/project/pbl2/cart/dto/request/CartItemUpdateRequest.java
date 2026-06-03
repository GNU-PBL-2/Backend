package gnu.project.pbl2.cart.dto.request;

import jakarta.validation.constraints.Min;

public record CartItemUpdateRequest(
    @Min(1) Integer quantity,   // null이면 수정 안 함
    Boolean isChecked           // null이면 수정 안 함
) {}
