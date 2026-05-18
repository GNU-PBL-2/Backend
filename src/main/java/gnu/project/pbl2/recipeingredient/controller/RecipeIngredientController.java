package gnu.project.pbl2.recipeingredient.controller;

import gnu.project.pbl2.recipeingredient.dto.request.RecipeIngredientCreateRequest;
import gnu.project.pbl2.recipeingredient.dto.request.RecipeIngredientUpdateRequest;
import gnu.project.pbl2.recipeingredient.dto.response.RecipeIngredientResponse;
import gnu.project.pbl2.recipeingredient.service.RecipeIngredientService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 레시피 재료 API */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recipes/{recipeId}/ingredients")
public class RecipeIngredientController {

    /** 레시피 재료 서비스 */
    private final RecipeIngredientService recipeIngredientService;

    /** 레시피 재료 목록 조회 */
    @GetMapping
    public ResponseEntity<List<RecipeIngredientResponse>> getRecipeIngredients(
        @PathVariable final Long recipeId
    ) {
        return ResponseEntity.ok(recipeIngredientService.getRecipeIngredients(recipeId));
    }

    /** 레시피 재료 단건 조회 */
    @GetMapping("/{ingredientId}")
    public ResponseEntity<RecipeIngredientResponse> getRecipeIngredient(
        @PathVariable final Long recipeId,
        @PathVariable final Long ingredientId
    ) {
        return ResponseEntity.ok(
            recipeIngredientService.getRecipeIngredient(recipeId, ingredientId)
        );
    }

    /** 레시피 재료 등록 */
    @PostMapping
    public ResponseEntity<RecipeIngredientResponse> createRecipeIngredient(
        @PathVariable final Long recipeId,
        @Valid @RequestBody final RecipeIngredientCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(recipeIngredientService.createRecipeIngredient(recipeId, request));
    }

    /** 레시피 재료 수정 */
    @PatchMapping("/{ingredientId}")
    public ResponseEntity<RecipeIngredientResponse> updateRecipeIngredient(
        @PathVariable final Long recipeId,
        @PathVariable final Long ingredientId,
        @Valid @RequestBody final RecipeIngredientUpdateRequest request
    ) {
        return ResponseEntity.ok(
            recipeIngredientService.updateRecipeIngredient(recipeId, ingredientId, request)
        );
    }

    /** 레시피 재료 삭제 */
    @DeleteMapping("/{ingredientId}")
    public ResponseEntity<Void> deleteRecipeIngredient(
        @PathVariable final Long recipeId,
        @PathVariable final Long ingredientId
    ) {
        recipeIngredientService.deleteRecipeIngredient(recipeId, ingredientId);
        return ResponseEntity.noContent().build();
    }
}
