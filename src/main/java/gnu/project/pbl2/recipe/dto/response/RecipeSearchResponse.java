package gnu.project.pbl2.recipe.dto.response;

import gnu.project.pbl2.fridge.enumerated.FridgeStatus;
import java.util.List;

public record RecipeSearchResponse(
    Long id,
    String title,
    String thumbnailUrl,
    Integer cookTimeMin,
    boolean cookable,
    int expiringIngredientCount,
    boolean isFavorite,
    List<IngredientSummary> ingredients
) {
    public record IngredientSummary(
        Long ingredientId,
        String name,
        FridgeStatus fridgeStatus
    ) {}

    public static RecipeSearchResponse ofBase(
        Long id, String title, String thumbnailUrl, Integer cookTimeMin
    ) {
        return new RecipeSearchResponse(id, title, thumbnailUrl, cookTimeMin, false, 0, false, List.of());
    }

    public RecipeSearchResponse withBadgeAndIngredients(
        boolean cookable, int expiringCount, boolean favorite,
        List<IngredientSummary> ingredients
    ) {
        return new RecipeSearchResponse(
            this.id, this.title, this.thumbnailUrl, this.cookTimeMin,
            cookable, expiringCount, favorite, ingredients
        );
    }
}
