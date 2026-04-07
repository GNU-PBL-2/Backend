package gnu.project.pbl2.Fridge.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FridgeUpdateRequest(
    BigDecimal quantity,
    String unit,
    LocalDate expiryDate
) {
}
