package gnu.project.pbl2.Fridge.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 냉장고 재료 등록 요청 데이터.
 * 어떤 회원의 냉장고에 어떤 재료를 얼마나 넣을지 표현한다.
 */
public record FridgeCreateRequest(
    // 재료를 추가할 회원 식별자
    Long memberId,
    // 추가할 재료 식별자
    Long ingredientId,
    // 보유 수량
    BigDecimal quantity,
    // 수량 단위
    String unit,
    // 유통기한
    LocalDate expiryDate
) {
}
