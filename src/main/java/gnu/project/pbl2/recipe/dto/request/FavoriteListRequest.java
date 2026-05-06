package gnu.project.pbl2.recipe.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record FavoriteListRequest(
    String keyword,
    @Min(0) int page,
    @Min(6) @Max(20) int size
) {
    public FavoriteListRequest {
        if (size == 0) {
            size = 10;
        }
    }
}
