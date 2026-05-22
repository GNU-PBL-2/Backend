package gnu.project.pbl2.recipe.dto.request;

import gnu.project.pbl2.recipe.enumerated.RecipeTab;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record RecipeSearchRequest(
    String keyword,

    RecipeTab tab,

    @Min(0) int page,
    @Min(6) @Max(20) int size

) {

    public RecipeSearchRequest {
        if (tab == null) {
            tab = RecipeTab.ALL;
        }
        if (size == 0) {
            size = 10;
        }
    }
}