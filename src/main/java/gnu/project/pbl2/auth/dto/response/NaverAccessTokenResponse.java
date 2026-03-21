package gnu.project.pbl2.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverAccessTokenResponse(
        @JsonProperty("access_token") // JSON의 access_token 필드를
        String accessToken,           // accessToken 필드에 매핑

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        int expiresIn
) {

}
