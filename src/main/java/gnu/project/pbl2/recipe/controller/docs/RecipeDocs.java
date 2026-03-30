package gnu.project.pbl2.recipe.controller.docs;

import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.recipe.dto.request.RecipeSearchRequest;
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

@Tag(name = "Recipe", description = "레시피 API")
public interface RecipeDocs {

    /**
     * 레시피 목록 조회 ------- (1)
     * <p>
     * 키워드 및 탭 조건(전체, 조리 가능, 임박, 즐겨찾기)에 따라
     * 레시피 목록을 페이징 형태로 조회한다.
     * <p>
     * - ALL: 전체 레시피 조회
     * - COOKABLE: 보유 재료로 조리 가능한 레시피
     * - EXPIRING: 임박 재료 포함 레시피
     * - FAVORITE: 즐겨찾기 레시피
     *
     * @param request 검색 조건 (키워드, 탭, 페이지)
     * @param accessor 인증된 사용자 정보
     * @return 레시피 목록 (Page)
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
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (페이지, 사이즈 등)",
            content = @Content(schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(hidden = true))
        )
    })
    ResponseEntity<Page<RecipeSearchResponse>> getRecipes(
        @Parameter(description = "레시피 검색 조건", required = true)
        RecipeSearchRequest request,

        @Parameter(hidden = true)
        Accessor accessor
    );

    /**
     * 레시피 상세 조회 ------- (1)
     * <p>
     * 레시피 ID를 기반으로 상세 정보를 조회한다.
     * <p>
     * 반환 정보:
     * - 기본 정보 (제목, 썸네일, 조리시간)
     * - 재료 목록
     * - 조리 단계
     * - 카테고리 / 맛 정보
     *
     * @param id 레시피 ID
     * @param accessor 인증된 사용자 정보
     * @return 레시피 상세 정보
     */
    @Operation(
        summary = "레시피 상세 조회",
        description = """
            레시피 ID를 기반으로 상세 정보를 조회합니다.
            
            포함 정보:
            - 레시피 기본 정보
            - 재료 목록
            - 조리 단계
            - 카테고리 및 맛
            
            사용자 기준으로 즐겨찾기 여부도 포함될 수 있습니다.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = RecipeResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "레시피를 찾을 수 없음",
            content = @Content(schema = @Schema(hidden = true))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(hidden = true))
        )
    })
    ResponseEntity<RecipeResponseDto> getRecipe(
        @Parameter(description = "레시피 ID", example = "1")
        Long id,

        @Parameter(hidden = true)
        Accessor accessor
    );
}