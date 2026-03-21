package gnu.project.pbl2.auth.entity;

import gnu.project.pbl2.auth.enumerated.SocialProvider;
import gnu.project.pbl2.common.enumerated.UserRole;

public interface OauthUser {

    Long getId();

    OauthInfo getOauthInfo();

    UserRole getUserRole();

    default String getEmail() {
        return getOauthInfo().getEmail();
    }

    default String getName() {
        return getOauthInfo().getName();
    }

    default String getSocialId() {
        return getOauthInfo().getSocialId();
    }

    default SocialProvider getSocialProvider() {
        return getOauthInfo().getSocialProvider();
    }
}
