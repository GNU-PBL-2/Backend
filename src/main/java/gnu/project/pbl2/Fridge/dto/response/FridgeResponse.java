@Getter
@AllArgsConstructor
public class FridgeResponse {

    private Long ingredientId;
    private String ingredientName;
    private BigDecimal quantity;
    private String unit;
    private LocalDate expiryDate;

    public static FridgeResponse from(Fridge fridge) {
        return new FridgeResponse(
                fridge.getIngredient().getIngredientId(),
                fridge.getIngredient().getName(),
                fridge.getQuantity(),
                fridge.getUnit(),
                fridge.getExpiryDate()
        );
    }
}