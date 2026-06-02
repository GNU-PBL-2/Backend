package gnu.project.pbl2.fridge.controller.docs;

import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.fridge.dto.request.FridgeCreateRequest;
import gnu.project.pbl2.fridge.dto.request.FridgeUpdateRequest;
import gnu.project.pbl2.fridge.dto.response.DetectedIngredientResponse;
import gnu.project.pbl2.fridge.dto.response.FridgeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * 냉장고 재료 관련 API Swagger 문서 인터페이스.
 */
@Tag(name = "Fridge", description = "냉장고 재료 관리 API")
public interface FridgeDocs {

    @Operation(summary = "냉장고 재료 목록 조회", description = "회원 ID로 냉장고에 등록된 재료 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<List<FridgeResponse>> getFridge(
        @Parameter(description = "회원 ID", required = true) Long memberId,
        Accessor accessor
    );

    @Operation(summary = "냉장고 재료 등록", description = "냉장고에 새로운 재료를 추가합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "등록 성공"),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 요청")
    })
    ResponseEntity<FridgeResponse> addIngredient(FridgeCreateRequest request);

    @Operation(summary = "냉장고 재료 수정", description = "냉장고 재료의 수량, 단위, 유통기한을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "404", description = "냉장고 재료를 찾을 수 없음")
    })
    ResponseEntity<FridgeResponse> updateIngredient(
        @Parameter(description = "냉장고 재료 ID", required = true) Long fridgeId,
        FridgeUpdateRequest request
    );

    @Operation(summary = "냉장고 재료 삭제", description = "냉장고 재료 ID로 특정 재료를 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "냉장고 재료를 찾을 수 없음")
    })
    ResponseEntity<Void> deleteIngredient(
        @Parameter(description = "냉장고 재료 ID", required = true) Long fridgeId
    );

    @Operation(
        summary = "카메라 이미지로 재료 자동 감지 (YOLO)",
        description = """
            카메라 또는 갤러리에서 가져온 이미지를 YOLO 모델로 분석해 재료를 자동으로 감지합니다.

            **요청**: `multipart/form-data`, 필드명 `image`

            **응답**:
            - `matched`: DB에 존재하는 재료 목록 (ingredientId, name). 냉장고 등록 화면에 pre-select 용도
            - `unmatched`: YOLO가 감지했으나 DB에 없는 재료명 목록 (참고용)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "감지 성공"),
        @ApiResponse(responseCode = "400", description = "이미지 파일이 아닌 경우"),
        @ApiResponse(responseCode = "500", description = "YOLO 서버 호출 실패")
    })
    @RequestBody(
        content = @Content(
            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
            schema = @Schema(type = "object", requiredProperties = {"image"})
        )
    )
    ResponseEntity<DetectedIngredientResponse> detectIngredients(
        @Parameter(description = "분석할 이미지 파일 (image/jpeg, image/png 등)", required = true)
        MultipartFile image
    );
}
