package gnu.project.pbl2.auth.dto.request;


import gnu.project.pbl2.auth.enumerated.SocialProvider;
import gnu.project.pbl2.common.enumerated.UserRole;

public record OauthLoginRequest(
    String code,
    SocialProvider socialProvider,
    UserRole userRole
    // String state //naver 필드
) {

}
