package gnu.project.pbl2.recipe.controller;

import gnu.project.pbl2.auth.aop.Auth;
import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.recipe.controller.docs.RecipeDocs;
import gnu.project.pbl2.recipe.dto.request.RecipeSearchRequest;
import gnu.project.pbl2.recipe.dto.response.RecipeResponseDto;
import gnu.project.pbl2.recipe.dto.response.RecipeSearchResponse;
import gnu.project.pbl2.recipe.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recipes")
public class RecipeController implements RecipeDocs {

    private final RecipeService recipeService;

    @GetMapping()
    public ResponseEntity<Page<RecipeSearchResponse>> getRecipes(
        @ModelAttribute @Valid final RecipeSearchRequest request,
        @Auth final Accessor accessor
    ) {
        return ResponseEntity.ok(recipeService.getRecipes(request,accessor));
    }
    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponseDto> getRecipe(
        @PathVariable(name = "id") final Long id,
        @Auth final Accessor accessor
    ) {
        return ResponseEntity.ok(recipeService.getRecipeDetail(id,accessor));
    }

}
