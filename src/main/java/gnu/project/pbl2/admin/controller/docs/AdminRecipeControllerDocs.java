package gnu.project.pbl2.admin.controller.docs;

import gnu.project.pbl2.admin.dto.AdminRecipeCreateRequest;
import gnu.project.pbl2.admin.dto.ImportStartResponse;
import gnu.project.pbl2.admin.dto.ImportStatusResponse;
import gnu.project.pbl2.admin.dto.RecipeImportRequest;
import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.recipe.dto.request.RecipeUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Admin", description = "관리자 전용 API")
public interface AdminRecipeControllerDocs {

    @Operation(summary = "유튜브 URL로 레시피 분석 시작",
        description = "유튜브 URL을 입력하면 Gemini AI 분석 작업을 시작하고 jobId를 즉시 반환합니다. (최대 10분 영상)")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "분석 작업 시작됨"),
        @ApiResponse(responseCode = "400", description = "이미 등록된 레시피")
    })
    ResponseEntity<ImportStartResponse> importRecipe(@RequestBody RecipeImportRequest request, Accessor accessor);

    @Operation(summary = "레시피 분석 상태 조회",
        description = "jobId로 분석 진행 상태를 조회합니다. status: pending|processing|completed|failed")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "분석 중 (pending/processing/failed)"),
        @ApiResponse(responseCode = "201", description = "분석 완료 및 레시피 등록"),
        @ApiResponse(responseCode = "404", description = "jobId 없음")
    })
    ResponseEntity<ImportStatusResponse> getImportStatus(String jobId, Accessor accessor);

    @Operation(summary = "레시피 수동 등록", description = "관리자가 직접 레시피를 입력해 등록합니다.")
    ResponseEntity<Void> createRecipe(@RequestBody AdminRecipeCreateRequest request, Accessor accessor);

    @Operation(summary = "레시피 수정", description = "관리자가 레시피 정보를 수정합니다.")
    ResponseEntity<Long> updateRecipe(Long id, @RequestBody RecipeUpdateRequest request, Accessor accessor);

    @Operation(summary = "레시피 삭제", description = "관리자가 레시피를 삭제합니다.")
    ResponseEntity<Long> deleteRecipe(Long id, Accessor accessor);
}
