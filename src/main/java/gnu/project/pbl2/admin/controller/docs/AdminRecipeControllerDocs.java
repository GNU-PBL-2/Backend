package gnu.project.pbl2.admin.controller.docs;

import gnu.project.pbl2.admin.dto.AdminRecipeCreateRequest;
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

    @Operation(summary = "유튜브 URL로 레시피 자동 등록",
        description = "유튜브 URL을 입력하면 Gemini AI가 영상을 분석해서 레시피를 자동 생성합니다.")
    @ApiResponses({@ApiResponse(responseCode = "500", description = "Gemini 분석 실패")})
    ResponseEntity<Void> importRecipe(@RequestBody RecipeImportRequest request, Accessor accessor);

    @Operation(summary = "레시피 수동 등록", description = "관리자가 직접 레시피를 입력해 등록합니다.")
    ResponseEntity<Void> createRecipe(@RequestBody AdminRecipeCreateRequest request, Accessor accessor);

    @Operation(summary = "레시피 수정", description = "관리자가 레시피 정보를 수정합니다.")
    ResponseEntity<Long> updateRecipe(Long id, @RequestBody RecipeUpdateRequest request, Accessor accessor);

    @Operation(summary = "레시피 삭제", description = "관리자가 레시피를 삭제합니다.")
    ResponseEntity<Long> deleteRecipe(Long id, Accessor accessor);
}
