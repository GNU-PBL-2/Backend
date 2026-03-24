package gnu.project.pbl2.user.controller.docs;

import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.user.dto.request.UserOnboardRequest;
import gnu.project.pbl2.user.dto.response.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User", description = "유저 API")
public interface UserDocs {

    @Operation(
        summary = "온보딩 선호도 저장",
        description = "회원가입 후 온보딩 단계에서 알레르기 / 취향 / 카테고리 선호도를 저장합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "온보딩 저장 성공",
            content = @Content(schema = @Schema(implementation = UserResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "유저를 찾을 수 없음",
            content = @Content(schema = @Schema(hidden = true))
        )
    })
    ResponseEntity<UserResponseDto> saveOnboard(
        @RequestBody UserOnboardRequest request,
        Accessor accessor
    );
    @Operation(
        summary = "유저 정보 조회",
        description = "현재 로그인한 유저의 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = UserResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "유저를 찾을 수 없음",
            content = @Content(schema = @Schema(hidden = true))
        )
    })
    ResponseEntity<UserResponseDto> getUserInfo(
        Accessor accessor
    );
    @Operation(
        summary = "회원 탈퇴",
        description = "현재 로그인한 유저를 탈퇴 처리합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "탈퇴 성공",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "유저를 찾을 수 없음",
            content = @Content(schema = @Schema(hidden = true))
        )
    })
    ResponseEntity<String> deleteUser(
        Accessor accessor
    );
}
