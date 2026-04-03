package gnu.project.pbl2.admin.controller.docs;

import gnu.project.pbl2.admin.dto.RecipeImportRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Admin", description = "관리자 전용 API")
public interface AdminRecipeControllerDocs {

    @Operation(
        summary = "유튜브 URL로 레시피 자동 등록",
        description = "유튜브 URL을 입력하면 Gemini AI가 영상을 직접 분석해서 재료/조리법을 추출 후 DB에 저장합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "레시피 등록 성공"),
        @ApiResponse(responseCode = "409", description = "이미 등록된 유튜브 URL"),
        @ApiResponse(responseCode = "500", description = "Gemini 분석 실패")
    })
    ResponseEntity<Void> importRecipe(@RequestBody RecipeImportRequest request);
}
