package gnu.project.pbl2.auth.entity;

import gnu.project.pbl2.auth.enumerated.SocialProvider;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OauthInfo {

    private String email;

    private String name;

    private String socialId;

    @Enumerated(EnumType.STRING)
    private SocialProvider socialProvider;

    public static OauthInfo of(
        final String email,
        final String name,
        final String socialId,
        final SocialProvider provider
    ) {
        return new OauthInfo(email, name, socialId, provider);
    }
}