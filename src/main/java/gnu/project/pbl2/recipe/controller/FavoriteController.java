package gnu.project.pbl2.recipe.controller;

import gnu.project.pbl2.auth.aop.Auth;
import gnu.project.pbl2.auth.aop.OnlyUser;
import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.recipe.controller.docs.FavoriteDocs;
import gnu.project.pbl2.recipe.dto.request.FavoriteListRequest;
import gnu.project.pbl2.recipe.dto.response.RecipeSearchResponse;
import gnu.project.pbl2.recipe.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recipes")
public class FavoriteController implements FavoriteDocs {

    private final FavoriteService favoriteService;

    @OnlyUser
    @PostMapping("/{recipeId}/favorites")
    public ResponseEntity<Void> addFavorite(
        @PathVariable final Long recipeId,
        @Auth final Accessor accessor
    ) {
        favoriteService.add(recipeId, accessor);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @OnlyUser
    @DeleteMapping("/{recipeId}/favorites")
    public ResponseEntity<Void> removeFavorite(
        @PathVariable final Long recipeId,
        @Auth final Accessor accessor
    ) {
        favoriteService.remove(recipeId, accessor);
        return ResponseEntity.noContent().build();
    }

    @OnlyUser
    @GetMapping("/favorites")
    public ResponseEntity<Page<RecipeSearchResponse>> getFavorites(
        @ModelAttribute @Valid final FavoriteListRequest request,
        @Auth final Accessor accessor
    ) {
        return ResponseEntity.ok(favoriteService.getFavorites(request, accessor));
    }
}
