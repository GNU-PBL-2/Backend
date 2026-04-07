package gnu.project.pbl2.Fridge.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FridgeCreateRequest(
    Long memberId,
    Long ingredientId,
    BigDecimal quantity,
    String unit,
    LocalDate expiryDate
) {
}
