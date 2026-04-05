package gnu.project.pbl2.admin.controller;

import gnu.project.pbl2.admin.controller.docs.AdminRecipeControllerDocs;
import gnu.project.pbl2.admin.dto.RecipeImportRequest;
import gnu.project.pbl2.admin.service.RecipeImportService;
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

    //TODO : @OnlyAdmin 추가
    @PostMapping("/import")
    @Override
    public ResponseEntity<Void> importRecipe(@RequestBody @Valid final RecipeImportRequest request) {
        Long recipeId = recipeImportService.importFromYoutube(request.youtubeUrl());
        return ResponseEntity
            .created(URI.create("/api/v1/recipe/" + recipeId))
            .build();
    }

}
