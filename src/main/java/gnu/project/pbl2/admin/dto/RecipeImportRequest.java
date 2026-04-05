package gnu.project.pbl2.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RecipeImportRequest(

    @NotBlank(message = "유튜브 URL을 입력해주세요")
    @Pattern(
        regexp = "^(https?://)?(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/)[\\w-]+.*$",
        message = "올바른 유튜브 URL 형식이 아닙니다"
    )
    String youtubeUrl

) {}
