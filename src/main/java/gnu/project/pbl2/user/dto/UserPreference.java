package gnu.project.pbl2.user.dto;

import java.util.List;

public record UserPreference(
    List<Long> categoryIds,
    List<Long> tasteIds,
    List<Long> allergenIngredientIds
) {

    public static UserPreference empty() {
        return new UserPreference(List.of(), List.of(), List.of());
    }

    public boolean hasCategoryPreference() {
        return !categoryIds.isEmpty();
    }

    public boolean hasTastePreference() {
        return !tasteIds.isEmpty();
    }

    public boolean hasAllergyRestriction() {
        return !allergenIngredientIds.isEmpty();
    }
}
