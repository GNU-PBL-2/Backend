package gnu.project.pbl2.admin.dto;

import java.util.List;

public record GeminiRecipeDto(
    String title,
    String categoryName,        // "한식", "양식" 등 — DB category.name과 매핑
    String tasteName,           // "매운맛" 등   — DB taste.name과 매핑
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
    ) {}
}