package gnu.project.pbl2.auth.jwt;

import static gnu.project.pbl2.auth.constant.JwtConstants.TOKEN_TYPE;
import static gnu.project.pbl2.auth.constant.JwtConstants.USER_ID;
import static gnu.project.pbl2.auth.constant.JwtConstants.USER_ROLE;

import gnu.project.pbl2.auth.enumerated.TokenType;
import gnu.project.pbl2.common.enumerated.UserRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;

    public String createAccessToken(final Long userId, final String socialId,
        final UserRole userRole) {
        return createToken(
            userId,
            socialId,
            jwtProperties.getAccessTokenExpirationMillis(),
            TokenType.ACCESS_TOKEN,
            userRole
        );
    }

    // TODO: 추후 사용
    public String createRefreshToken(final Long userId, final String socialId,
        final UserRole userRole) {
        return createToken(
            userId,
            socialId,
            jwtProperties.getAccessTokenExpirationMillis(),
            TokenType.REFRESH_TOKEN,
            userRole
        );
    }

    private String createToken(
        final Long userId,
        final String socialId,
        final long expirationMillis,
        final TokenType tokenType,
        final UserRole userRole
    ) {
        final Date now = new Date();
        final Date expiredDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
            .setSubject(socialId)
            .claim(USER_ID, userId)
            .claim(USER_ROLE, userRole)
            .setIssuedAt(now)
            .setExpiration(expiredDate)
            .claim(TOKEN_TYPE, tokenType.name())
            .signWith(jwtProperties.getSecretKey(), SignatureAlgorithm.HS256)
            .compact();
    }
}
