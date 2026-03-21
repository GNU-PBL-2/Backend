package gnu.project.pbl2.auth.jwt;

import static gnu.project.pbl2.auth.constant.JwtConstants.AUTHORIZATION_HEADER;
import static gnu.project.pbl2.auth.constant.JwtConstants.BEARER_PREFIX_LENGTH;
import static gnu.project.pbl2.auth.constant.JwtConstants.REQUEST_ATTR_SOCIAL_ID;
import static gnu.project.pbl2.auth.constant.JwtConstants.REQUEST_ATTR_USER_ID;
import static gnu.project.pbl2.auth.constant.JwtConstants.REQUEST_ATTR_USER_ROLE;
import static gnu.project.pbl2.auth.constant.KakaoOauthConstants.BEARER_PREFIX;
import static gnu.project.pbl2.common.error.ErrorCode.AUTH_TOKEN_INVALID;

import gnu.project.pbl2.common.enumerated.UserRole;
import gnu.project.pbl2.common.exception.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtResolver jwtResolver;

    @Override
    public boolean preHandle(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler
    ) throws Exception {

        final String token = extractToken(request);

        if (token == null) {
            setGuestAttributes(request);
            return true;
        }

        if (!jwtResolver.isValid(token)) {
            throw new AuthException(AUTH_TOKEN_INVALID);
        }

        setUserAttributes(request, token);
        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return header.substring(BEARER_PREFIX_LENGTH);
    }

    private void setUserAttributes(HttpServletRequest request, String token) {
        String socialId = jwtResolver.extractSocialId(token);
        UserRole userRole = jwtResolver.extractUserRole(token);
        Long userId = jwtResolver.extractUserId(token);

        request.setAttribute(REQUEST_ATTR_SOCIAL_ID, socialId);
        request.setAttribute(REQUEST_ATTR_USER_ID, userId);
        request.setAttribute(REQUEST_ATTR_USER_ROLE, userRole);
    }

    private void setGuestAttributes(HttpServletRequest request) {
        request.setAttribute(REQUEST_ATTR_USER_ROLE, UserRole.GUEST);
    }
}
