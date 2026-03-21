package gnu.project.pbl2.auth.dto.request;


import gnu.project.pbl2.auth.enumerated.SocialProvider;
import gnu.project.pbl2.common.enumerated.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

public record OauthLoginRequest(
    @Schema(description = "OAuth 인증 코드", example = "abc123")
    String code,
    @Schema(description = "소셜 로그인 제공자", example = "KAKAO")
    SocialProvider socialProvider,
    @Schema(description = "유저 역할", example = "USER")
    UserRole userRole
    // String state //naver 필드
) {

}
