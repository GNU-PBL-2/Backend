package gnu.project.pbl2.storage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** 보관 방법 등록 요청 */
public record StorageMethodCreateRequest(
    @NotNull(message = "재료 ID는 필수입니다.")
    Long ingredientId,

    @NotBlank(message = "보관 유형은 필수입니다.")
    @Size(max = 20, message = "보관 유형은 20자 이하여야 합니다.")
    String storageType,

    BigDecimal minTemp,
    BigDecimal maxTemp,
    Integer durationDays,
    String tip
) {
}
