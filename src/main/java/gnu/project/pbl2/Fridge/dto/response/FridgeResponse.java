package gnu.project.pbl2.Fridge.dto.response;

import gnu.project.pbl2.Fridge.entity.Fridge;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 냉장고 재료 조회/등록/수정 결과를 클라이언트에 반환하는 응답 데이터.
 */
public record FridgeResponse(
    // 냉장고 재료 레코드 식별자
    Long fridgeId,
    // 재료 식별자
    Long ingredientId,
    // 재료명
    String ingredientName,
    // 보유 수량
    BigDecimal quantity,
    // 수량 단위
    String unit,
    // 유통기한
    LocalDate expiryDate
) {

    // 엔티티를 API 응답용 DTO로 변환한다.
    public static FridgeResponse from(final Fridge fridge) {
        return new FridgeResponse(
            fridge.getFridgeId(),
            fridge.getIngredient().getIngredientId(),
            fridge.getIngredient().getName(),
            fridge.getQuantity(),
            fridge.getUnit(),
            fridge.getExpiryDate()
        );
    }
}
