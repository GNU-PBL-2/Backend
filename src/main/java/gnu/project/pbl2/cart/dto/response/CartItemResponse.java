package gnu.project.pbl2.cart.dto.response;

import gnu.project.pbl2.cart.entity.CartItem;

public record CartItemResponse(
    Long cartItemId,
    Long ingredientId,
    String name,
    Integer quantity,
    String unit,
    boolean isChecked
) {
    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(
            item.getCartItemId(),
            item.getIngredientId(),
            item.getName(),
            item.getQuantity(),
            item.getUnit(),
            item.isChecked()
        );
    }
}
