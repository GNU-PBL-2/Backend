package gnu.project.pbl2.user.dto.response;

import gnu.project.pbl2.user.entity.User;
import java.util.List;
import java.util.UUID;

public record UserResponseDto(
    UUID publicId,
    String email,
    String name,
    List<String> allergies,
    List<String> tastes,
    List<String> categories
) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(
            user.getPublicId(),
            user.getOauthInfo().getEmail(),
            user.getOauthInfo().getName(),
            user.getAllergyNames(),   // User가 직접 제공
            user.getTasteNames(),
            user.getCategoryNames()
        );
    }
}