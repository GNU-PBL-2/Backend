package gnu.project.pbl2.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverUserInfoResponse(

        @JsonProperty("resultcode")
        String resultCode,

        @JsonProperty("message")
        String message,

        @JsonProperty("response") // ✅ 실제 사용자 정보는 'response' 객체 안에 들어있음
        Response response
) {

    // ✅ 'response' 객체의 내부 구조와 일치시킴
    public record Response(
            @JsonProperty("id")
            String id,

            @JsonProperty("email")
            String email,

            @JsonProperty("name")
            String name
    ) {

    }
}
