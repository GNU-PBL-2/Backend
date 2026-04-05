package gnu.project.pbl2.recipe.controller.docs;

import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.recipe.dto.request.RecipeSearchRequest;
import gnu.project.pbl2.recipe.dto.request.RecipeUpdateRequest;
import gnu.project.pbl2.recipe.dto.response.RecipeResponseDto;
import gnu.project.pbl2.recipe.dto.response.RecipeSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Recipe", description = "레시피 API")
public interface RecipeDocs {

    /**
     * 레시피 목록 조회
     */
    @Operation(
        summary = "레시피 목록 조회",
        description = """
            키워드 및 탭 조건을 기반으로 레시피 목록을 조회합니다.
            
            탭 종류:
            - ALL: 전체 레시피
            - COOKABLE: 조리 가능한 레시피
            - EXPIRING: 유통기한 임박 재료 포함
            - FAVORITE: 즐겨찾기
            
            페이지네이션이 적용됩니다.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = RecipeSearchResponse.class))
        )
    })
    ResponseEntity<Page<RecipeSearchResponse>> getRecipes(
        @Parameter(description = "레시피 검색 조건", required = true)
        RecipeSearchRequest request,

        @Parameter(hidden = true)
        Accessor accessor
    );

    /**
     * 레시피 상세 조회
     */
    @Operation(
        summary = "레시피 상세 조회",
        description = "레시피 ID를 기반으로 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "레시피를 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<RecipeResponseDto> getRecipe(
        @Parameter(description = "레시피 ID", example = "1")
        Long id,

        @Parameter(hidden = true)
        Accessor accessor
    );

    /**
     * 레시피 수정 (관리자 전용)
     */
    @Operation(
        summary = "레시피 수정 (관리자)",
        description = """
            레시피 정보를 수정합니다. 
            
            **주의 사항:**
            - 재료(ingredients)와 조리 단계(steps)는 기존 데이터를 삭제하고 전달받은 데이터로 **전체 교체**됩니다.
            - 카테고리와 맛 정보는 전달된 이름을 기반으로 DB에서 찾아 연결합니다.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "수정 성공",
            content = @Content(schema = @Schema(implementation = Long.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "레시피를 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<Long> updateRecipe(
        @Parameter(description = "수정할 레시피 ID", example = "1")
        Long id,

        @RequestBody RecipeUpdateRequest updateRequest,

        @Parameter(hidden = true)
        Accessor accessor
    );

    /**
     * 레시피 삭제 (관리자 전용)
     */
    @Operation(
        summary = "레시피 삭제 (관리자)",
        description = "레시피 ID를 기반으로 레시피를 삭제(상태 변경)합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "삭제 성공",
            content = @Content(schema = @Schema(implementation = Long.class))
        ),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "레시피를 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<Long> deleteRecipe(
        @Parameter(description = "삭제할 레시피 ID", example = "1")
        Long id,

        @Parameter(hidden = true)
        Accessor accessor
    );
}