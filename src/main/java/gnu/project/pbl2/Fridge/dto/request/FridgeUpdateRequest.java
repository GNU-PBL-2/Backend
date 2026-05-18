package gnu.project.pbl2.Fridge.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 냉장고 재료 수정 요청 데이터.
 * 수량, 단위, 유통기한처럼 냉장고에 저장된 재료 정보를 갱신할 때 사용한다.
 */
public record FridgeUpdateRequest(
    // 수정할 수량
    @Positive(message = "수량은 0보다 커야 합니다.")
    BigDecimal quantity,
    // 수정할 수량 표현 단위 (기본값: 적음, 중간, 많음 / 직접 입력: 개, g, ml 등)
    @Pattern(regexp = ".*\\S.*", message = "수량 표현 단위는 공백일 수 없습니다.")
    @Size(max = 20, message = "수량 표현 단위는 20자 이하여야 합니다.")
    String unit,
    // 수정할 유통기한
    LocalDate expiryDate
) {

    public boolean hasUpdatableField() {
        return quantity != null || unit != null || expiryDate != null;
    }
}
