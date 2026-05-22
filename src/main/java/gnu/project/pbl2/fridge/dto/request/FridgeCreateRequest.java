package gnu.project.pbl2.fridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 냉장고 재료 등록 요청 데이터.
 * 어떤 회원의 냉장고에 어떤 재료를 얼마나 넣을지 표현한다.
 */
public record FridgeCreateRequest(
    // 재료를 추가할 회원 식별자
    @NotNull(message = "회원 ID는 필수입니다.")
    Long memberId,
    // 추가할 재료 식별자
    @NotNull(message = "재료 ID는 필수입니다.")
    Long ingredientId,
    // 보유 수량
    @NotNull(message = "수량은 필수입니다.")
    @Positive(message = "수량은 0보다 커야 합니다.")
    BigDecimal quantity,
    // 수량 표현 단위 (기본값: 적음, 중간, 많음 / 직접 입력: 개, g, ml 등)
    @NotBlank(message = "수량 표현 단위는 필수입니다.")
    @Size(max = 20, message = "수량 표현 단위는 20자 이하여야 합니다.")
    String unit,
    // 유통기한
    LocalDate expiryDate
) {
}
