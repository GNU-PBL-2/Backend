package gnu.project.pbl2.recipe.dto.request;

import java.util.List;

public record RecipeUpdateRequest(
    String title,
    String categoryName,
    String tasteName,
    Integer cookTimeMin,
    String description,
    List<IngredientDto> ingredients,
    List<String> steps
) {

    public record IngredientDto(
        String name,
        String amount,
        String unit,
        boolean isSubstitutable
    ) {

    }
}
