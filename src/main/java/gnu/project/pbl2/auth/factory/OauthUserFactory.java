package gnu.project.pbl2.auth.factory;

import gnu.project.pbl2.auth.entity.OauthUser;
import gnu.project.pbl2.auth.enumerated.SocialProvider;
import gnu.project.pbl2.auth.userinfo.OauthUserInfo;
import gnu.project.pbl2.common.enumerated.UserRole;
import gnu.project.pbl2.user.entity.User;
import gnu.project.pbl2.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OauthUserFactory {

    private final UserRepository userRepository;

    public OauthUser findOrCreateUser(OauthUserInfo userInfo, SocialProvider provider,
        UserRole userRole) {
        return switch (userRole) {
            case USER -> findOrCreateUser(userInfo, provider);
            case ADMIN -> null;
            case GUEST -> null;
             /*
            TODO : 추후 추가 예정
             */

        };
    }


    private User findOrCreateUser(OauthUserInfo userInfo, SocialProvider provider) {
        return userRepository.findByOauthInfo_SocialId(userInfo.getSocialId())
            .orElseGet(() -> userRepository.save(
                User.createFromOAuth(
                    userInfo.getEmail(),
                    userInfo.getName(),
                    userInfo.getSocialId(),
                    provider
                )
            ));
    }

}
