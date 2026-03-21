package gnu.project.pbl2.auth.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class JwtConstants {

    // ClaimNames
    public static final String TOKEN_TYPE = "token_type";
    public static final String USER_ROLE = "user_role";
    public static final String USER_ID = "user_id";

    // JwtInterceptor
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();

    // Request Attribute
    public static final String REQUEST_ATTR_SOCIAL_ID = "socialId";
    public static final String REQUEST_ATTR_USER_ROLE = "userRole";
    public static final String REQUEST_ATTR_USER_ID = "userId";

}
