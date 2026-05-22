package gnu.project.pbl2.recipe.controller.docs;

import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.recipe.dto.request.FavoriteListRequest;
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

@Tag(name = "Favorite", description = "즐겨찾기 API")
public interface FavoriteDocs {

    @Operation(summary = "즐겨찾기 추가", description = "레시피를 즐겨찾기에 추가합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "추가 성공"),
        @ApiResponse(responseCode = "400", description = "이미 즐겨찾기한 레시피", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "레시피를 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<Void> addFavorite(
        @Parameter(description = "레시피 ID", example = "1") Long recipeId,
        @Parameter(hidden = true) Accessor accessor
    );

    @Operation(summary = "즐겨찾기 삭제", description = "레시피를 즐겨찾기에서 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "즐겨찾기를 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<Void> removeFavorite(
        @Parameter(description = "레시피 ID", example = "1") Long recipeId,
        @Parameter(hidden = true) Accessor accessor
    );

    @Operation(
        summary = "즐겨찾기 목록 조회",
        description = "내가 즐겨찾기한 레시피 목록을 페이지네이션으로 조회합니다. 조리가능·임박재료 뱃지도 함께 반환됩니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = RecipeSearchResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<Page<RecipeSearchResponse>> getFavorites(
        @Parameter(description = "즐겨찾기 목록 조회 조건") FavoriteListRequest request,
        @Parameter(hidden = true) Accessor accessor
    );
}
