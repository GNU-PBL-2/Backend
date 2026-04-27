package gnu.project.pbl2.storage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** 보관 방법 수정 요청 */
public record StorageMethodUpdateRequest(
    @NotBlank(message = "보관 유형은 필수입니다.")
    @Size(max = 20, message = "보관 유형은 20자 이하여야 합니다.")
    String storageType,

    BigDecimal minTemp,
    BigDecimal maxTemp,
    Integer durationDays,
    String tip
) {
}
