package gnu.project.pbl2.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AdminRecipeCreateRequest(
    @NotBlank String title,
    String thumbnailUrl,
    @NotBlank String categoryName,
    @NotBlank String tasteName,
    @NotNull @Min(1) Integer cookTimeMin,
    String description,
    String youtubeUrl,
    List<IngredientDto> ingredients,
    List<String> steps
) {
    public record IngredientDto(
        String name,
        String amount,
        String unit,
        boolean isSubstitutable
    ) {}
}
