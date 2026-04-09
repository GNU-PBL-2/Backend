package gnu.project.pbl2.storage.dto.response;

import gnu.project.pbl2.storage.entity.StorageMethod;
import java.math.BigDecimal;

/** 보관 방법 응답 */
public record StorageMethodResponse(
    Long storageId,
    Long ingredientId,
    String ingredientName,
    String storageType,
    BigDecimal minTemp,
    BigDecimal maxTemp,
    Integer durationDays,
    String tip
) {

    /** 엔티티를 응답으로 변환 */
    public static StorageMethodResponse from(final StorageMethod storageMethod) {
        return new StorageMethodResponse(
            storageMethod.getStorageId(),
            storageMethod.getIngredient().getIngredientId(),
            storageMethod.getIngredient().getName(),
            storageMethod.getStorageType(),
            storageMethod.getMinTemp(),
            storageMethod.getMaxTemp(),
            storageMethod.getDurationDays(),
            storageMethod.getTip()
        );
    }
}
