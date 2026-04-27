package gnu.project.pbl2.fridge.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 냉장고 재료 수정 요청 데이터.
 * 수량, 단위, 유통기한처럼 냉장고에 저장된 재료 정보를 갱신할 때 사용한다.
 */
public record FridgeUpdateRequest(
    // 수정할 수량
    BigDecimal quantity,
    // 수정할 단위
    String unit,
    // 수정할 유통기한
    LocalDate expiryDate
) {
}
