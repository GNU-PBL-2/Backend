package gnu.project.pbl2.auth.userinfo;

import org.springframework.stereotype.Component;

@Component
public interface OauthUserInfo {

    String getSocialId();

    String getEmail();

    String getName();
    
}
