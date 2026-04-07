package gnu.project.pbl2.Fridge.dto.response;

import gnu.project.pbl2.Fridge.entity.Fridge;
import java.math.BigDecimal;
import java.time.LocalDate;

public record FridgeResponse(
    Long fridgeId,
    Long ingredientId,
    String ingredientName,
    BigDecimal quantity,
    String unit,
    LocalDate expiryDate
) {

    public static FridgeResponse from(final Fridge fridge) {
        return new FridgeResponse(
            fridge.getFridgeId(),
            fridge.getIngredient().getIngredientId(),
            fridge.getIngredient().getName(),
            fridge.getQuantity(),
            fridge.getUnit(),
            fridge.getExpiryDate()
        );
    }
}
