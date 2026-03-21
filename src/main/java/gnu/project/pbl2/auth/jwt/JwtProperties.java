package gnu.project.pbl2.auth.jwt;

import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
@Slf4j
public class JwtProperties {

    private final String secretKey;
    private final long accessTokenExpirationMillis;

    public JwtProperties(
        @Value("${jwt.secret-key}") String secretKey,
        @Value("${jwt.accessToken-expiration-millis}") long accessTokenExpirationMillis
    ) {
        this.secretKey = secretKey;
        this.accessTokenExpirationMillis = accessTokenExpirationMillis;
    }

    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}
