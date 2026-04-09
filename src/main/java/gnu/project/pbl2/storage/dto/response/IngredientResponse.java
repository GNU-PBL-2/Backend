package gnu.project.pbl2.storage.dto.response;

import gnu.project.pbl2.Fridge.entity.Ingredient;
import gnu.project.pbl2.storage.entity.StorageMethod;
import java.util.List;

/** 재료 응답 */
public record IngredientResponse(
    Long ingredientId,
    String name,
    Long categoryId,
    String categoryName,
    List<StorageMethodResponse> storageMethods
) {

    /** 엔티티를 응답으로 변환 */
    public static IngredientResponse from(
        final Ingredient ingredient,
        final List<StorageMethod> storageMethods
    ) {
        return new IngredientResponse(
            ingredient.getIngredientId(),
            ingredient.getName(),
            ingredient.getCategory().getId(),
            ingredient.getCategory().getName(),
            storageMethods.stream()
                .map(StorageMethodResponse::from)
                .toList()
        );
    }
}
