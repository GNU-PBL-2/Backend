package gnu.project.pbl2.auth.provider;

import gnu.project.pbl2.auth.enumerated.SocialProvider;
import gnu.project.pbl2.auth.userinfo.OauthUserInfo;

public interface OauthProvider {

    SocialProvider getProvider();

    //naver 추후 parmeter state 추가
    String getAccessToken(String code);

    OauthUserInfo getUserInfo(String accessToken);


}
