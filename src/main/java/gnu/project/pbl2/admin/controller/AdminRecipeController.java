package gnu.project.pbl2.admin.controller;

import gnu.project.pbl2.admin.controller.docs.AdminRecipeControllerDocs;
import gnu.project.pbl2.admin.dto.RecipeImportRequest;
import gnu.project.pbl2.admin.service.RecipeImportService;
import gnu.project.pbl2.auth.aop.Auth;
import gnu.project.pbl2.auth.aop.OnlyAdmin;
import gnu.project.pbl2.auth.entity.Accessor;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/recipes")
public class AdminRecipeController implements AdminRecipeControllerDocs {

    private final RecipeImportService recipeImportService;

    @OnlyAdmin
    @PostMapping("/import")
    @Override
    public ResponseEntity<Void> importRecipe(@RequestBody @Valid final RecipeImportRequest request,
        @Auth final Accessor accessor) {
        Long recipeId = recipeImportService.importFromYoutube(request.youtubeUrl());
        return ResponseEntity
            .created(URI.create("/api/v1/recipes/" + recipeId))
            .build();
    }

}
