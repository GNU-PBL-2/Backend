package gnu.project.pbl2.recipe.dto.response;

import gnu.project.pbl2.fridge.enumerated.FridgeStatus;
import java.util.List;

public record RecipeResponseDto(

    Long id,
    String title,
    String thumbnailUrl,
    String categoryName,
    Integer cookTimeMin,
    String youtubeUrl,
    boolean favorite,

    List<RecipeIngredientDetail> ingredients,
    List<RecipeStepDetail> steps

) {
    public record RecipeIngredientDetail(
        Long ingredientId,
        String name,
        String amount,
        String unit,
        boolean isSubstitutable,
        FridgeStatus fridgeStatus
    ) {}


    public record RecipeStepDetail(
        int stepOrder,
        String content
    ) {}
}
