package gnu.project.pbl2.auth.controller.docs;

import gnu.project.pbl2.auth.dto.request.OauthLoginRequest;
import gnu.project.pbl2.auth.dto.response.AuthTokenDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "OAuth 로그인 API")
public interface OauthDocs {

    @Operation(
        summary = "소셜 로그인",
        description = """
            카카오, 네이버, 구글 OAuth 로그인을 처리합니다.
            
            - code: OAuth 인증 코드
            - socialProvider: KAKAO / NAVER / GOOGLE
            - userRole: USER / ADMIN / GUEST
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "로그인 성공",
        content = @Content(schema = @Schema(implementation = AuthTokenDto.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 (code 또는 enum 값 오류)"
    )
    ResponseEntity<AuthTokenDto> login(
        @RequestBody OauthLoginRequest request
    );
}