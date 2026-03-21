package gnu.project.pbl2.user.dto.request;

import java.util.List;

public record UserOnboardRequest(
    List<Long> allergies,
    List<Long> tastes,
    List<Long> categories
) {
    public UserOnboardRequest {
        allergies  = allergies  != null ? allergies  : List.of();
        tastes    = tastes    != null ? tastes    : List.of();
        categories = categories != null ? categories : List.of();
    }
}
