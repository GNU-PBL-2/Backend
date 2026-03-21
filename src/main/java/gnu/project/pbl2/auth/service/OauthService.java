package gnu.project.pbl2.auth.service;

import static gnu.project.pbl2.common.error.ErrorCode.AUTH_USER_NOT_FOUND;

import gnu.project.pbl2.auth.dto.request.OauthLoginRequest;
import gnu.project.pbl2.auth.dto.response.AuthTokenDto;
import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.auth.entity.OauthUser;
import gnu.project.pbl2.auth.factory.OauthUserFactory;
import gnu.project.pbl2.auth.jwt.JwtProvider;
import gnu.project.pbl2.auth.provider.OauthProvider;
import gnu.project.pbl2.auth.provider.OauthProviders;
import gnu.project.pbl2.auth.userinfo.OauthUserInfo;
import gnu.project.pbl2.common.enumerated.UserRole;
import gnu.project.pbl2.common.exception.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OauthService {

    private final OauthProviders oauthProviders;
    private final OauthUserFactory oauthUserFactory;
    private final JwtProvider jwtProvider;

    public AuthTokenDto login(final OauthLoginRequest request) {
        final OauthProvider provider = oauthProviders.getProvider(request.socialProvider());
        final String accessToken = provider.getAccessToken(request.code());
        final OauthUserInfo userInfo = provider.getUserInfo(accessToken);
        final OauthUser user = oauthUserFactory.findOrCreateUser(
            userInfo,
            provider.getProvider(),
            request.userRole()
        );
        return AuthTokenDto.of(
            jwtProvider.createAccessToken(user.getId(), user.getSocialId(), user.getUserRole())
        );
    }

    public Accessor getCurrentAccessor(final String socialId, final Long userId,
        final UserRole userRole) {
        log.debug("Getting accessor for socialId: {}, userRole: {}", socialId, userRole);

        if (!isUserExists(socialId, userRole)) {
            log.warn("User not found for socialId: {}, userRole: {}", socialId, userRole);
            throw new AuthException(AUTH_USER_NOT_FOUND);
        }

        return Accessor.user(socialId, userId, userRole);
    }

    private boolean isUserExists(String socialId, UserRole userRole) {
        return switch (userRole) {
//            case USER -> ownerRepository.existsByOauthInfo_SocialId(socialId);
//            case ADMIN -> customerRepository.existsByOauthInfo_SocialId(socialId);
            default -> false;
        };
    }

}
